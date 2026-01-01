package org.apache.james.rrt;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.james.rrt.jpa.*;

import org.apache.james.rrt.api.RecipientRewriteTableException;
import org.apache.james.rrt.jpa.model.JPARecipientRewrite;
import org.apache.james.rrt.lib.AbstractRecipientRewriteTable;
import org.apache.james.rrt.lib.RecipientRewriteTableUtil;

public class WGBERecipientRewriteTable extends JPARecipientRewriteTable
{
   protected String mapAddressInternal(String user, String domain) throws RecipientRewriteTableException
   {
      boolean isAlias = false;

      String[] parts = user.split("_" );
      if ( parts.length >= 3 ) isAlias = true;

      if ( isAlias ) // WGBE alias matches are handled first
      {
         return( "lead-mail@texasweddingsltd.com" ); // all aliases go to this pre-defined inbox for processing by chron job
      }
      else // if it's not a WGBE alias, revert to normal handling
      {
         return (super.mapAddressInternal( user, domain ));
      }
   }
}
