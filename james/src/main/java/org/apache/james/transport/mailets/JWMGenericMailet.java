package org.apache.james.transport.mailets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.james.core.MailAddress;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class JWMGenericMailet extends GenericMailet
{
    public static int SENDER = 1;
    public static int RECIPIENTS = 2;

    protected Logger logger = LoggerFactory.getLogger(JWMGenericMailet.class);

    public void log(String message)
    {
       logger.info("+ " +message);
    }

    String getHeader( MimeMessage message, String header )
    {
       String[] headerValues = new String[0];
       try
       {
          headerValues= message.getHeader( header );
          String value = "--";
          if (headerValues == null ) return( value );
          if (headerValues.length == 0 ) return( value );
       }
       catch( Exception e)
       {
          return( "--" );
       }
       log( "Header: " + header + " = " + headerValues[0] );
       return( headerValues[0] );
    }

    int getIntHeader( MimeMessage message, String header )
    {
       String headerValue = getHeader( message, header );
       int rc = -1;
       try
       {
          rc = (new Integer( headerValue )).intValue();
       }
       catch( Exception e)
       {
       } // ignore error
       log( "Header (int): " + header + " = " + rc );
       return( rc );
    }

    public void showJWMBOSLog( String[] loggerStrings )
    {
       logger.debug( "\n\nvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv Custom Mailet JAMES Logs" );
       logger.debug( "showJWMBOSLog() count: " + loggerStrings.length );
       for (int i = 0; i < loggerStrings.length; i++ )
       {
          logger.debug( loggerStrings[i] );
       }
       logger.debug( "\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" );
    }

    public String getHeader( Mail mail, String headerName )
    {
       try
       {
          String hdr = mail.getMessage().getHeader( headerName, null );
          if ( hdr != null ) return(hdr);
       }
       catch( Exception e)
       {
       }
       return( headerName + "not found" );
    }

    public MailAddress getEmailAddress( Mail mail, int type ) throws MessagingException
    {
       return( getEmailAddress( mail, type, 0 ));
    }

    public MailAddress getEmailAddress( Mail mail, int type, int ndx ) throws MessagingException
    {
        if ( type == SENDER )
        {
           if (ndx == 0 ) return( mail.getSender() );
           return( null );
        }

        MailAddress[] addrs =  mail.getRecipients().toArray(new MailAddress[0]);
        if ( ndx >= addrs.length ) return( null );
        return( addrs[ndx] );
    }
 }
