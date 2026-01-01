package org.apache.james.transport.mailets;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWMToFoldersInbound extends JWMToFolder
{
    public void init() throws MessagingException {
        super.init();
        logger = LoggerFactory.getLogger(JWMToFoldersInbound.class);
    }

    public String getDefaultFolder()
    {
       return( "INBOX" );
    }

    public boolean getConsume()
    {
      return( true );
    }


    public String getMailetInfo()
    {
        return "JWMToFoldersInbound Mailet";
    }

    public int getTargetMailAcctBasis()
    {
       return( JWMToFolder.RECIPIENTS ); // determine target mail acct for folder storage based on RECIPIENTS for inbound
    }
}
