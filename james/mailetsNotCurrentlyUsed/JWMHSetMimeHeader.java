package org.apache.james.transport.mailets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage ;
import org.apache.mailet.base.GenericMailet ;
import org.apache.mailet.Mail ;
import org.apache.james.mailrepository.api.*;
import javax.annotation.Resource;
import java.util.Enumeration;
/**
 * <p>Adds a specified header and value to the message.</p>
 *
 * <p>Sample configuration:</p>
 *
 * <pre><code>
 * &lt;mailet match="All" class="AddHeader"&gt;
 *   &lt;name&gt;X-MailetHeader&lt;/name&gt;
 *   &lt;value&gt;TheHeaderValue&lt;/value&gt;
 * &lt;/mailet&gt;
 * </code></pre>
 *
 * @version 1.0.0, 2002-09-11
 */
public class JWMHSetMimeHeader extends JWMGenericMailet
{
    private String headerName;
    private String headerValue;

    public void init() throws MessagingException
    {
        logger = LoggerFactory.getLogger(JWMHSetMimeHeader.class);
        headerName = getInitParameter("name");
        headerValue = getInitParameter("value");

        // Check if needed config values are used
        if (headerName == null || headerName.equals("") || headerValue == null || headerValue.equals(""))
        {
            throw new MessagingException("Please configure a name and a value");
        }
    }

    public void service(Mail mail) throws MessagingException
    {
        try
        {
            MimeMessage message = mail.getMessage () ;
            message.setHeader(headerName, headerValue);
            logger.debug( headerName + "=" + headerValue );
            message.saveChanges();
        }
        catch (javax.mail.MessagingException me)
        {
            log ("JWMHSetMimeHeader exception-error -- " + me.getMessage());
        }
    }

    public String getMailetInfo()
    {
        return "SetMimeHeader Mailet" ;
    }
}
