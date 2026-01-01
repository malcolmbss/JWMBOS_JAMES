package org.apache.james.transport.mailets;

import java.util.*;
import org.apache.mailet.Mail;
import org.apache.james.core.MailAddress;
import org.apache.james.dnsservice.api.DNSService;
import javax.mail.MessagingException;
import javax.mail.internet.*;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mailet.base.StringUtils;

public class JWMResend extends Resend
{
    protected Logger logger = LoggerFactory.getLogger(JWMResend.class);
    String addressList = "";

    @Inject
    public JWMResend(DNSService dns)
    {
        super(dns);
    }

    public String getMailetInfo() {
        return "JWM Resend Mailet";
    }

    public void service(Mail mail) throws MessagingException
    {
       addressList = mail.getMessage().getHeader( "X_JWMH_BCC", null );
       mail.getMessage().removeHeader( "X_JWMH_BCC" ); // remove the header for the resend... otherwise, infinite loop!!
       mail.getMessage().removeHeader( "X_JWMH_CC_FORWARDER" ); // also remove forwards from bcc resend
       super.service( mail );
    }


    public List<InternetAddress> getTo() throws MessagingException
    {
       return( getResendRecipients() );
    }

    @Override
    public List<MailAddress> getTo(Mail originalMail) throws MessagingException
    {
       List<MailAddress> recipients = new ArrayList();
       List<InternetAddress> iAddresses = getResendRecipients();

       for (InternetAddress iAddress : iAddresses)
       {
          recipients.add( new MailAddress( iAddress ) );
       }
       return( recipients );
    }

    // get recipient from special header.... not from parm passed in
    protected List<InternetAddress> getResendRecipients()
    {
        logger.info( "Getting bcc address: " + addressList );
        List<InternetAddress> recipients = new ArrayList();
        try
        {
            InternetAddress[] iaarray = InternetAddress.parse(addressList, false);
            for (InternetAddress anIaarray : iaarray)
            {
               recipients.add(anIaarray);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception thrown in getRecipients() parsing: " + addressList, e);
        }
        if (recipients.size() == 0)
        {
            logger.error("Failed to initialize \"recipients\" list; empty <recipients> init parameter found.");
        }
        return( recipients );
    }
}
