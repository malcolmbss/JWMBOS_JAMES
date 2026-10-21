package org.apache.james.transport.mailets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.james.backends.postgres.PostgresConfiguration;
import org.apache.james.core.MailAddress;
import org.apache.james.core.Username;
import org.apache.james.server.core.MailImpl;
import org.apache.james.user.api.UsersRepository;
import org.apache.mailet.Attribute;
import org.apache.mailet.AttributeName;
import org.apache.mailet.Mail;
import org.apache.mailet.StorageDirective;
import org.apache.mailet.base.GenericMailet;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Files an archive copy of authenticated outgoing mail in the sender's folders.
 * The original message continues through the existing outbound pipeline.
 *
 * REQUIRED: implement loadActiveSieveScript() before deployment.
 * Wire this mailet once in authorized-outbound, before ToProcessor transport.
 *
 * Supported Sieve mapping:
 * if allof (header :contains "from" "Amazon.com") {
 * fileinto "Amazon";
 * }
 *
 * This is a restricted mapping extractor, NOT a Sieve interpreter.
 * Only exact supported top-level blocks are used. Nested/complex blocks,
 * Spam rules, and unsupported actions are ignored.
 *
 * Filing occurs on submission, not after SES confirms delivery.
 * The queued marker prevents repeats on the same persisted Mail attributes,
 * but cannot guarantee exactly-once filing across queue/crash boundaries.
 */
public class JWMOutboundFolderArchive extends GenericMailet {
    private DataSource dataSource;

    private String active_sieve_script = "SELECT script_content FROM sieve_scripts WHERE username = ? AND is_active is TRUE";

    private static final Logger LOGGER = LoggerFactory.getLogger(JWMOutboundFolderArchive.class);

    private static final AttributeName ARCHIVE_COPY = AttributeName.of("com.jwmhosting.outboundArchive.copy");
    private static final AttributeName ARCHIVE_QUEUED = AttributeName.of("com.jwmhosting.outboundArchive.queued");

    private static final String QUOTED = "\"((?:\\\\.|[^\"\\\\])*)\"";
    private static final Pattern SIMPLE_RULE = Pattern.compile(
            "\\s*if\\s+allof\\s*\\(\\s*header\\s+:contains\\s+\"from\"\\s+"
                    + QUOTED + "\\s*\\)\\s*\\{\\s*fileinto\\s+"
                    + QUOTED + "\\s*;\\s*\\}\\s*",
            Pattern.CASE_INSENSITIVE);

    private final UsersRepository usersRepository;
    private String archiveProcessor;
    private String outboundKeyword;

    @Inject
    public JWMOutboundFolderArchive(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public void init() throws MessagingException {
        archiveProcessor = getInitParameter("archiveProcessor", "outbound-archive");
        outboundKeyword = getInitParameter("outboundKeyword", "$label3");
        if (!archiveProcessor.matches("[A-Za-z0-9_-]+")) {
            throw new MessagingException(
                    "archiveProcessor/outboundKeyword must be simple identifiers");
        }
    }
@Inject
   public void setPostgresConfiguration(PostgresConfiguration config) {

      PGSimpleDataSource ds = new PGSimpleDataSource();

      ds.setServerNames(new String[] { config.getHost() });
      ds.setPortNumbers(new int[] { config.getPort() });
      ds.setDatabaseName(config.getDatabaseName());

      ds.setUser(config.getDefaultCredential().getUsername());
      ds.setPassword(config.getDefaultCredential().getPassword());

      ds.setCurrentSchema(config.getDatabaseSchema());

      this.dataSource = ds;
   }

    @Override
    public void service(Mail original) throws MessagingException {
        if (isTrue(original, ARCHIVE_COPY) || isTrue(original, ARCHIVE_QUEUED)) {
            return;
        }

        // Use the authenticated identity, never the untrusted From header.
        Optional<String> authenticatedUser = original.getAttribute(Mail.SMTP_AUTH_USER)
                .flatMap(attribute -> attribute.getValue().valueAs(String.class))
                .filter(value -> !value.isBlank());

        if (authenticatedUser.isEmpty()) {
            return;
        }

        try {
            Username owner = Username.of(authenticatedUser.get());
            if (!usersRepository.contains(owner)) {
                LOGGER.warn("Outbound archive skipped for mail {}: unknown authenticated user",
                        original.getName());
                return;
            }

            String sieve = loadActiveSieveScript(owner);
            Set<String> folders = resolveFolders(original.getMessage(), sieve);
            if (folders.isEmpty()) {
                return;
            }

            MailAddress ownerAddress = usersRepository.getMailAddressFor(owner);
            StorageDirective directive = StorageDirective.builder()
                    .targetFolders(new ArrayList<>(folders))
                    .seen(Optional.of(true))
                    .keywords(Optional.<Collection<String>>of(List.of(outboundKeyword)))
                    .build();

            MailImpl copy = MailImpl.duplicate(original);
            try {
                copy.removeAllAttributes();
                copy.setRecipients(List.of(ownerAddress));
                // No SMTP sender: archive problems must not bounce to correspondents.
                copy.setSender(null);
                copy.setState(archiveProcessor);
                copy.setAttribute(Attribute.convertToAttribute(
                        ARCHIVE_COPY.asString(), true));
                directive.encodeAsAttributes(
                        usersRepository.getUsername(ownerAddress))
                        .forEach(copy::setAttribute);

                // Queue directly to the archive processor, bypassing normal routing.
                getMailetContext().sendMail(copy, archiveProcessor);
                original.setAttribute(Attribute.convertToAttribute(
                        ARCHIVE_QUEUED.asString(), true));
            } finally {
                // sendMail queues its own representation; release our local copy.
                copy.dispose();
            }
        } catch (Exception exception) {
            // Archive failure must not prevent the original outgoing delivery.
            LOGGER.error("Outbound archive preparation/queueing failed for mail {}; "
                    + "original delivery continues", original.getName(), exception);
        }
    }

    private static boolean isTrue(Mail mail, AttributeName name) {
        return mail.getAttribute(name)
                .flatMap(attribute -> attribute.getValue().valueAs(Boolean.class))
                .orElse(false);
    }

    protected String loadActiveSieveScript(Username owner) throws Exception {
        /*
         * TODO: RETRIEVE THE USER'S ACTIVE SIEVE SCRIPT FROM YOUR DATABASE.
         *
         * Use owner.asString() as the authenticated mailbox identity.
         * Use a parameterized query and return ONLY the active script.
         * Return "" if there is no active script.
         * Close JDBC resources using try-with-resources.
         * Let DB errors propagate to service(), which logs and permits sending.
         *
         * Example shape:
         * try (Connection connection = dataSource.getConnection();
         * PreparedStatement statement = connection.prepareStatement(...)) {
         * statement.setString(1, owner.asString());
         * try (ResultSet rows = statement.executeQuery()) {
         * return rows.next() ? rows.getString(...) : "";
         * }
         * }
         */

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(active_sieve_script)) {
            statement.setString(1, owner.asString());
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getString("script_content") : "";
            }
        }
    }

    /**
     * Returns distinct destination folders in rule order. Matches actual To/Cc
     * addresses case-insensitively; ignores display names and Bcc.
     * Folder equality remains case-sensitive.
     */
    public static Set<String> resolveFolders(
            MimeMessage message,
            String activeSieveScript) throws MessagingException {

        Set<String> folders = new LinkedHashSet<>();

        if (message == null) {
            return folders;
        }

        List<String> recipients = new ArrayList<>();
        addRecipients(message, Message.RecipientType.TO, recipients);
        addRecipients(message, Message.RecipientType.CC, recipients);

        if (recipients.isEmpty()) {
            return folders;
        }

        String cleaned = removeComments(
                activeSieveScript == null ? "" : activeSieveScript);

        List<String> statements = cleaned == null ? null : topLevelStatements(cleaned);

        // Missing or malformed scripts provide no usable mappings.
        if (statements == null) {
            statements = List.of();
        }

        boolean[] matched = new boolean[recipients.size()];

        // Preserve Sieve rule order for matching folders.
        for (String statement : statements) {
            Matcher rule = SIMPLE_RULE.matcher(statement);

            if (!rule.matches()) {
                continue;
            }

            String needle = unescape(rule.group(1))
                    .toLowerCase(Locale.ROOT);
            String folder = unescape(rule.group(2));

            if (needle.isBlank() || folder.isBlank()
                    || folder.indexOf('\0') >= 0
                    || folder.indexOf('\r') >= 0
                    || folder.indexOf('\n') >= 0) {
                continue;
            }

            for (int i = 0; i < recipients.size(); i++) {
                if (recipients.get(i).contains(needle)) {
                    matched[i] = true;
                    folders.add(folder);
                }
            }
        }

        // Any unmatched To/Cc address adds one INBOX destination.
        for (boolean addressMatched : matched) {
            if (!addressMatched) {
                folders.add("INBOX");
                break;
            }
        }

        return folders;
    }

    private static void addRecipients(MimeMessage message,
            Message.RecipientType type, List<String> recipients)
            throws MessagingException {
        Address[] addresses = message.getRecipients(type);
        if (addresses == null) {
            return;
        }
        for (Address address : addresses) {
            if (address instanceof InternetAddress internetAddress) {
                addAddress(internetAddress, recipients);
            }
        }
    }

    private static void addAddress(InternetAddress address,
            List<String> recipients) throws MessagingException {
        if (address.isGroup()) {
            InternetAddress[] members = address.getGroup(false);
            if (members != null) {
                for (InternetAddress member : members) {
                    addAddress(member, recipients);
                }
            }
        } else if (address.getAddress() != null) {
            recipients.add(address.getAddress().toLowerCase(Locale.ROOT));
        }
    }

    // Only match whole top-level blocks, never a rule nested inside another rule.
    private static List<String> topLevelStatements(String script) {
        List<String> statements = new ArrayList<>();
        boolean quoted = false;
        int depth = 0;
        int start = 0;
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            if (quoted) {
                if (c == '\\') {
                    if (++i >= script.length()) {
                        return null;
                    }
                } else if (c == '"') {
                    quoted = false;
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                if (--depth < 0) {
                    return null;
                }
                if (depth == 0) {
                    statements.add(script.substring(start, i + 1));
                    start = i + 1;
                }
            } else if (c == ';' && depth == 0) {
                statements.add(script.substring(start, i + 1));
                start = i + 1;
            }
        }
        if (quoted || depth != 0 || !script.substring(start).isBlank()) {
            return null;
        }
        return statements;
    }

    private static String removeComments(String script) {
        StringBuilder result = new StringBuilder(script.length());
        boolean quoted = false;
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            if (quoted) {
                result.append(c);
                if (c == '\\') {
                    if (++i >= script.length()) {
                        return null;
                    }
                    result.append(script.charAt(i));
                } else if (c == '"') {
                    quoted = false;
                }
            } else if (c == '"') {
                quoted = true;
                result.append(c);
            } else if (c == '#') {
                while (i < script.length() && script.charAt(i) != '\n'
                        && script.charAt(i) != '\r') {
                    i++;
                }
                result.append('\n');
            } else if (c == '/' && i + 1 < script.length()
                    && script.charAt(i + 1) == '*') {
                int end = script.indexOf("*/", i + 2);
                if (end < 0) {
                    return null;
                }
                result.append(' ');
                i = end + 1;
            } else {
                result.append(c);
            }
        }
        return quoted ? null : result.toString();
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                c = value.charAt(++i);
            }
            result.append(c);
        }
        return result.toString();
    }
}
