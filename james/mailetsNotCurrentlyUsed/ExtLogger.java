package org.apache.james.transport.mailets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jwm.entity.*;
import org.apache.mailet.base.GenericMailet;
import org.apache.mailet.Mail;
import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.MessagingException;
import javax.sql.DataSource;

public class ExtLogger extends JWMGenericMailet
{
    String logMessage = "Error: unknown event type in ExtLogger Mailet definition";
    int    historyType = History.TYPE_ERROR;

    public void init() throws MessagingException
    {
        logger = LoggerFactory.getLogger(ExtLogger.class);
        String event = getInitParameter("event");
        if (event != null )
        {
           if (event.equals( "EXT_LOG_INBOUND" ) )
           {
              logMessage = "Inbound";
              historyType = History.TYPE_RECEIVED_EMAIL3x;
           }
           else if (event.equals( "EXT_LOG_OUTBOUND" ) )
           {
              logMessage = "Outbound";
              historyType = History.TYPE_SENT_EMAIL3x;
           }
           else if (event.equals( "EXT_LOG_DELIVERED" ) )
           {
              logMessage = "Delivered";
              historyType = History.TYPE_MAIL_DELIVERED;
           }
           else if (event.equals( "EXT_LOG_REMOTEDELIVERY" ) )
           {
              logMessage = "Remote Delivery";
              historyType = History.TYPE_MAIL_DELIVERED;
           }
           else if (event.equals( "EXT_LOG_BOUNCE" ) )
           {
              logMessage = "Bounce";
              historyType = History.TYPE_BOUNCE;
           }
           else if (event.equals( "EXT_LOG_REMOTEDELIVERY_NO_GATEWAY" ) )
           {
              logMessage = "Remote Delivery -- No Gateway";
              historyType = History.TYPE_MAIL_DELIVERED;
           }
           else
           {
              logMessage = event;
           }
        }
    }

    @Override
    public void service(Mail mail) throws MessagingException
    {
       logger.info( logMessage + " -- " + mail.getName() );
    }

}
