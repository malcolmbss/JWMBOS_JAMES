package org.apache.james.transport.mailets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.james.core.MailAddress;
import org.apache.james.util.sql.JDBCUtil;
import org.apache.mailet.Mail;
import org.slf4j.LoggerFactory;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


public class RecognizedListManager extends JWMGenericMailet
{
    private DataSource datasource;

    private String action = "";

    private final String update = "UPDATE recognizedlist set lastUpdate=now() where remoteUser=? AND remoteHost=?"; // track how recent entry was used... for pruning of list
    private final String query  = "SELECT * FROM recognizedlist where remoteUser=? AND remoteHost=?";
    private final String insert = "INSERT INTO recognizedlist (localUser, localHost, remoteUser, remoteHost, lastUpdate, type) VALUES (?,?,?,?, now(), ?)";

    private final String checkSender  = "SELECT * FROM recognizedlist where ((remoteUser LIKE ? AND  remoteHost LIKE ?) OR (remoteUser='*' AND remoteHost LIKE ?)) AND TYPE = ?";

    private final static int RECOGNIZEDLISTTYPE_WHITELIST_SENDER  = 1;
   //  private final static int RECOGNIZEDLISTTYPE_WHITELIST_SUBJECT = 2;
    private final static int RECOGNIZEDLISTTYPE_BLACKLIST_SENDER  = 3;
   //  private final static int RECOGNIZEDLISTTYPE_BLACKLIST_SUBJECT = 4;
   //  private final static int RECOGNIZEDLISTTYPE_BLACKLIST_SENDERNANE = 5;
     private final static int RECOGNIZEDLISTTYPE_ACCEPTED_EXPIRING  = 6;


    public void setDataSource(DataSource datasource)
    {
        this.datasource = datasource;
    }

    private final JDBCUtil theJDBCUtil = new JDBCUtil();
   //  {
   //      protected void delegatedLog(String logString)
   //      {
   //          log("RecognizedListManager: " + logString);
   //      }
   //  };

    @Override public void init() throws MessagingException
    {
        logger = LoggerFactory.getLogger(RecognizedListManager.class);
        String repositoryPath = getInitParameter("repositoryPath");
        if (repositoryPath != null)
        {
            logger.debug("RecognizedListManager repositoryPath: " + repositoryPath);
        } else
        {
            throw new MessagingException("repositoryPath is null");
        }
        action = getInitParameter("action");
    }

    @Override public void service(Mail mail) throws MessagingException
    {
        // The RecognizedList table contains whitelist and blacklist entries....

        MimeMessage message = mail.getMessage();

        MailAddress senderMailAddress = mail.getSender();
        if (senderMailAddress == null) return;

        Collection<MailAddress> recipients = mail.getRecipients();

        String senderUser = (senderMailAddress.getLocalPart()).toLowerCase(Locale.US);
        String senderHost = (senderMailAddress.getDomain().name()).toLowerCase(Locale.US);

        Connection conn = null;

// ====== Add recipient to white list =============================================================

        if ( action.equals( "addToWhiteList" ))
        {
           PreparedStatement selectStmt = null;
           PreparedStatement updateStmt = null;
           PreparedStatement insertStmt = null;

           try
           {
               for (MailAddress recipient : recipients)
               {
                   ResultSet selectRS = null;
                   try
                   {
                       MailAddress recipientMailAddress = recipient;
                       String recipientUser = (recipientMailAddress.getLocalPart()).toLowerCase(Locale.US);
                       String recipientHost = (recipientMailAddress.getDomain().name()).toLowerCase(Locale.US);

                       if (conn == null) conn = datasource.getConnection();


                       if (selectStmt == null) selectStmt = conn.prepareStatement(query);

                       selectStmt.setString(1, recipientUser);
                       selectStmt.setString(2, recipientHost);
                       selectRS = selectStmt.executeQuery();

                       if (selectRS.next()) // This address was already in the list
                       {

                          if (updateStmt == null) updateStmt = conn.prepareStatement(update);
                          updateStmt.setString(1, recipientUser);
                          updateStmt.setString(2, recipientHost);
                          updateStmt.executeUpdate();
                          logger.debug("RecognizedListManager: whiteList entry exists, lastUpdate=now(): " + recipientUser + " " + recipientHost);

                       }
                       else
                       {

                          if (insertStmt == null) insertStmt = conn.prepareStatement(insert);

                          insertStmt.setString(1, senderUser); // sender not used in matcher... but nice to know for pruning purposes
                          insertStmt.setString(2, senderHost);
                          insertStmt.setString(3, recipientUser);
                          insertStmt.setString(4, recipientHost);
                          insertStmt.setString(5, String.valueOf(RECOGNIZEDLISTTYPE_WHITELIST_SENDER));
                          insertStmt.executeUpdate();
                          logger.debug("RecognizedListManager: whiteList new entry: " + recipientUser + " " + recipientHost);
                       }
                   }
                   finally
                   {
                       theJDBCUtil.closeJDBCResultSet(selectRS);
                   }

                   // Commit our changes if necessary.
                   if (conn != null && !conn.getAutoCommit())
                   {
                       conn.commit();
                   }
               }
           }
           catch (SQLException sqle)
           {
               logger.debug("Error accessing database", sqle);
               throw new MessagingException("Exception thrown", sqle);
           }
           finally
           {
               theJDBCUtil.closeJDBCStatement(selectStmt);
               theJDBCUtil.closeJDBCStatement(updateStmt);
               theJDBCUtil.closeJDBCStatement(insertStmt);
               theJDBCUtil.closeJDBCConnection(conn);
           }
        }

// ====== Check inbound mail to determine if recognized ===========================================

        boolean recognized = false;

        if ( action.equals( "checkRecognized" ))
        {
           // 2/25/15 -- free pass for apache forums....
           String apacheHeader = message.getHeader( "Delivered-To", null );
           if (( apacheHeader != null )
            && ( apacheHeader.contains("apache.org") ))
           {
                 message.setHeader("X-WhiteListSenderMatch", "true"  );
                 recognized = true;
                 logger.debug("RecognizedListManager: apache forum" );
           }

           // 7/10/10 - JWM - sender address is sometimes cryptic string; use from address where possible
           try
           {
              Address[] fromAddresses = mail.getMessage().getFrom();
              InternetAddress fromAddress = (InternetAddress) fromAddresses[0];
              String[] fromAddressParts = fromAddress.getAddress().split("@");
              senderUser = fromAddressParts[0];
              senderHost = fromAddressParts[1];

              message.setHeader("X-SenderEqualsRecipientCount", String.valueOf(recipients.size()));
              // 5/9/16 check for sender=recipient... huge rash of spam that is bypassing spam filter due to white listing 'sender' (local email addresses)
              if ( recipients.size() == 1 )
              {
                 try
                 {
                    for (MailAddress recipient : recipients)
                    {
                      MailAddress recipientMailAddress = recipient;
                      String recipientUser = (recipientMailAddress.getLocalPart()).toLowerCase(Locale.US);
                      String recipientHost = (recipientMailAddress.getDomain().name()).toLowerCase(Locale.US);

                      message.setHeader("X-SenderEqualsRecipientTest", recipientUser+"@"+recipientHost + " " + senderUser+"@"+senderHost );
                      if ( ( senderUser.toUpperCase().equals( recipientUser.toUpperCase() ) )
                         &&( senderHost.toUpperCase().equals( recipientHost.toUpperCase() ) ) )
                      {
                         message.setHeader("X-RecognizedListSpamFlag", "true"  );
                         message.setHeader("X-SenderEqualsRecipient", "YES"  );
                         logger.debug("RecognizedListManager: Sender=Recipient (Assume Spam)" );
                        //  recognized = true;
                         return;
                      }
                      else
                      {
                        logger.debug( "RecognizedListManager: sender ?==? recipient : false");
                        logger.debug( "["+senderUser.toUpperCase()+"] [" + recipientUser.toUpperCase()+"]");
                        logger.debug( "["+senderHost.toUpperCase()+"] [" + recipientHost.toUpperCase()+"]");
                      }
                    }
                 }
                 catch (MessagingException | RuntimeException e)
                 {
                    logger.debug("RecognizedListManager: Sender=Recipient (Assume Spam Test) " + e);
                    recognized = true;
                 }
              }
           }
           catch (MessagingException | RuntimeException e)
           {
              // fall back to sender addr
           }

           PreparedStatement checkStmt = null;
           ResultSet selectRS;
           try
           {
              if (conn == null) conn = datasource.getConnection();

              // check against whitelist_sender

              if (checkStmt == null) checkStmt = conn.prepareStatement(checkSender);

              checkStmt.setString(1, senderUser);
              checkStmt.setString(2, senderHost);
              checkStmt.setString(3, senderHost);
              checkStmt.setString(4, String.valueOf(RECOGNIZEDLISTTYPE_WHITELIST_SENDER) );
              logger.debug( checkStmt.toString() );
              selectRS = checkStmt.executeQuery();

              if (selectRS.next())
              {
                 message.setHeader("X-Recognized", "["+selectRS.getString("remoteUser")+"] [" + selectRS.getString( "remoteHost" )+"]" );
                 message.setHeader("X-WhiteListSenderMatch", "true"  );
                 logger.debug("RecognizedListManager: whiteList sender match: " + senderUser + " " + senderHost);
              }

              // check against blacklist_sender

            //   if (checkStmt == null) checkStmt = conn.prepareStatement(checkSender);

              checkStmt.setString(1, senderUser);
              checkStmt.setString(2, senderHost);
              checkStmt.setString(3, senderHost);
              checkStmt.setString(4, String.valueOf(RECOGNIZEDLISTTYPE_BLACKLIST_SENDER) );
              logger.debug( checkStmt.toString() );
              selectRS = checkStmt.executeQuery();

              if (selectRS.next())
              {
                 message.setHeader("X-BlackListSenderMatch", "true"  );
                 message.setHeader("X-RecognizedListSpamFlag", "true"  );
                 logger.debug("RecognizedListManager: blackList sender match: " + senderUser + " " + senderHost);
                 recognized = true;
              }

              // check against blacklist_sender (using TLD only this time as senderHost

              // Look in RecognizedList table.... senderHost spec can be  *.<tld>   or   *.*.<tld>
              // first check for *.<tld>

              checkStmt = conn.prepareStatement(checkSender);

              String[] senderHostParts = senderHost.split("\\.");
              if ( senderHostParts.length == 2 )
              {
                 String senderHostTLD = "----";
                 try
                 {
                    senderHostTLD = "*." + senderHostParts[ senderHostParts.length-1 ];
                 }
                 catch( Exception e)
                 {
                 }

                 checkStmt.setString(1, senderUser);
                 checkStmt.setString(2, senderHost);
                 checkStmt.setString(3, senderHostTLD);
                 checkStmt.setString(4, String.valueOf(RECOGNIZEDLISTTYPE_BLACKLIST_SENDER) );
                 logger.debug( "TLD " + checkStmt.toString() );
                 selectRS = checkStmt.executeQuery();

                 if (selectRS.next())
                 {
                    message.setHeader("X-BlackListSenderMatch", "true"  );
                    message.setHeader("X-RecognizedListSpamFlag", "true"  );
                    logger.debug("RecognizedListManager: blackList sender TLD match: " + senderUser + " " + senderHost);
                    recognized = true;
                 }
              }

              // check against blacklist_sender (using TLD only this time as senderHost
              // now check for *.*.<tld>

            //   if (checkStmt == null) checkStmt = conn.prepareStatement(checkSender);

              senderHostParts = senderHost.split("\\.");
              if ( senderHostParts.length > 2 )   // current rash of uncaught spam is @*.*.<tld>
              {
                 String senderHostTLD = "----";
                 try
                 {
                    senderHostTLD = "*.*." + senderHostParts[ senderHostParts.length-1 ];
                 }
                 catch( Exception e)
                 {
                 }

                 checkStmt.setString(1, senderUser);
                 checkStmt.setString(2, senderHost);
                 checkStmt.setString(3, senderHostTLD);
                 checkStmt.setString(4, String.valueOf(RECOGNIZEDLISTTYPE_BLACKLIST_SENDER) );
                 logger.debug( "TLD " + checkStmt.toString() );
                 selectRS = checkStmt.executeQuery();

                 if (selectRS.next())
                 {
                    message.setHeader("X-BlackListSenderMatch", "true"  );
                    message.setHeader("X-RecognizedListSpamFlag", "true"  );
                    logger.debug("RecognizedListManager: blackList sender TLD match: " + senderUser + " " + senderHost);
                    recognized = true;
                 }
              }

              theJDBCUtil.closeJDBCResultSet(selectRS);

           }
           catch (SQLException sqle)
           {
               logger.debug("Error accessing database", sqle);
               throw new MessagingException("Exception thrown", sqle);
           }
           finally
           {
               theJDBCUtil.closeJDBCStatement(checkStmt);
               theJDBCUtil.closeJDBCConnection(conn);
           }
//           message.setHeader("X-Recognized", ""+recognized  );
           // split into different headers until I can figure out why HeaderContains matcher isn't working
           if ( recognized )
           {
              message.setHeader("X-Recognized", "recognized"  );
           }
           else
           {
              message.setHeader("X-Not-Recognized", "not-recognized"  );
           }
        }
    }

     @Override public String getMailetInfo()
     {
         return "RecognizedListManager mailet";
     }

 }
