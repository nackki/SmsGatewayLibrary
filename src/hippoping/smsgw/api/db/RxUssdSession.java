/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

/**
 *
 * @author nacks_mcair
 */
public class RxUssdSession {

    private static final Logger log = Logger.getLogger(RxUssdSession.class.getClass().getName());
    protected String tran_no;
    protected Timestamp create_dt;
    protected long rx_id;
    protected String msisdn;
    protected String content;

    public static int add(String tran_no, long rx_id, String msisdn, String content) {
        int row = 0;

        String sql = "INSERT INTO trns_rx_ussd_sess (tran_no, rx_id, msisdn, content) VALUES (?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setString(1, tran_no);
                cp.getPreparedStatement().setLong(2, rx_id);
                cp.getPreparedStatement().setString(3, msisdn);
                cp.getPreparedStatement().setString(4, content);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, e.getMessage());
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }
}
