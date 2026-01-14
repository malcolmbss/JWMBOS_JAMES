package org.apache.james.transport.mailets;

import javax.sql.DataSource;

import org.apache.mailet.Mail;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;

public class JWMProcessRules extends JWMGenericMailet
{
    // public static int SENDER = 1;
    // public static int RECIPIENTS = 2;

    // private DataSource datasource;

    // private org.apache.james.rrt.api.RecipientRewriteTable rrt;

    // @Inject
    // @Resource(name = "usersrepository")
    // private UsersRepository usersRepository;

    // @Inject
    // public void setRrt( org.apache.james.rrt.api.RecipientRewriteTable rrt)
    // {
    //     this.rrt = rrt;
    // }

    private int ruleType = -1;

    public void init() throws MessagingException
    {
       logger = LoggerFactory.getLogger(JWMProcessRules.class);
       ruleType = (new Integer(getInitParameter("ruleType")).intValue());
    }

    public void setDataSource(DataSource datasource)
    {
        // this.datasource = datasource;
        // logger.debug( "ProcessRules.setDataSource(): " + datasource );
    }

    public void service(Mail mail) throws MessagingException
    {
    //    logger.debug( "Enter: " + getMailetInfo() + " " + ruleType );
    //    DBData ruleDbData = null;
    //    BufferLog jwmbosLogger = null;
    //    try
    //    {
    //       jwmbosLogger = new BufferLog(Log.DEBUG);
    //       BufferLogExceptionHandler exceptionHandler = new BufferLogExceptionHandler( jwmbosLogger );
    //       ruleDbData = new DBData( datasource, jwmbosLogger, exceptionHandler );

    //       MailAddress[] emailAddresses = getEmailAddresses(mail );
    //       RuleList ruleList = new RuleList( ruleDbData, ObjectClassID.MAILACCT, getTargetMailAcct(emailAddresses[0]), ruleType );

    //       showJWMBOSLog( ruleList.processRules(mail, false));

    //    }
    //    catch( Exception e)
    //    {
    //       logger.debug( "Exception: " + e );
    //    }
    //    finally
    //    {
    //       logger.debug( "Finally: " + getMailetInfo() + " " + ruleType );
    //       try
    //       {
    //          if (ruleDbData != null) ruleDbData.close();
    //       }
    //       catch( Exception e )
    //       {
    //          System.out.println( e );
    //       }
    //    }
    //    logger.debug( "Exit: " + getMailetInfo() + " " + ruleType );
    //    logger.debug( "\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> JWMProcessRules JWMBOS logs\n" + jwmbosLogger.getLogData() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" );
    // }

    // public String getMailetInfo()
    // {
    //     return "JWMProcessRules";
    // }

    // public String getTargetMailAcct( MailAddress emailAddress ) throws MessagingException
    // {
    //     String rc =  "----";
    //     if (emailAddress == null )
    //     {
    //        logger.debug("getTargetEmailAcct() -- emailAddress is null");
    //        return( rc );
    //     }

    //     try
    //     {
    //         Mappings mappings = rrt.getResolvedMappings(emailAddress.getLocalPart(), emailAddress.getDomain());

    //         if (mappings != null)
    //         {
    //             Iterator<Mapping> i = mappings.iterator();
    //             while (i.hasNext())
    //             {
    //                 rc = i.next().asString();
    //             }
    //         }
    //         if (rc.equals( "----" )) // no hits on the mappings.... revert to using sender email as targetMailAcct
    //         {
    //            if (usersRepository.supportVirtualHosting())
    //            {
    //                rc = emailAddress.toString();
    //            }
    //            else
    //            {
    //                rc = emailAddress.getLocalPart();
    //            }
    //         }
    //     }

    //     catch (ErrorMappingException e)
    //     {
    //         String errorBuffer = getMailetInfo() + " - A problem has occoured trying to map email address to repository " + emailAddress + ": " + e.getMessage();
    //         throw new MessagingException(errorBuffer);
    //     }
    //     catch (RecipientRewriteTableException e)
    //     {
    //         throw new MessagingException("Unable to access RecipientRewriteTable", e);
    //     }
    //     catch (UsersRepositoryException e)
    //     {
    //         throw new MessagingException(e.getMessage());
    //     }

    //     logger.debug("getTargetEmailAcct() -- " + rc);
    //     return( rc );
    // }

    // public MailAddress[] getEmailAddresses( Mail mail ) throws MessagingException
    // {
    //     MailAddress[] addrs;
    //     // 9/27/19 -- if there's outbound AND inbound, it probably came from another account on my server
    //     //            therefore inbound header takes priority over outbound header

    //     logger.debug( "X_JWMH_OUTBOUND = " + mail.getMessage().getHeader( "X_JWMH_OUTBOUND", null ) );
    //     logger.debug( "X_JWMH_INBOUND  = " + mail.getMessage().getHeader( "X_JWMH_INBOUND", null ) );

    //     if ((mail.getMessage().getHeader( "X_JWMH_INBOUND" ) != null )
    //       && (!mail.getMessage().getHeader( "X_JWMH_INBOUND", null ).equals("false") )
    //       && (!mail.getMessage().getHeader( "X_JWMH_INBOUND", null ).equals("") ) )
    //     {
    //        //inbound... get recipient address
    //        logger.debug( "Process Rules based on recipient" );
    //        Collection<MailAddress> recipients = mail.getRecipients();
    //        addrs =  recipients.toArray(new MailAddress[0]);
    //     }
    //     else
    //     {
    //        //outbound... get sender address
    //        logger.debug( "Process Rules based on sender" );
    //        addrs = new MailAddress[1];
    //        addrs[0] = mail.getSender();
    //     }
    //     return( addrs );
    }
}
