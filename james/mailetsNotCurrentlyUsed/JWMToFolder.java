package org.apache.james.transport.mailets;
import org.apache.james.core.MailAddress;
import org.apache.mailet.Mail;

import jakarta.mail.MessagingException;

public abstract class JWMToFolder extends JWMGenericMailet
{

    // private DataSource datasource;
    // private org.apache.james.rrt.api.RecipientRewriteTable rrt;

    // @Inject
    // @Resource(name = "usersrepository")
    // private UsersRepository usersRepository;

    // @Inject
    // public void setRrt(@Named("recipientrewritetable") org.apache.james.rrt.api.RecipientRewriteTable rrt)
    // {
    //     this.rrt = rrt;
    // }


    // public void init() throws MessagingException {
    //     super.init();
    //     logger = LoggerFactory.getLogger(JWMToFolder.class);
    // }

    // public void setDataSource(DataSource datasource)
    // {
    //     this.datasource = datasource;
    // }

    @Override
    public void service(Mail mail) throws MessagingException
    {
        // BufferLog jwmbosLogger = null;
        // BufferLogExceptionHandler exceptionHandler = null;
        // DBData ruleDbData = null;
        // try
        // {
        //    jwmbosLogger = new BufferLog(Log.DEBUG);
        //    exceptionHandler = new BufferLogExceptionHandler( jwmbosLogger );
        //    ruleDbData = new DBData( datasource, jwmbosLogger, exceptionHandler );
        //    String targetMailAcctHdr = "";
        //    String folderHdr = "";
        //    for ( int i = 0; i < 3; i++ ) // get sender or each recipient email address
        //    {
        //       MailAddress emailAddress = getEmailAddress(mail, getTargetMailAcctBasis(), i );
        //       if ( emailAddress == null ) break; // done...
        //       logger.trace( "emailAddress from basis " + emailAddress + " " + getTargetMailAcctBasis() );
        //       String targetMailAcct = getTargetMailAcct( emailAddress );
        //       targetMailAcctHdr += targetMailAcct+";";
        //       folderHdr         += getFolder( ruleDbData, mail, targetMailAcct )+";";
        //    }

        //    try
        //    {
        //        MimeMessage message = mail.getMessage () ;
        //        message.setHeader("X_JWMH_TGTACCT", targetMailAcctHdr);
        //        message.setHeader("X_JWMH_TGTFOLDER", folderHdr);
        //        message.saveChanges();
        //    }
        //    catch (jakarta.mail.MessagingException me)
        //    {
        //        log (me.getMessage());
        //    }
        //    logger.debug( "\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> JWMProcessRules JWMBOS logs\n" + jwmbosLogger.getLogData() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" );
        // }
        // catch( Exception e)
        // {
        //   logger.error( getMailetInfo() + " Exception " + e.toString() );
        // }
        // finally
        // {
        //    try
        //    {
        //       ruleDbData.close();
        //    }
        //    catch( Exception e )
        //    {
        //       System.out.println( e );
        //    }
        // }
    }

    // public String getFolder(DBData ruleDbData, Mail mail, String targetMailAcct ) throws MessagingException
    // {
    //    try
    //    {
    //       logger.debug( ">> Bgn processing targetFolder rules for: " + targetMailAcct + " [Sender: " + getEmailAddress(mail, SENDER) + "] [Recipient: " + getEmailAddress(mail, RECIPIENTS) + "] [Subj: " + mail.getMessage().getSubject() + "]");
    //       RuleList ruleList = new RuleList( ruleDbData, ObjectClassID.MAILACCT, targetMailAcct, Rule.TYPE_MAIL_TARGETFOLDER );

    //       String[] jwmbosLoggerStrings = ruleList.processRules(mail, true);  // if rule hit, header will be added with target folder name

    //       showJWMBOSLog( jwmbosLoggerStrings );

    //       String folder = mail.getMessage().getHeader( "X_JWMH_TGTFOLDER", null );
    //       logger.debug( "header " + folder );
    //       if (( folder == null ) || (folder.equals(""))) folder = getDefaultFolder();

    //       logger.debug( "<< End processing targetFolder rules for: " + targetMailAcct + " -- folder: " + folder + " " + mail.getName() );
    //       logger.info( "Folder: " + folder + " -- " + targetMailAcct + " [Sender: " + getEmailAddress(mail, SENDER) + "] [Recipient: " + getEmailAddress(mail, RECIPIENTS)  + "] [Subj: " + mail.getMessage().getSubject() + "]");
    //       return( folder );
    //    }
    //    catch( Exception e)
    //    {
    //      logger.error( getMailetInfo() + " Exception " + e.toString() );
    //    }
    //    return( null );
    // }

    protected String[] addrsToStrings( MailAddress[] addrs )
    {
       String[] addrStrings = new String[ 0 ];
       if ( addrs != null )
       {
          addrStrings = new String[ addrs.length ];
          for ( int i = 0; i < addrs.length; i++ )
          {
             try
             {
                addrStrings[i] = addrs[i].toString();
             }
             catch( Exception e)
             {
                addrStrings[i] = "---"; // apparently, some elements of array are null (??).  Just handle and move on
             }
          }
       }
       return( addrStrings );
     }

    // public String getTargetMailAcct( MailAddress emailAddress ) throws MessagingException
    // {
        // String rc =  "----";
        // if (emailAddress == null )
        // {
        //    logger.error("getTargetMailAcct() -- emailAddress is null");
        //    return( rc );
        // }

        // try
        // {
        //     Mappings mappings = rrt.getResolvedMappings(emailAddress.getLocalPart(), emailAddress.getDomain());

        //     if (mappings != null )
        //     {
        //         Iterator<Mapping> i = mappings.iterator();
        //         while (i.hasNext())
        //         {
        //             rc = i.next().asString();
        //             logger.debug( "rc " + rc );
        //         }
        //     }
        //     if (rc.equals( "----" )) // no hits on the mappings.... revert to using sender email as targetMailAcct
        //     {
        //        logger.trace( "no hits on mapping" );
        //        if (usersRepository.supportVirtualHosting())
        //        {
        //            rc = emailAddress.toString();
        //        }
        //        else
        //        {
        //            rc = emailAddress.getLocalPart();
        //        }
        //        logger.trace( "using: " + rc );
        //     }
        // }

        // catch (ErrorMappingException e)
        // {
        //     String errorBuffer = getMailetInfo() + " - A problem has occurred trying to map email address to repository " + emailAddress + ": " + e.getMessage();
        //     throw new MessagingException(errorBuffer);
        // }
        // catch (RecipientRewriteTableException e)
        // {
        //     throw new MessagingException("Unable to access RecipientRewriteTable", e);
        // }
        // catch (UsersRepositoryException e)
        // {
        //     throw new MessagingException(e.getMessage());
        // }
        // return( rc );
    }

    // public abstract String  getDefaultFolder();
    // public abstract boolean getConsume();
    // public abstract int     getTargetMailAcctBasis();
