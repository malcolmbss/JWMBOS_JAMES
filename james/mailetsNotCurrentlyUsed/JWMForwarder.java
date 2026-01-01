package org.apache.james.transport.mailets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import org.apache.mailet.Mail;
import org.apache.james.core.MailAddress;
import org.apache.james.dnsservice.api.DNSService;
import javax.mail.MessagingException;
import javax.mail.internet.*;
import javax.inject.Inject;

public class JWMForwarder extends Resend
{
    @Inject
    public JWMForwarder(DNSService dns)
    {
        super(dns);
    }

    public String getMailetInfo() {
        return "JWM Forwarder Mailet";
    }

    // get recipient from special header.... not from parm passed in
    protected void setRecipients(Mail newMail, Collection recipients1, Mail originalMail)
    {
        Collection recipients = new HashSet();
        String addressList = "";
        try
        {
            addressList = originalMail.getMessage().getHeader( "X_JWMH_CC_FORWARDER", null );
            newMail.getMessage().removeHeader( "X_JWMH_CC_FORWARDER" ); // remove the header for the resend... otherwise, infinite loop!!
            newMail.getMessage().removeHeader( "X_JWMH_BCC" ); // also remove bcc headers from forward

            InternetAddress[] iaarray = InternetAddress.parse(addressList, false);
            for (InternetAddress anIaarray : iaarray)
            {
               recipients.add(new MailAddress(anIaarray));
            }
        }
        catch (Exception e)
        {
            log("Exception thrown in getRecipients() parsing: " + addressList, e);
        }
        if (recipients.size() == 0)
        {
            log("Failed to initialize \"recipients\" list; empty <recipients> init parameter found.");
        }
        newMail.setRecipients(recipients);
    }
}
