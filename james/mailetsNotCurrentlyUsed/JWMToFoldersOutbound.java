package org.apache.james.transport.mailets;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWMToFoldersOutbound extends JWMToFolder
{
    public void init() throws MessagingException {
        super.init();
        logger = LoggerFactory.getLogger(JWMToFoldersOutbound.class);
    }

    public String getDefaultFolder()
    {
       return( "Sent" );
    }

    public boolean getConsume()
    {
      return( false );
    }

    public String getMailetInfo()
    {
        return "JWMToFoldersOutbound Mailet";
    }

    public int getTargetMailAcctBasis()
    {
       return( JWMToFolder.SENDER ); // determine target mail acct for folder storage based on SENDER for outbound
    }
}
