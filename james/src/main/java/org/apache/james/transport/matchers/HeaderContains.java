package org.apache.james.transport.matchers;
import java.util.Collection;

import org.apache.mailet.Mail;
import org.apache.james.core.MailAddress;
import org.apache.mailet.MatcherConfig;
import org.apache.mailet.base.GenericMatcher;
import org.slf4j.Logger;

public class HeaderContains extends GenericMatcher {
   
   private Logger logger;

    @Override
    public void init(MatcherConfig config) {
        // You can read matcher parameters here if desired
    }

    @Override
    public Collection<MailAddress> match(Mail mail) 
    {
        String[] condition = getCondition().split("-");
        String header;
        String value;

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

        try {
            logger.debug("HeaderContains [{}] = {}?", header, value);
            if (mail.getMessage().getHeader(header)[0] == null )
            {
               logger.debug("Header does not exist");
               return(null);
            }
            else
            {
               if ( mail.getMessage().getHeader(header)[0].contains( value ) )
               {
                  logger.debug("[{}] true", value);
                   return( mail.getRecipients() );
               }
            }
            logger.debug("[{}] false", value);
            return (null);
        } catch (Exception e) {
            logger.error("Error accessing message headers", e);
            return (null);
        }
    }
}

