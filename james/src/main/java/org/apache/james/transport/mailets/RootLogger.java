package org.apache.james.transport.mailets;

import java.util.Collection;

import org.apache.james.core.MailAddress;
import org.apache.mailet.Mail;
import org.slf4j.LoggerFactory;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

public class RootLogger extends JWMGenericMailet
{
    public void init() throws MessagingException
    {
       logger = LoggerFactory.getLogger(RootLogger.class);
    }

    @Override
    public void service(Mail mail) throws MessagingException
    {
       try
       {
          Address[] fromAddresses = mail.getMessage().getFrom();
          InternetAddress fromAddress = (InternetAddress) fromAddresses[0];
          String logMessage = ">>> Mail entering pipeline -- From: " + fromAddress.getAddress() + " To: ";

          Collection<MailAddress> recipients = mail.getRecipients();
          for (MailAddress recipient : recipients)
          {
                logMessage += recipient.asString() + " | ";
          }
          logMessage += "msgId: " + mail.getName();
          logger.info( logMessage );
       }
       catch( Exception e)
       {
          logger.error( "Excption in RootLogger " + e + " " + mail.getName());
       }
    }
}
