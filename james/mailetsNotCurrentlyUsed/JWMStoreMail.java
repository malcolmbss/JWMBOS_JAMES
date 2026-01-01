package org.apache.james.transport.mailets;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.Flags;

import org.apache.james.core.MailAddress;
import org.apache.james.core.Username;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.transport.mailets.delivery.MailboxAppender;
import org.apache.james.transport.mailets.delivery.MailboxAppenderImpl;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.mailet.Experimental;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives a Mail from the Queue and takes care to deliver the message
 * to a defined folder of the sender.
 *
 * You have to define the folder name of the sender.
 * The flag 'consume' will tell is the mail will be further
 * processed by the upcoming processor mailets, or not.
 *
 * <pre>
 * &lt;mailet match="RecipientIsLocal" class="JWMStoreMail"&gt;
 *    &lt;folder&gt; <i>Sent Items</i> &lt;/folder&gt;
 *    &lt;consume&gt; <i>false</i> &lt;/consume&gt;
 * &lt;/mailet&gt;
 * </pre>
 *
 */
@Experimental
public class JWMStoreMail extends JWMGenericMailet
{
    private static final Logger logger = LoggerFactory.getLogger(JWMStoreMail.class);

    private final UsersRepository usersRepository;
    private final MailboxManager mailboxManager;
    private String tgtAcctHdrName = "";
    private String tgtFolderInitParm = "";
    private boolean consume;
    private MailboxAppender mailboxAppender;

    @Inject
    public JWMStoreMail(UsersRepository usersRepository, @Named("mailboxmanager") MailboxManager mailboxManager) {
        this.usersRepository = usersRepository;
        this.mailboxManager = mailboxManager;
    }

    @Override
    public void init() throws MessagingException {
        consume = getInitParameter("consume", false);
        mailboxAppender = new MailboxAppenderImpl(mailboxManager);
        tgtAcctHdrName = getInitParameter ("targetAcctHeaderName", "X_JWMH_TGTACCT"  );
        tgtFolderInitParm = getInitParameter ("targetFolder", "" );
    }

    /**
     * Delivers a mail to a local mailbox in a given folder.
     */
    @Override
    public void service(Mail mail) throws MessagingException {
        if (!mail.getState().equals(Mail.GHOST)) {
            doService(mail);
        }
    }

    private void doService(Mail mail) throws MessagingException
    {
       // this is code to debug userRepository problem... not functional inline code...
//     logger.info("UsersRepository instance: " + usersRepository.getClass().getName());
//     if (mail.hasSender()) {
//        MailAddress sender = mail.getMaybeSender().get();
//        String username = retrieveUser(sender);
//        logger.info("Sender{}; User name: {}", sender, username );
//     }
       // end of debug code

       logger.debug( "tgtAcct: "    + getHeader( mail, tgtAcctHdrName).toLowerCase() );
       logger.debug( "tgtFolder: "  + getHeader( mail, "X_JWMH_TGTFOLDER") );

       String[] tgtAcct    = getHeader( mail, tgtAcctHdrName).toLowerCase().split(";");
       String[] tgtFolder  = getHeader( mail, "X_JWMH_TGTFOLDER").split(";");
       if ( !tgtFolderInitParm.equals("") )
       {
          for ( int i = 0; i < tgtFolder.length; i++ )
          {
             tgtFolder[i] = tgtFolderInitParm; // mailet init parm overrides header (to allow logger to put everything in inbox...)
          }
       }

       String flag = getHeader( mail, "X_JWMH_FLAG" ); // would have been set in authOutbound processRules
       if ( flag.equals("$label3") )
       {
          try
          {
             mail.getMessage().setFlags(new Flags(flag), true);  // this shows email as 'green outbound'
             mail.getMessage().saveChanges();
          }
          catch( Exception e )
          {
             logger.error( "Exception setting $label3 outbound flag in message -- " + e + " " + mail.getName() );
          }
       }

       for ( int i = 0; i < tgtAcct.length; i++ )
       {
          logger.info( "Attempting StoreMail: ["+i+"] Folder: " + tgtFolder[i] + " -- " + tgtAcct[i] + " [Sender: " + getEmailAddress(mail, SENDER) + "] [Recipient: " + getEmailAddress(mail, RECIPIENTS)  + "] [Subj: " + mail.getMessage().getSubject() + "] " + mail.getName() );
          try
          {
             mailboxAppender.append(mail.getMessage(), retrieveUser(new MailAddress(tgtAcct[i])), tgtFolder[i]);
//           mailboxAppender.append(mail.getMessage(), tgtAcct[i], tgtFolder[i]);
             if (consume) {
                 mail.setState(Mail.GHOST); // only consume if successful save; pipeline should have another mailet in stream to try to store in deadletter if still active
             }
          }
          catch( MessagingException e )
          {
             logger.error( e.toString() + " -  Folder: " + tgtFolder[i] + " -- " + tgtAcct[i] + " [Sender: " + getEmailAddress(mail, SENDER) + "] [Recipient: " + getEmailAddress(mail, RECIPIENTS)  + "] [Subj: " + mail.getMessage().getSubject() + "] " + mail.getName());
             mail.getMessage().setHeader("X_JWMH_ERROR ["+i+"] ", e.toString() + " " + tgtAcct[i] + "/" + tgtFolder[i] );
             mail.getMessage().saveChanges();
          }
       }
    }

    @Override
    public String getMailetInfo() {
        return JWMStoreMail.class.getName() + " Mailet";
    }

    private Username retrieveUser(MailAddress sender) throws MessagingException {
        try {
            return usersRepository.getUsername(sender);
        } catch (UsersRepositoryException e) {
            throw new MessagingException(e.getMessage());
        }
    }

}
