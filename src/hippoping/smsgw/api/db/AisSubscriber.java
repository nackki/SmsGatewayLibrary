/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

/**
 *
 * @author nack
 */
public class AisSubscriber extends Subscriber implements Comparable<Subscriber>, Serializable {

    private static final Logger log = Logger.getLogger(AisSubscriber.class.getName());
    protected String recipient;
    protected String linkedID;

    public AisSubscriber(String msisdn, int srvc_main_id, CARRIER oper) throws Exception {
        super(msisdn, srvc_main_id, oper);

        DBPoolManager cp = new DBPoolManager();

        String sql =
                "SELECT recipient, linkedID"
                + "  FROM mmbr_ais_custid"
                + " WHERE srvc_main_id=?"
                + "   AND msisdn=?";
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, srvc_main_id);
            cp.getPreparedStatement().setString(2, msisdn);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.recipient = rs.getString(1);
                this.linkedID = rs.getString(2);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Subscriber [{0},{1}, srvc_main_id:{2}] not found!!", new Object[]{msisdn, oper.name(), srvc_main_id});

            throw e;
        } finally {
            cp.release();
        }
    }

    public static Hashtable getExtraParam(String msisdn, int srvc_main_id) throws Exception {
        Hashtable params = new Hashtable();
        DBPoolManager cp = new DBPoolManager();

        String sql =
                "SELECT recipient, linkedID"
                + "  FROM mmbr_ais_custid"
                + " WHERE srvc_main_id=?"
                + "   AND msisdn=?";
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, srvc_main_id);
            cp.getPreparedStatement().setString(2, msisdn);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                params.put("recipient", rs.getString(1));
                params.put("linkedID", rs.getString(2));
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Subscriber [{0}, srvc_main_id:{1}] not found!!", new Object[]{msisdn, srvc_main_id});

            throw e;
        } finally {
            cp.release();
        }
        
        return params;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getLinkedID() {
        return linkedID;
    }
}
