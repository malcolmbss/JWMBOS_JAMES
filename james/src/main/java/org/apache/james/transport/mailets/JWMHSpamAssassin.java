package org.apache.james.transport.mailets;

import org.apache.mailet.Mail;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
// import org.apache.james.core.Attribute;
// import org.apache.james.core.AttributeName;

public class JWMHSpamAssassin extends JamesSpamAssassin {

   public final static String STATUS_MAIL_ATTRIBUTE_NAME = "org.apache.james.spamassassin.status";
   public final static String FLAG_MAIL_ATTRIBUTE_NAME = "org.apache.james.spamassassin.flag";

   @Override
   public void init() {
      super.init();
      logger = LoggerFactory.getLogger(JWMHSpamAssassin.class);
   }

   @Override
   public void service(Mail mail) {
      super.service(mail);
      MimeMessage message = null;

      try {
         logger.debug("Enter JWMHSpamAssassin.service() from:  -- " + mail.getMaybeSender().asString());
         super.service(mail);

         message = mail.getMessage();

         String[] spamStatusHeader = message.getHeader(JWMSpamAssassinInvoker.STATUS_MAIL_ATTRIBUTE_NAME);
         if (spamStatusHeader != null) {
            String[] statusArray = spamStatusHeader[0].split(",");

            if (statusArray.length > 0) {
               message.setHeader("X-Spam-Flag", statusArray[0].toUpperCase().trim());
               if (statusArray[0].toUpperCase().equals("YES")) {
                  message.setHeader("X-Spam-Flag-True", "True");
               }
               message.setSubject("[Spam Score: " + message.getHeader("X-Spam-Score")[0] + "] " + message.getSubject());
               logger.debug("JWMHSpamAssassin.service() setHeader: X-Spam-Flag " + statusArray[0].toUpperCase().trim());
            } else {
               message.setHeader("X-Spam-Flag", "Error");
            }

            message.setHeader("X-Spam-Status", spamStatusHeader[0].trim());
         } else // spamAssassin error
         {
            message.setSubject("[Spam: Error] " + message.getSubject());
            message.setHeader("X-Spam-Flag", "Error");
            message.setHeader("X-Spam-Status", "Error");
            message.setHeader("X-Fatal-Abort", "True");
            logger.debug("***** JWMHSpamAssassin.service() fatal abort on email.");
         }

         message.saveChanges();
      } catch (MessagingException e) {
         try {
            message.setHeader("X-SpamAssassin-Mailet-Exception", e.getMessage());
            logger.debug("JWMHSpamAssassin " + e.getMessage());
         } catch (Exception e1) {
            logger.debug("JWMHSpamAssassin " + e1.getMessage());
         }
      }
      logger.debug("Exit JWMHSpamAssassin.service() from:  -- " + mail.getMaybeSender().asString() + " - "
            + (String) mail.getAttribute(FLAG_MAIL_ATTRIBUTE_NAME) + " "
            + (String) mail.getAttribute(STATUS_MAIL_ATTRIBUTE_NAME));
   }
}
