package hippoping.smsgw.api.db;

import com.dtac.cpa.dr.reply.Msg;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.TxQueue.TX_TYPE;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class DeliveryReport
        implements Serializable {

    private static final Logger log = Logger.getLogger(DeliveryReport.class.getName());
    protected String txid;
    protected String msisdn;
    protected int srvc_main_id;
    protected int oper_id;
    protected int status_code;
    protected String status_desc;
    protected String dr_timestamp;
    protected String last_mod_dt;
    protected String status_text;

    public static List<DeliveryReport> getDr(String txid, String msisdn)
            throws Exception {
        List list = new ArrayList();
        String sql = "SELECT *"
                + "   FROM trns_dlvr_rept"
                + "  WHERE txid=?"
                + "   AND msisdn=?"
                + " ORDER BY dr_timestamp;";

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);

            cp.getPreparedStatement().setString(1, txid);
            cp.getPreparedStatement().setString(2, msisdn);

            ResultSet rs = cp.execQueryPrepareStatement();
            while (rs.next()) {
                DeliveryReport dr = new DeliveryReport();

                dr.setTxid(txid);
                dr.setMsisdn(msisdn);
                dr.setSrvc_main_id(rs.getInt("srvc_main_id"));
                dr.setOper_id(rs.getInt("oper_id"));
                dr.setStatus_code(rs.getInt("status_code"));
                dr.setStatus_desc(rs.getString("status_desc"));
                dr.setDr_timestamp(rs.getString("dr_timestamp"));
                dr.setLast_mod_dt(rs.getString("last_mod_dt"));

                list.add(dr);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            log.severe(e.getMessage());
        } finally {
            cp.release();
        }

        return list;
    }

    public static List<DeliveryReport> getCdgDr(TxQueue txq) throws Exception {
        List list = new ArrayList();
        String sql;

        if (txq.piority == TX_TYPE.INTERACTIVE.getId()) {
            sql = "SELECT rc.LinkedID, q.msisdn"
                    + " , IF(dr.StatusCode IS NULL or dr.StatusCode = '', 0, dr.StatusCode)"
                    + " , dr.MMStatus, dr.StatusText, rc.timestamp, rc.recv_dt"
                    + "  FROM trns_tx_queue q"
                    + " INNER JOIN trns_rx r"
                    + "    ON r.rx_id = q.rx_id"
                    + " INNER JOIN trns_rx_cdg rc"
                    + "    ON rc.rx_cdg_id = r.rx_cdg_id"
                    + " INNER JOIN trns_dr_cdg dr"
                    + "    ON dr.LinkedID = rc.LinkedID"
                    + " WHERE q.tx_queue_id = ?";
        } else {
            sql = "SELECT q.txid, q.msisdn"
                    + " , IF(dr.StatusCode IS NULL or dr.StatusCode = '', 0, dr.StatusCode)"
                    + " , dr.MMStatus, dr.StatusText, dr.timestamp, dr.last_mod_dt"
                    + "  FROM trns_tx_queue q"
                    + " INNER JOIN trns_dr_cdg dr"
                    + "    ON dr.GMessageID = q.txid"
                    + "   AND dr.msisdn = q.msisdn"
                    + " WHERE q.tx_queue_id = ?";
        }

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);

            cp.getPreparedStatement().setLong(1, txq.tx_queue_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            while (rs.next()) {
                DeliveryReport dr = new DeliveryReport();

                dr.setTxid(rs.getString(1));
                dr.setMsisdn(rs.getString(2));
                dr.setSrvc_main_id(txq.srvc_main_id);
                dr.setOper_id(txq.oper_id);
                dr.setStatus_code(rs.getInt(3));
                dr.setStatus_desc(rs.getString(4));
                dr.setStatus_text(rs.getString(5));
                dr.setDr_timestamp(rs.getString(6));
                dr.setLast_mod_dt(rs.getString(7));

                list.add(dr);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }

        return list;
    }

    public static List<Hashtable> getCdgDr(Hashtable param) throws Exception {
        List list = new ArrayList();
        Set keys = param.keySet();
        String where = "";

        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = (String) param.get(key);
            where = where + " AND " + key + " = '" + value + "'";
        }
        String sql
                = "  SELECT dr_cdg_id"
                + "  FROM trns_dr_cdg"
                + "  WHERE 1"
                + where
                + " ORDER BY dr_cdg_id DESC"
                + " LIMIT 100";

        DBPoolManager cp = new DBPoolManager();
        try {
            ResultSet rs = cp.execQuery(sql);
            while (rs.next()) {
                Hashtable dr = AisCdgDrFactory.get(rs.getLong(1));
                if (dr != null) {
                    list.add(dr);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }

        return list;
    }

    public static List<DeliveryReport> get(TxQueue txq, String msisdn) throws Exception {
        List list = null;
        // DR record required MSISDN
        if (msisdn == null) {
            return list;
        }

        switch (CARRIER.fromId(txq.oper_id)) {
            case AIS: // CDG
                list = getCdgDr(txq);
                break;
            default:
                list = getDr(txq.txid, msisdn);
        }

        return list;
    }

    public static boolean isDuplicated(String txid, String msisdn) {
        boolean ret = false;
        String sql
                = "    SELECT *"
                + "    FROM trns_dlvr_rept"
                + "   WHERE txid=?"
                + "     AND msisdn=?;";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setString(1, txid);
                cp.getPreparedStatement().setString(2, msisdn);

                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    ret = true;
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public static int add(String txid, String msisdn, int status_code, String status_desc, String dr_timestamp, int oper_id) {
        return add(txid, msisdn, status_code, status_desc, dr_timestamp, oper_id, null);
    }

    public static int add(String txid, String msisdn, int status_code, String status_desc, String dr_timestamp, int oper_id, String srvc_id) {
        int row = 0;

        String sql = "INSERT INTO trns_dlvr_rept (txid, msisdn, oper_id,"
                + "        status_code, status_desc, dr_timestamp, srvc_id)"
                + " VALUES( ?, ?, ?, ?, ?, ?, ?);";

        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setString(1, txid);
                cp.getPreparedStatement().setString(2, msisdn);
                cp.getPreparedStatement().setInt(3, oper_id);
                cp.getPreparedStatement().setInt(4, status_code);
                cp.getPreparedStatement().setString(5, status_desc);
                cp.getPreparedStatement().setString(6, dr_timestamp);
                cp.getPreparedStatement().setString(7, srvc_id);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        /**
         * removed do pure insert DR only String sql = "INSERT INTO" + "
         * trns_dlvr_rept (txid, msisdn, srvc_main_id, oper_id," + "
         * status_code, status_desc, dr_timestamp)" + " SELECT ?, ?,
         * srvc_main_id, ?, ?, ?, ?" + " FROM trns_tx_queue" + " WHERE 1" + "
         * AND oper_id=?" + " AND MATCH(txid) AGAINST(?)" + " LIMIT 1"; try {
         * DBPoolManager cp = new DBPoolManager(); try {
         * cp.prepareStatement(sql);
         *
         * cp.getPreparedStatement().setString(1, txid);
         * cp.getPreparedStatement().setString(2, msisdn);
         * cp.getPreparedStatement().setInt(3, oper_id);
         * cp.getPreparedStatement().setInt(4, status_code);
         * cp.getPreparedStatement().setString(5, status_desc);
         * cp.getPreparedStatement().setString(6, dr_timestamp);
         * cp.getPreparedStatement().setInt(7, oper_id);
         * cp.getPreparedStatement().setString(8, txid);
         *
         * row = cp.execUpdatePrepareStatement();
         *
         * if (row == 0) { if (srvc_id != null) { sql = "INSERT INTO
         * trns_dlvr_rept" + " (txid, msisdn, srvc_main_id, oper_id,
         * status_code, dr_timestamp)" + " SELECT ?, ?, srvc_main_id, ?, ?, ?" +
         * " FROM srvc_sub" + " WHERE 1" + " AND ? IN (srvc_id,
         * srvc_id_non_chrg, srvc_id_mo_test, bcast_srvc_id)" + " AND oper_id="
         * + oper_id + " LIMIT 1";
         *
         * cp.prepareStatement(sql); cp.getPreparedStatement().setString(1,
         * txid); cp.getPreparedStatement().setString(2, msisdn);
         * cp.getPreparedStatement().setInt(3, oper_id);
         * cp.getPreparedStatement().setInt(4, status_code);
         * cp.getPreparedStatement().setString(5, dr_timestamp);
         * cp.getPreparedStatement().setString(6, srvc_id);
         *
         * row = cp.execUpdatePrepareStatement(); }
         *
         * if (row == 0) { sql = "INSERT INTO trns_dlvr_rept (txid, msisdn,
         * oper_id," + " status_code, status_desc, dr_timestamp)" + " VALUES( ?,
         * ?, ?, ?, ?, ?);";
         *
         * cp.prepareStatement(sql);
         *
         * cp.getPreparedStatement().setString(1, txid);
         * cp.getPreparedStatement().setString(2, msisdn);
         * cp.getPreparedStatement().setInt(3, oper_id);
         * cp.getPreparedStatement().setInt(4, status_code);
         * cp.getPreparedStatement().setString(5, status_desc);
         * cp.getPreparedStatement().setString(6, dr_timestamp);
         *
         * row = cp.execUpdatePrepareStatement(); } } } catch (SQLException e) {
         * log.log(Level.SEVERE, "SQL error!!", e); } finally { cp.release(); }
         * } catch (Exception e) { log.severe(e.getMessage()); } *
         */
        return row;
    }

    public static int add(String srvc_id, List msgList, long rx_id, int oper_id, String txid) {
        int row = 0;

        String sql = "INSERT INTO trns_dlvr_rept (txid, msisdn, oper_id, status_code, rx_id, dr_timestamp, srvc_id)"
                + " VALUES (?,?,?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                PreparedStatement ps = cp.getPreparedStatement();

                for (Iterator iter = msgList.iterator(); iter.hasNext(); row++) {
                    Msg msg = (Msg) iter.next();
                    if (msg != null) {
                        ps.setString(1, msg.getTransactionId());
                        ps.setString(2, msg.getMsn());
                        ps.setInt(4, Integer.valueOf(msg.getStatus()).intValue());
                        ps.setString(6, msg.getTimestamp());
                    } else {
                        log.warning("Object[Msg] doesn't supported!!");
                        continue;
                    }

                    ps.setInt(3, oper_id);
                    ps.setLong(5, rx_id);
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

    public static int updateStatus(String txid, String msisdn, int status_code, String status_desc, String dr_timestamp) {
        int row = 0;
        String sql = "UPDATE trns_dlvr_rept"
                + "    SET status_code=?"
                + "      , status_desc=?"
                + "      , dr_timestamp=?"
                + "  WHERE txid=?"
                + "    AND msisdn=?;";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, status_code);
                cp.getPreparedStatement().setString(2, status_desc);
                cp.getPreparedStatement().setString(3, dr_timestamp);
                cp.getPreparedStatement().setString(4, txid);
                cp.getPreparedStatement().setString(5, msisdn);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public boolean isChargeSuccess() {
        boolean success = false;

        switch (CARRIER.fromId(this.oper_id)) {
            case DTAC:
            case DTAC_SDP:
                if (this.status_code == 4) {
                    success = true;
                }
                break;
            case TRUE:
            case TRUEH:
            case AIS_LEGACY:
                if (this.status_code == 0) {
                    success = true;
                }
                break;
            case AIS:
                if (this.status_text.matches("(?i).*DELIVRD.*")) {
                    success = true;
                }
                break;
        }

        return success;
    }

    public String getLast_mod_dt() {
        return this.last_mod_dt;
    }

    public void setLast_mod_dt(String last_mod_dt) {
        this.last_mod_dt = last_mod_dt;
    }

    public String getDr_timestamp() {
        return this.dr_timestamp;
    }

    public void setDr_timestamp(String dr_timestamp) {
        this.dr_timestamp = dr_timestamp;
    }

    public String getStatus_desc() {
        return this.status_desc;
    }

    public void setStatus_desc(String status_desc) {
        this.status_desc = status_desc;
    }

    public String getStatus_text() {
        return this.status_text;
    }

    public void setStatus_text(String status_text) {
        this.status_text = status_text;
    }

    public int getStatus_code() {
        return this.status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public int getOper_id() {
        return this.oper_id;
    }

    public void setOper_id(int oper_id) {
        this.oper_id = oper_id;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public void setSrvc_main_id(int srvc_main_id) {
        this.srvc_main_id = srvc_main_id;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getTxid() {
        return this.txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }
}
