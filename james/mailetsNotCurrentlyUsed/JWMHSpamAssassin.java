package org.apache.james.transport.mailets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mailet.base.GenericMailet;
import org.apache.mailet.Mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.inject.Inject;
import org.apache.james.metrics.api.MetricFactory;
import org.apache.james.user.api.UsersRepository;

public class JWMHSpamAssassin extends JamesSpamAssassin {

    protected Logger logger = LoggerFactory.getLogger(JWMHSpamAssassin.class);

    public final static String STATUS_MAIL_ATTRIBUTE_NAME = "org.apache.james.spamassassin.status";
    public final static String FLAG_MAIL_ATTRIBUTE_NAME = "org.apache.james.spamassassin.flag";

    public void init() throws MessagingException {
        super.init();
        logger = LoggerFactory.getLogger(JWMHSpamAssassin.class);
    }

    public void service(Mail mail)
    {
       super.service( mail );
       MimeMessage message = null;

       try
       {
          logger.debug("Enter JWMHSpamAssassin.service() from:  -- " + mail.getSender() );
          super.service( mail );

          message = mail.getMessage();

          String spamStatus = (String)mail.getAttribute( STATUS_MAIL_ATTRIBUTE_NAME );
          if ( spamStatus != null )
          {
             String[] statusArray = spamStatus.split(",");

             if ( statusArray.length > 0 )
             {

                message.setHeader("X-Spam-Flag", statusArray[0].toUpperCase().trim());
                if ( statusArray[0].toUpperCase().equals("YES") ) message.setHeader("X-Spam-Flag-True", "True" );
                logger.debug( "JWMHSpamAssassin.service() setHeader: X-Spam-Flag "+statusArray[0].toUpperCase().trim());
             }
             else
             {
                message.setHeader("X-Spam-Flag", "Error" );
             }

             message.setHeader("X-Spam-Status", spamStatus.trim() );
          }
          else // spamAssassin error
          {
             message.setHeader("X-Spam-Flag", "Error" );
             message.setHeader("X-Spam-Status", "Error" );
             message.setHeader("X-Fatal-Abort", "True" );
             logger.debug("***** JWMHSpamAssassin.service() fatal abort on email." );
          }

          message.saveChanges();
       }
       catch (MessagingException e)
       {
           try
           {
              message.setHeader("X-SpamAssassin-Mailet-Exception",  e.getMessage() );
              logger.debug("JWMHSpamAssassin " + e.getMessage());
           }
           catch( Exception e1 )
           {
              logger.debug("JWMHSpamAssassin " + e1.getMessage());
           }
       }
       logger.debug("Exit JWMHSpamAssassin.service() from:  -- " + mail.getSender() + " - " + (String)mail.getAttribute( FLAG_MAIL_ATTRIBUTE_NAME ) + " " + (String)mail.getAttribute( STATUS_MAIL_ATTRIBUTE_NAME ));
    }
}
