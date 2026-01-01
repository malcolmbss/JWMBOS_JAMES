package org.apache.james.transport.mailets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.mailet.base.GenericMailet;
import org.apache.mailet.Mail;
import org.apache.james.core.MailAddress;
import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import jwm.db.*;
import jwm.entity.*;
import jwm.logger.*;
import jwm.servletdb.*;
import jwm.servletlogger.*;
import javax.sql.DataSource;
import javax.mail.Address;

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
