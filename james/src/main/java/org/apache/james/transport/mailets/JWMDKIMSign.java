package org.apache.james.transport.mailets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.james.core.Domain;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.api.DomainListException;
import org.apache.james.jdkim.DKIMSigner;
import org.apache.james.jdkim.api.BodyHasher;
import org.apache.james.jdkim.api.Headers;
import org.apache.james.jdkim.api.SignatureRecord;
import org.apache.james.jdkim.exceptions.PermFailException;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;

import jakarta.inject.Inject;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class JWMDKIMSign extends GenericMailet {

    private final DomainList domainList;

    private PrivateKey privateKey;
    private String selector;
    private boolean forceCRLF;

    @Inject
    public JWMDKIMSign(DomainList domainList) {
        this.domainList = domainList;
    }

    @Override
    public void init() throws MessagingException {

        selector = getInitParameter("selector", "primary");
        forceCRLF = getInitParameter("forceCRLF", true);

        String keyFile = getInitParameter("privateKeyFilepath");

        if (keyFile == null || keyFile.isBlank()) {
            throw new MessagingException(
                    "JWMDKIMSign: privateKeyFilepath is required");
        }

        try {
            privateKey = loadPrivateKey(Path.of(keyFile));
        } catch (Exception e) {
            throw new MessagingException(
                    "JWMDKIMSign: Unable to load private key", e);
        }

        log("JWMDKIMSign initialized. Selector=" + selector);
    }

    @Override
    public void service(Mail mail) throws MessagingException {

        MimeMessage message = mail.getMessage();

        String domain = getFromDomain(message);

        if (domain == null) {
            log("JWMDKIMSign: Skipping message with invalid From header");
            return;
        }

        // Only sign mail originating from domains hosted by James.

        try {
            if (!domainList.containsDomain(Domain.of(domain))) {
                log("JWMDKIMSign: Skipping nonlocal domain " + domain);
                return;
            }
        } catch (DomainListException e) {
            throw new MessagingException(
                    "JWMDKIMSign: Domain lookup failed", e);
        }

        String template = "v=1; " +
                "s=" + selector + "; " +
                "d=" + domain + "; " +
                "c=relaxed/relaxed; " +
                "h=Subject:From:To:MIME-Version:Content-Type; " +
                "a=rsa-sha256; " +
                "bh=; b=;";

        try {

            DKIMSigner signer = new DKIMSigner(template, privateKey);

            SignatureRecord record = signer.newSignatureRecordTemplate(template);

            BodyHasher hasher = signer.newBodyHasher(record);

            Headers headers = new JWMMessageHeaders(message);

            // Stream the serialized MIME message into the body hasher.

            try (OutputStream bodyStream = new MIMEBodyOutputStream(
                    hasher.getOutputStream(),
                    forceCRLF)) {

                message.writeTo(bodyStream);
            }

            String signature = signer.sign(headers, hasher);

            prependSignature(message, signature);

            log("JWMDKIMSign: Signed message for " + domain);

        } catch (IOException | PermFailException e) {

            throw new MessagingException(
                    "JWMDKIMSign: Signing failed for " + domain, e);
        }
    }

    // ------------------------------------------------------------
    // Extract domain from RFC5322 From header
    // ------------------------------------------------------------

    private String getFromDomain(MimeMessage message)
            throws MessagingException {

        Address[] addresses = message.getFrom();

        if (addresses == null ||
                addresses.length != 1 ||
                !(addresses[0] instanceof InternetAddress)) {

            return null;
        }

        String address = ((InternetAddress) addresses[0]).getAddress();

        if (address == null) {
            return null;
        }

        int at = address.lastIndexOf('@');

        if (at <= 0 || at == address.length() - 1) {
            return null;
        }

        return address.substring(at + 1)
                .toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------
    // Load PKCS8 RSA private key
    // ------------------------------------------------------------

    private PrivateKey loadPrivateKey(Path path)
            throws Exception {

        String pem = Files.readString(path);

        pem = pem.replace(
                "-----BEGIN PRIVATE KEY-----", "")
                .replace(
                        "-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(pem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(spec);
    }

    // ------------------------------------------------------------
    // Add DKIM-Signature header
    // ------------------------------------------------------------

    private void prependSignature(
            MimeMessage message,
            String signature)
            throws MessagingException {

        String value = signature.replaceFirst(
                "(?i)^DKIM-Signature:\\s*", "");

        message.addHeader("DKIM-Signature", value);
    }

    // ------------------------------------------------------------
    // Implementation of Apache JDKIM Headers interface
    // ------------------------------------------------------------

    private static class JWMMessageHeaders implements Headers {

        private final MimeMessage message;

        JWMMessageHeaders(MimeMessage message) {
            this.message = message;
        }

        @Override
        public List<String> getFields() {

            try {

                List<String> fields = new ArrayList<>();

                Enumeration<?> e = message.getAllHeaderLines();

                while (e.hasMoreElements()) {
                    fields.add((String) e.nextElement());
                }

                return fields;

            } catch (MessagingException e) {

                throw new IllegalStateException(
                        "Unable to read message headers", e);
            }
        }

        @Override
        public List<String> getFields(String name) {

            try {

                String[] fields = message.getHeader(name);

                if (fields == null) {
                    return List.of();
                }

                List<String> result = new ArrayList<>();

                for (String field : fields) {
                    result.add(name + ": " + field);
                }

                return result;

            } catch (MessagingException e) {

                throw new IllegalStateException(
                        "Unable to read header " + name, e);
            }
        }
    }

    // ------------------------------------------------------------
    // Streaming MIME body extractor with optional CRLF conversion
    // ------------------------------------------------------------

    private static class MIMEBodyOutputStream extends OutputStream {

        private final OutputStream output;
        private final boolean forceCRLF;

        private boolean inHeaders = true;

        private int separatorState = 0;
        private boolean pendingCR = false;

        MIMEBodyOutputStream(
                OutputStream output,
                boolean forceCRLF) {

            this.output = output;
            this.forceCRLF = forceCRLF;
        }

        @Override
        public void write(int value) throws IOException {

            int b = value & 0xff;

            if (inHeaders) {

                // Detect CRLF CRLF separating headers from body.

                switch (separatorState) {

                    case 0:
                        separatorState = (b == '\r') ? 1 : 0;
                        break;

                    case 1:
                        separatorState = (b == '\n') ? 2 : (b == '\r') ? 1 : 0;
                        break;

                    case 2:
                        separatorState = (b == '\r') ? 3 : 0;
                        break;

                    case 3:
                        if (b == '\n') {
                            inHeaders = false;
                        }
                        separatorState = 0;
                        break;
                }

                return;
            }

            if (!forceCRLF) {
                output.write(b);
                return;
            }

            // Normalize line endings to CRLF.

            if (pendingCR) {

                pendingCR = false;

                if (b == '\n') {
                    output.write('\r');
                    output.write('\n');
                    return;
                }

                output.write('\r');
                output.write('\n');
            }

            if (b == '\r') {
                pendingCR = true;
            } else if (b == '\n') {
                output.write('\r');
                output.write('\n');
            } else {
                output.write(b);
            }
        }

        @Override
        public void flush() throws IOException {
            output.flush();
        }

        @Override
        public void close() throws IOException {

            if (pendingCR) {
                output.write('\r');
                output.write('\n');
                pendingCR = false;
            }

            output.close();
        }
    }
}