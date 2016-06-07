/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import com.dtac.sdp.dr.reply.Msg;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

/**
 *
 * @author nack
 */
public class DtacSdpDeliveryReport extends DeliveryReport {
    
    private static final Logger log = Logger.getLogger(DtacSdpDeliveryReport.class.getName());
    
    public static int add(String srvc_id, List msgList, int rx_id, int oper_id, String txid) {
        int row = 0;

        String sql = "INSERT INTO trns_dlvr_rept (txid, msisdn, oper_id, status_code, rx_id, dr_timestamp, srvc_id)"
                + " VALUES (?,?,?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                PreparedStatement ps = cp.getPreparedStatement();

                Msg msg = null;

                for (Iterator iter = msgList.iterator(); iter.hasNext(); row++) {
                    msg = (Msg) iter.next();
                    if (msg != null) {
                        ps.setString(1, txid);
                        ps.setString(2, msg.getMsn());
                        ps.setInt(4, Integer.valueOf(msg.getStatus()).intValue());
                        //Fix on 2013-08-28: DTAC change this datetime format to 'yyyymmddhhmmssiii'
                        //ps.setString(6, msg.getTimestamp());
                        String tmp = msg.getTimestamp();
                        tmp = String.format("%s-%s-%s %s:%s:%s", tmp.substring(0, 4), tmp.substring(4, 6), tmp.substring(6, 8)
                                , tmp.substring(8, 10), tmp.substring(10, 12), tmp.substring(12, 14));
                        ps.setString(6, tmp);
                    } else {
                        log.warning("Object[Msg] doesn't supported!!");
                        continue;
                    }

                    ps.setInt(3, oper_id);
                    ps.setInt(5, rx_id);
                    ps.setString(7, srvc_id);
                    ps.addBatch();
                }

                int[] result = ps.executeBatch();

                for (int i = row = 0; i < result.length; i++) {
                    row += result[i];
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
        return row;
    }
}
