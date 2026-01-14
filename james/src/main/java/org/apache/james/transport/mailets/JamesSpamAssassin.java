package org.apache.james.transport.mailets;

import org.apache.mailet.base.GenericMailet;
import org.apache.mailet.Mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends the message through daemonized SpamAssassin (spamd), visit <a
 * href="http://spamassassin.apache.org/">spamassassin.apache.org/</a> for info
 * on configuration. The header X-Spam-Status is added to every message, this
 * contains the score and the threshold score for spam (usually 5.0). If the
 * message exceeds the threshold, the header X-Spam-Flag will be added with the
 * value of YES. The default host for spamd is localhost and the default port is
 * 783.
 *
 * <pre>
 * <code>
 *  org.apache.james.spamassassin.status - Holds the status
 *  org.apache.james.spamassassin.flag   - Holds the flag
 * </code>
 * </pre>
 *
 * Sample Configuration:
 *
 * <pre>
 * &lt;mailet notmatch="SenderHostIsLocal" class="SpamAssassin"&gt;
 * &lt;spamdHost&gt;localhost&lt;/spamdHost&gt;
 * &lt;spamdPort&gt;783&lt;/spamdPort&gt;
 * </pre>
 */
public class JamesSpamAssassin extends GenericMailet {

    String spamdHost;

    int spamdPort;

    protected Logger logger = LoggerFactory.getLogger(JamesSpamAssassin.class);

    /**
     * @see org.apache.mailet.base.GenericMailet#init()
     */
    @Override public void init(){
        spamdHost = getInitParameter("spamdHost");
        if (spamdHost == null || spamdHost.equals("")) {
            spamdHost = "127.0.0.1";
        }

        String port = getInitParameter("spamdPort");
        if (port == null || port.equals("")) {
            spamdPort = 783;
        } else {

            try {
                spamdPort = Integer.parseInt(getInitParameter("spamdPort"));
            } catch (NumberFormatException e) {
                logger.error("Please configure a valid port. Not valid: " + spamdPort);
            }
        }
    }

    /**
     * @see org.apache.mailet.base.GenericMailet#service(Mail)
     */
    @Override public void service(Mail mail) {
        try {
            MimeMessage message = mail.getMessage();

            // Invoke spamassian connection and scan the message
            logger.debug( "calling JWMSpamAssassinInvoker (spamd)" );
            JWMSpamAssassinInvoker sa = new JWMSpamAssassinInvoker(spamdHost, spamdPort);
            sa.scanMail(message);

            // Add headers as attribute to mail object
            for (String key : sa.getHeadersAsAttribute().keySet())
            {
                String headerValue = sa.getHeadersAsAttribute().get(key);
                message.setHeader(key, headerValue);
                logger.debug("(spamd) - JWMHSpamAssassin Add Header: " + key + " = " + headerValue);
            }

            message.saveChanges();
        } catch (MessagingException e) {
            logger.debug("(spamd) - " + e.getMessage());
        }

    }

    /**
     * @see org.apache.mailet.base.GenericMailet#getMailetInfo()
     */
    @Override public String getMailetInfo() {
        return "Checks message against SpamAssassin";
    }
}
