package org.apache.james.transport.matchers;
import org.apache.mailet.GenericMatcher;  // base class to extend
import org.apache.mailet.Mail;            // mail object
import org.apache.mailet.MailAddress;     // sender/recipient addresses

import javax.mail.MessagingException;     // for any JavaMail exceptions
import java.util.Collection;              // return type for matched recipients

/**
 * use: <mailet match="HeaderContains=<header>-[value]" class="..." />
 *
**/
public class HeaderContains extends GenericMatcher {

    public Collection match(Mail mail) throws javax.mail.MessagingException {

        String[] condition = getCondition().split("-");
        String header = "";
        String value = "";

        if ( condition.length >2 )
        {
           header = condition[0] + "-" + condition[1];
           value = condition[2];
        }
        else
        {
           header = condition[0];
           value  = condition[1];
        }

        log( "HeaderContains " + "[" + header + "] = " + value +"?");
        if (mail.getMessage().getHeader(header)[0] == null )
        {
           log( "Header does not exist" );
           return(null);
        }
        else
        {
           if ( mail.getMessage().getHeader(header)[0].indexOf( value ) > -1 )
           {
              log( "[" + value + "] true" );
               return( mail.getRecipients() );
           }
        }
        log( "[" + value + "] false" );
        return (null);
    }
}

