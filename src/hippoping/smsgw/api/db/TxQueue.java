package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.mt.exsql.PushMTExtendSql;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class TxQueue implements Serializable {

    private static final Logger log = Logger.getLogger(TxQueue.class.getClass().getName());
    protected long tx_queue_id;
    public int srvc_main_id;
    public int oper_id;
    public String msisdn;
    public int content_id;
    public ServiceContentAction.ACTION_TYPE content_type;
    public String deliver_dt;
    protected TX_STATUS status;
    public String ivr_resp_id;
    public int status_code;
    public String status_desc;
    public String chrg_flg;
    public int dr_flg = 0;
    public int piority = 0;
    public String txid;
    public long rx_id;

    public TxQueue() {
    }

    public TxQueue(long tx_queue_id)
            throws Exception {
        this.tx_queue_id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT * FROM trns_tx_queue"
                    + "  WHERE tx_queue_id=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, tx_queue_id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.tx_queue_id = tx_queue_id;
                this.srvc_main_id = rs.getInt("srvc_main_id");
                this.oper_id = rs.getInt("oper_id");
                this.msisdn = rs.getString("msisdn");
                this.content_id = rs.getInt("ctnt_id");
                this.content_type = ServiceContentAction.ACTION_TYPE.values()[rs.getInt("ctnt_type")];
                this.deliver_dt = rs.getString("deliver_dt");
                this.status = TX_STATUS.values()[rs.getInt("status")];
                this.ivr_resp_id = rs.getString("ivr_resp_id");
                this.status_code = rs.getInt("status_code");
                this.status_desc = rs.getString("status_desc");
                this.chrg_flg = rs.getString("chrg_flg");
                this.dr_flg = rs.getInt("dr_flg");
                this.piority = rs.getInt("piority");
                this.txid = rs.getString("txid");
                this.rx_id = rs.getLong("rx_id");
            } else {
                throw new Exception("tx queue not found!!");
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public static TxQueue getTxQueueByRxid(String msisdn, int srvc_main_id, int oper_id, long rx_id) {
        TxQueue txq = null;
        try {
            DBPoolManager cp = new DBPoolManager();

            String whereoper = "";
            if (oper_id > 0) {
                whereoper = " AND q.oper_id=" + oper_id;
            }

            String whereservice = "";
            if (srvc_main_id > 0) {
                whereservice = " AND q.srvc_main_id=" + srvc_main_id;
            }

            String whererxid = "";
            if (rx_id > 0) {
                whererxid = " AND q.rx_id=" + rx_id;
            }

            String sql = "SELECT q.tx_queue_id"
                    + "  FROM trns_tx_queue q"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON ss.srvc_main_id = q.srvc_main_id"
                    + "   AND ss.oper_id = q.oper_id"
                    + " WHERE 1"
                    + "   AND q.msisdn" + (msisdn != null ? "='" + msisdn + "'" : " IS NULL")
                    + whereoper
                    + whereservice
                    + whererxid;
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    txq = new TxQueue(rs.getLong(1));
                } else {
                    throw new Exception("tx queue not found!!");
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }

        return txq;
    }

    public static TxQueue getTxQueueByTxid(String msisdn, int oper_id, String txid)
            throws Exception {
        TxQueue txq = null;
        try {
            DBPoolManager cp = new DBPoolManager();

            String whereoper = "";
            if (oper_id > 0) {
                whereoper = " AND oper_id=" + oper_id;
            }

            String wheretxid = "";
            if (txid != null) {
                wheretxid = " AND MATCH(txid) AGAINST ('" + txid + "')";
            }

            String wheremsisdn = "";
            if (msisdn == null) {
                wheremsisdn = " AND msisdn IS NULL";
            } else if (!msisdn.isEmpty()) {
                wheremsisdn = " AND msisdn='" + msisdn + "'";
            }

            String sql = "  SELECT tx_queue_id"
                    + "  FROM trns_tx_queue"
                    + " WHERE 1"
                    + wheremsisdn
                    + whereoper
                    + wheretxid;
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    txq = new TxQueue(rs.getLong(1));
                } else {
                    //throw new Exception("tx queue not found[" + sql + "]!!");
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return txq;
    }

    public static TxQueue getTxQueueByTxid(int oper_id, String txid)
            throws Exception {
        return getTxQueueByTxid("", oper_id, txid);
    }

    @Override
    public int hashCode() {
        return ((int) (tx_queue_id >>> 32)) ^ ((int) tx_queue_id);
        //return Long.valueOf(tx_queue_id).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof TxQueue)) {
            TxQueue txq = (TxQueue) obj;

            return this.tx_queue_id == txq.tx_queue_id;
        }
        return false;
    }

    public long getTx_queue_id() {
        return this.tx_queue_id;
    }

    public void setTx_queue_id(long tx_queue_id) {
        this.tx_queue_id = tx_queue_id;
    }

    public TX_STATUS getStatus() {
        return this.status;
    }

    public int setStatus(TX_STATUS status) {
        int istatus = status.getId();
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_tx_queue"
                        + "  SET status = ?"
                        + " WHERE tx_queue_id = ?";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, istatus);
                cp.getPreparedStatement().setLong(2, this.tx_queue_id);
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

    public int setTxid(String txid) {
        this.txid = txid;
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql
                        = "  UPDATE trns_tx_queue"
                        + "   SET txid = ?"
                        + " WHERE tx_queue_id = ?";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setString(1, txid);
                cp.getPreparedStatement().setLong(2, this.tx_queue_id);
                row = cp.execUpdatePrepareStatement();
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

    public int cancel() {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql
                        = "  UPDATE trns_tx_queue"
                        + "   SET status = ?"
                        + " WHERE tx_queue_id = ?"
                        + "   AND ( status=" + TX_STATUS.QUEUE.getId() + " OR status=" + TX_STATUS.ERROR.getId() + ")";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, TX_STATUS.CANCEL.getId());
                cp.getPreparedStatement().setLong(2, this.tx_queue_id);
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

    public int updateResponse(String txid, int status_code, String status_desc) {
        int istatus;
        int row = 0;

        // Fix 2015-01-13, always set status=2(SENT)
        istatus = TX_STATUS.SENT.getId();
//        if (status_code == 200) {
//            istatus = TX_STATUS.SENT.getId();
//        } else {
//            istatus = TX_STATUS.ERROR.getId();
//        }
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_tx_queue"
                        + "   SET status = ?"
                        + "     , txid = ?"
                        + "     , status_code = ?"
                        + "     , status_desc = ?"
                        + " WHERE tx_queue_id = ?";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, istatus);
                cp.getPreparedStatement().setString(2, txid);
                cp.getPreparedStatement().setInt(3, status_code);
                cp.getPreparedStatement().setString(4, status_desc);
                cp.getPreparedStatement().setLong(5, getTx_queue_id());
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

    public int updateResponse(String txid) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql
                        = "  UPDATE trns_tx_queue"
                        + "    SET txid = ?"
                        + " WHERE tx_queue_id = ?";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setString(1, txid);
                cp.getPreparedStatement().setLong(2, getTx_queue_id());
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

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, null, "NOW()", 0, TX_TYPE.INTERACTIVE.getId(), rx_id);
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, ivr_resp_id, "NOW()", 0, TX_TYPE.INTERACTIVE.getId(), rx_id);
    }

    // support DTAC_SDP reference txid
    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, int dr_flg, long rx_id, String txid) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, null, "NOW()", dr_flg, TX_TYPE.INTERACTIVE.getId(), rx_id, txid);
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, int dr_flg, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, null, "NOW()", dr_flg, TX_TYPE.INTERACTIVE.getId(), rx_id);
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, int dr_flg, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, ivr_resp_id, "NOW()", dr_flg, TX_TYPE.INTERACTIVE.getId(), rx_id);
    }

    // support fast reply
    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, int dr_flg, long rx_id, int status) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, ivr_resp_id, "NOW()", dr_flg, TX_TYPE.INTERACTIVE.getId(), rx_id, null, status);
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, int dr_flg, int piority, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, null, "NOW()", dr_flg, piority, rx_id);
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, int dr_flg, int piority, long rx_id, Date deliver_dt) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, null, DatetimeUtil.print("yyyy-MM-dd HH:mm:ss", deliver_dt), dr_flg, piority, rx_id);
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, int dr_flg, int piority, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, ivr_resp_id, "NOW()", dr_flg, piority, rx_id);
    }

    // support DTAC_SDP reference TXID
    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, String datetime, int dr_flg, int piority, long rx_id) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, ivr_resp_id, datetime, dr_flg, piority, rx_id, null);
    }

    // support set status flag
    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, String datetime, int dr_flg, int piority, long rx_id, String ref_txid) {
        return add(action, srvc_main_id, oper_id, msisdn, charging, ivr_resp_id, datetime, dr_flg, piority, rx_id, ref_txid, TX_STATUS.QUEUE.getId());
    }

    public long add(ServiceContentAction action, int srvc_main_id, int oper_id, String msisdn, String charging, String ivr_resp_id, String datetime, int dr_flg, int piority, long rx_id, String ref_txid, int status) {
        synchronized (TxQueue.class) {
            long qid = -1;

            if ((action.action_type == ServiceContentAction.ACTION_TYPE.SMS)
                    && (ContentSmsMessage.isContentBlank(action.contentId) == true)) {
                log.log(Level.WARNING, "content length is zero, rejected!!");
                return qid;
            }

            String date_format = null;
            if (datetime.trim().matches("^(19|20)\\d{2}-(0[1-9]|1[012]|[1-9])-(0[1-9]|[1-9]|[12][0-9]|3[01])$")) {
                date_format = "yyyy-MM-dd";
            } else if (datetime.trim().matches("^(19|20)\\d{2}-(0[1-9]|1[012]|[1-9])-(0[1-9]|[1-9]|[12][0-9]|3[01])(\\ )(\\d{1}|(0|1)\\d{1}|2[0-3]):([0-5]\\d{1}):([0-5]\\d{1})$")) {
                date_format = "yyyy-MM-dd HH:mm:ss";
            } else if (datetime.trim().matches("^(19|20)\\d{2}-(0[1-9]|1[012]|[1-9])-(0[1-9]|[1-9]|[12][0-9]|3[01])([tT])(\\d{1}|(0|1)\\d{1}|2[0-3]):([0-5]\\d{1}):([0-5]\\d{1})$")) {
                date_format = "yyyy-MM-dd'T'HH:mm:ss";
            }

            if (piority != TX_TYPE.INTERACTIVE.getId()) {
                if (action.action_type == ServiceContentAction.ACTION_TYPE.SMS) {
                    Date deliver = new Date();
                    try {
                        if (date_format != null) {
                            deliver = DatetimeUtil.toDate(datetime, date_format);
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "exception caught", e);
                    }
                    long id = checkTxQueueDuplicate(srvc_main_id, oper_id, msisdn, action.contentId, deliver);
                    if (id > 0) {
                        log.log(Level.WARNING, "duplicate message --> {0}, rejected!!", id);
                        return id;
                    }

                }

            }

            String sql = "INSERT INTO trns_tx_queue"
                    + "          (srvc_main_id, oper_id, msisdn"
                    + "           ,ctnt_id, ctnt_type, deliver_dt"
                    + "           , chrg_flg, ivr_resp_id, dr_flg, piority, rx_id, txid)"
                    + "   VALUES (?,?,?,?,?, " + (date_format != null ? "'" + datetime + "'" : "NOW()")
                    + ", ?, "
                    + ((ivr_resp_id != null) && (!ivr_resp_id.trim().isEmpty()) ? ivr_resp_id : "NULL")
                    + ", ?, ?, ?, "
                    + ((ref_txid != null) && (!ref_txid.trim().isEmpty()) ? ref_txid : "NULL")
                    + ");";
            try {
                DBPoolManager cp = new DBPoolManager();
                try {
                    cp.prepareStatement(sql, 1);

                    cp.getPreparedStatement().setInt(1, srvc_main_id);
                    cp.getPreparedStatement().setInt(2, oper_id);
                    cp.getPreparedStatement().setString(3, msisdn);
                    cp.getPreparedStatement().setInt(4, action.contentId);
                    cp.getPreparedStatement().setInt(5, action.action_type.getId());
                    cp.getPreparedStatement().setString(6, charging);
                    cp.getPreparedStatement().setInt(7, dr_flg);
                    cp.getPreparedStatement().setInt(8, piority);
                    cp.getPreparedStatement().setLong(9, rx_id);

                    int row = cp.execUpdatePrepareStatement();
                    if (row == 1) {
                        ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                        try {
                            if (rs.next()) {
                                qid = rs.getLong(1);
                            }
                        } finally {
                            rs.close();
                        }
                    }
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "exception caught", e);
            }
            return qid;
        }
    }

    public int add(long tx_queue_id, int srvc_main_id, CARRIER oper) {
        int row = 0;
        try {
            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(), ServiceElement.SERVICE_TYPE.ALL.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

            String sql = " INSERT INTO trns_tx_queue"
                    + "         (msisdn, srvc_main_id, oper_id, ctnt_id, ctnt_type,"
                    + "         deliver_dt, chrg_flg, ivr_resp_id, dr_flg, piority, rx_id)"
                    + " SELECT m.msisdn, q.srvc_main_id, q.oper_id, q.ctnt_id, q.ctnt_type,"
                    + "         q.deliver_dt,"
                    + "         IF( CURDATE() >= DATE_ADD(m.register_date, INTERVAL m.free_trial DAY), q.chrg_flg, 'MO' ),"
                    + "         q.ivr_resp_id, IFNULL(dr.broadcast, 0), q.piority, q.rx_id"
                    + "   FROM trns_tx_queue As q"
                    + "  INNER JOIN mmbr_" + oper.toString().toLowerCase() + " AS m"
                    + "     ON ("
                    + "        m.state=2"
                    + "        OR (m.state=1 AND CURDATE() <= DATE_ADD(expired_date, INTERVAL " + se.rchg_ctr + " DAY))"
                    + "        )"
                    + "    AND m.msisdn IS NOT NULL"
                    + "    AND m.srvc_main_id=q.srvc_main_id"
                    + "   LEFT JOIN conf_dro As dr"
                    + "     ON dr.srvc_main_id=q.srvc_main_id"
                    + "    AND dr.oper_id = " + oper.getId()
                    + "  WHERE q.tx_queue_id=?";

            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setLong(1, tx_queue_id);

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

    public long add(long tx_queue_id, String msisdn) {
        long qid = 0;
        try {
            String sql
                    = " INSERT INTO trns_tx_queue (msisdn, srvc_main_id, oper_id, ctnt_id, ctnt_type,"
                    + "         deliver_dt, chrg_flg, ivr_resp_id, dr_flg, piority, rx_id, status)"
                    + " SELECT ?, q.srvc_main_id, q.oper_id, q.ctnt_id, q.ctnt_type,"
                    + "         q.deliver_dt, q.chrg_flg, q.ivr_resp_id, q.dr_flg, q.piority, q.rx_id, "
                    + TX_STATUS.SENT.getId()
                    + "   FROM trns_tx_queue As q"
                    + "  WHERE q.tx_queue_id=?"
                    + "  LIMIT 1";

            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setString(1, msisdn);
                cp.getPreparedStatement().setLong(2, tx_queue_id);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        qid = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return qid;
    }

    public int remove() {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM trns_tx_queue WHERE tx_queue_id = ? ";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setLong(1, this.tx_queue_id);
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

    public static Integer[] getTxQueueList(OperConfig.CARRIER carrier, int records) {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "  SELECT tx_queue_id"
                    + "  FROM trns_tx_queue"
                    + " WHERE oper_id=" + carrier.getId()
                    + "   AND deliver_dt <= now()"
                    + "   AND status=0"
                    + " ORDER BY piority DESC, deliver_dt ASC"
                    + " LIMIT " + records;
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(rs.getLong(1));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            return null;
        }
        return (Integer[]) list.toArray(new Integer[0]);
    }

    public static synchronized Timestamp getShortestOccurring(OperConfig.CARRIER carrier) {
        Timestamp ts = null;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "  SELECT deliver_dt"
                    + "  FROM trns_tx_queue"
                    + " WHERE oper_id=" + carrier.getId()
                    + "   AND status=0"
                    + " ORDER BY deliver_dt ASC"
                    + " LIMIT 1";
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ts = rs.getTimestamp(1);
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
        return ts;
    }

    public long get(OperConfig.CARRIER oper) throws Exception {
        synchronized (TxQueue.class) {
            long id = -1;
            try {
                DBPoolManager cp = new DBPoolManager();

                String sql = "";

                switch (oper) {
                    case AIS_LEGACY:
                        sql
                                = "  SELECT q.tx_queue_id"
                                + "  FROM trns_tx_queue q"
                                + " WHERE ( q.msisdn IS NOT NULL"
                                + "         OR q.msisdn IS NULL AND q.srvc_main_id IN"
                                + "          (SELECT srvc_main_id"
                                + "             FROM srvc_sub"
                                + "            WHERE oper_id=" + oper.getId()
                                + "              AND srvc_type &" + ServiceElement.SERVICE_TYPE.SFTP.getId()
                                + "          ) "
                                + "       )"
                                + "   AND q.deliver_dt <= NOW()"
                                + "   AND q.oper_id=" + oper.getId()
                                + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                                + " ORDER BY q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT 1";

                        break;
                    case AIS:
                        sql
                                = "  SELECT q.tx_queue_id"
                                + "  FROM trns_tx_queue q"
                                + " WHERE ( q.msisdn IS NOT NULL"
                                + "         OR q.msisdn IS NULL AND q.srvc_main_id IN"
                                + "         (SELECT srvc_main_id"
                                + "            FROM srvc_sub"
                                + "           WHERE oper_id=" + oper.getId()
                                + "             AND ((srvc_type &" + ServiceElement.SSS_TYPE.TYPE_L.getId() + ")=0)"
                                + "             AND ((srvc_type &" + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId() + ")=0)"
                                + "         )"
                                + "       )"
                                + "   AND q.deliver_dt <= NOW()"
                                + "   AND q.oper_id=" + oper.getId()
                                + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                                + " ORDER BY q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT 1";

                        break;
                    case DTAC:
                    case DTAC_SDP:
                        sql
                                = "  SELECT q.tx_queue_id"
                                + "  FROM trns_tx_queue q "
                                + " WHERE ( q.msisdn IS NOT NULL"
                                + "         OR q.msisdn IS NULL AND q.srvc_main_id IN"
                                + "         (SELECT srvc_main_id"
                                + "            FROM srvc_sub"
                                + "           WHERE oper_id=" + oper.getId()
                                + "             AND ((srvc_type &" + ServiceElement.SERVICE_TYPE.DDS.getId() + ")=0)"
                                + "         )"
                                + "       )"
                                + "   AND q.deliver_dt <= NOW()"
                                + "   AND q.oper_id=" + oper.getId()
                                + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                                + " ORDER BY q.msisdn IS NOT NULL, q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT 1";

                        break;
                    default:
                        sql
                                = " SELECT q.tx_queue_id"
                                + "   FROM trns_tx_queue q"
                                + "  WHERE q.oper_id=" + oper.getId()
                                + "    AND q.deliver_dt <= NOW()"
                                + ((oper == OperConfig.CARRIER.TRUE) || (oper == OperConfig.CARRIER.TRUEH)
                                ? "   AND q.msisdn IS NOT NULL"
                                : "")
                                + "    AND q.status=" + TX_STATUS.QUEUE.getId()
                                + "  ORDER BY q.piority DESC, q.deliver_dt ASC"
                                + "  LIMIT 1";
                }

                try {
                    ResultSet rs = cp.execQuery(sql);
                    if (rs.next()) {
                        id = rs.getLong(1);
                        new TxQueue(id).setStatus(TX_STATUS.SENDING);
                    }

                    rs.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                throw e;
            }
            return id;
        }
    }

    private static int updateFlag(Set set) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            String sql
                    = "   UPDATE trns_tx_queue"
                    + "    SET status=" + TX_STATUS.SENDING.getId()
                    + "  WHERE tx_queue_id=?";
            try {
                cp.prepareStatement(sql);
                for (Iterator iter = set.iterator(); iter.hasNext();) {
                    long i = ((Long) iter.next()).longValue();

                    cp.getPreparedStatement().setLong(1, i);
                    row += cp.execUpdatePrepareStatement();
                }
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Update status flag error!!", e);
        }

        return row;
    }

    private static int updateFlag(Long qid, TX_STATUS status) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            String sql
                    = "   UPDATE trns_tx_queue"
                    + "    SET status=" + status.getId()
                    + "  WHERE tx_queue_id=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setLong(1, qid);
                row = cp.execUpdatePrepareStatement();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Update status flag [" + qid + "] error!!", e);
        }

        return row;
    }

    /**
     * Get TX in QUEUE state by size 2013-01-10 Add condition to prevent invalid
     * content
     *
     * @param oper
     * @param size
     * @return
     * @throws Exception
     */
    public Set get(OperConfig.CARRIER oper, int size) throws Exception {
        synchronized (TxQueue.class) {
            Set sending_set = new LinkedHashSet();
            try {
                DBPoolManager cp = new DBPoolManager();

                String sql;

                switch (oper) {
                    case AIS_LEGACY:
                        sql = "    SELECT q.tx_queue_id"
                                + "  FROM trns_tx_queue q "
                                + PushMTExtendSql.getInstance(oper).getExsql()
                                + " WHERE ( q.msisdn IS NOT NULL"
                                + "         OR q.msisdn IS NULL AND q.srvc_main_id IN"
                                + "          (SELECT srvc_main_id"
                                + "             FROM srvc_sub"
                                + "            WHERE oper_id=" + oper.getId()
                                + "              AND srvc_type &" + ServiceElement.SERVICE_TYPE.SFTP.getId()
                                + "          )"
                                + "       )"
                                + "   AND q.deliver_dt <= NOW()"
                                + "   AND q.oper_id=" + oper.getId()
                                + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                                + "   AND q.ctnt_id != 0 "
                                + PushMTExtendSql.getInstance(oper).getTxsql()
                                + " ORDER BY q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT " + size;

                        break;
                    case AIS:
                        sql = "    SELECT q.tx_queue_id"
                                + "  FROM trns_tx_queue q "
                                + PushMTExtendSql.getInstance(oper).getExsql()
                                + " WHERE ( q.msisdn IS NOT NULL"
                                + "         OR q.msisdn IS NULL AND q.srvc_main_id IN"
                                + "          (SELECT srvc_main_id"
                                + "             FROM srvc_sub"
                                + "            WHERE oper_id=" + oper.getId()
                                + "              AND ((srvc_type &" + ServiceElement.SSS_TYPE.TYPE_L.getId() + ")=0)"
                                + "              AND ((srvc_type &" + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId() + ")=0)"
                                + "          )"
                                + "       )"
                                + "   AND q.deliver_dt <= NOW()"
                                + "   AND q.oper_id=" + oper.getId()
                                + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                                + "   AND q.ctnt_id != 0 "
                                + PushMTExtendSql.getInstance(oper).getTxsql()
                                + " ORDER BY q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT " + size;

                        break;
                    case DTAC:
                    case DTAC_SDP:
                        sql = "    SELECT q.tx_queue_id"
                                + "  FROM trns_tx_queue q "
                                + PushMTExtendSql.getInstance(oper).getExsql()
                                + " WHERE ( q.msisdn IS NOT NULL"
                                + "         OR q.msisdn IS NULL AND q.srvc_main_id IN"
                                + "          (SELECT srvc_main_id"
                                + "             FROM srvc_sub"
                                + "            WHERE oper_id=" + oper.getId()
                                + "              AND ((srvc_type &" + ServiceElement.SERVICE_TYPE.DDS.getId() + ")=0)"
                                + "          )"
                                + "       )"
                                + "   AND q.deliver_dt <= NOW()"
                                + "   AND q.oper_id=" + oper.getId()
                                + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                                + "   AND q.ctnt_id != 0 "
                                + PushMTExtendSql.getInstance(oper).getTxsql()
                                + " ORDER BY q.msisdn IS NOT NULL, q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT " + size;

                        break;
                    default:
                        sql = "     SELECT q.tx_queue_id"
                                + "   FROM trns_tx_queue q "
                                + PushMTExtendSql.getInstance(oper).getExsql()
                                + "  WHERE q.oper_id=" + oper.getId()
                                + "    AND q.deliver_dt <= NOW()"
                                + ((oper == OperConfig.CARRIER.TRUE) || (oper == OperConfig.CARRIER.TRUEH)
                                ? "    AND q.msisdn IS NOT NULL"
                                : "")
                                + "    AND q.status=" + TX_STATUS.QUEUE.getId()
                                + "    AND q.ctnt_id != 0 "
                                + PushMTExtendSql.getInstance(oper).getTxsql()
                                + "  ORDER BY q.piority DESC, q.deliver_dt ASC"
                                + " LIMIT " + size;
                }

                try {
                    ResultSet rs = cp.execQuery(sql);
                    while (rs.next()) {
                        long qid = rs.getLong(1);
                        // prevent duplicate sent, make sure its flag had set to SENDING
                        if (updateFlag(qid, TX_STATUS.SENDING) == 1) {
                            sending_set.add(rs.getLong(1));
                        }
                    }

                    rs.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                throw e;
            }
            return sending_set;
        }
    }

    public static synchronized long getBroadcast(OperConfig.CARRIER oper) {
        long id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "  SELECT tx_queue_id"
                    + "  FROM trns_tx_queue"
                    + " WHERE oper_id=" + oper.getId()
                    + "   AND deliver_dt <= now()"
                    + ((oper == OperConfig.CARRIER.TRUE) || (oper == OperConfig.CARRIER.TRUEH) || (oper == OperConfig.CARRIER.AIS_LEGACY)
                    ? "   AND msisdn IS NOT NULL"
                    : "")
                    + "   AND piority=" + TX_TYPE.BULK.getId()
                    + "   AND status=" + TX_STATUS.QUEUE.getId()
                    + " ORDER BY piority DESC, deliver_dt ASC"
                    + " LIMIT 1";
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    id = rs.getLong(1);
                    new TxQueue(id).setStatus(TX_STATUS.SENDING);
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
        return id;
    }

    public static long get(OperConfig.CARRIER oper, TX_TYPE type) throws Exception {
        long id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT tx_queue_id"
                    + "  FROM trns_tx_queue"
                    + " WHERE oper_id=" + oper.getId()
                    + "   AND deliver_dt <= now()"
                    + ((oper == OperConfig.CARRIER.TRUE) || (oper == OperConfig.CARRIER.TRUEH) || (oper == OperConfig.CARRIER.AIS_LEGACY)
                    ? "   AND msisdn IS NOT NULL"
                    : "")
                    + "   AND piority=" + type.getId()
                    + "   AND status=" + TX_STATUS.QUEUE.getId()
                    + " ORDER BY piority DESC, deliver_dt ASC"
                    + " LIMIT 1";
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    id = rs.getLong(1);
                    new TxQueue(id).setStatus(TX_STATUS.SENDING);
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
        return id;
    }

    public static int getSrvcIdChargeMT(OperConfig.CARRIER carrier, String txid, String msisdn) {
        int srvc_main_id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT srvc_main_id"
                    + "  FROM trns_tx_queue"
                    + " WHERE oper_id=" + carrier.getId()
                    + "   AND chrg_flg='MT'"
                    + "   AND msisdn='" + msisdn + "'"
                    + "   AND MATCH(txid) AGAINST ('" + txid + "')"
                    + "   AND status=" + TX_STATUS.SENT.getId();
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    srvc_main_id = rs.getInt("srvc_main_id");
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
        return srvc_main_id;
    }

    public static int getSrvcMainId(OperConfig.CARRIER carrier, String txid) {
        int srvc_main_id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT srvc_main_id"
                    + "  FROM trns_tx_queue"
                    + " WHERE oper_id=" + carrier.getId()
                    + "   AND MATCH(txid) AGAINST ('" + txid + "')";
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    srvc_main_id = rs.getInt("srvc_main_id");
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
        return srvc_main_id;
    }

    /**
     * @deprecated moved to run with bash shell script instead
     * @param oper
     */
    @Deprecated
    public static void doRetryFlag(OperConfig.CARRIER oper) {
        /*
         try {
         DBPoolManager cp = new DBPoolManager();
         try {
         String sql = "UPDATE trns_tx_queue"
         + "   SET status=" + TX_STATUS.QUEUE.getId()
         + " WHERE oper_id=" + oper.getId()
         + "   AND last_mod_dt <= DATE_SUB( NOW( ) , INTERVAL 5 MINUTE )"
         + "   AND deliver_dt > DATE_SUB( NOW( ) , INTERVAL 1 HOUR ) "
         + "   AND ("
         + "        status=" + TX_STATUS.ERROR.getId()
         + "        OR status=" + TX_STATUS.SENDING.getId()
         + "       )"
         + "   AND txid IS NULL";

         cp.execUpdate(sql);

         sql = "UPDATE trns_tx_queue"
         + "   SET status=" + TX_STATUS.SENT.getId()
         + " WHERE oper_id=" + oper.getId()
         + "   AND (status=" + TX_STATUS.ERROR.getId()
         + "       )"
         + "   AND txid IS NOT NULL";

         cp.execUpdate(sql);
         } catch (SQLException e) {
         log.log(Level.SEVERE, "SQL error!!", e);
         } finally {
         cp.release();
         }
         } catch (Exception e) {
         log.log(Level.SEVERE, "exception caught", e);
         }
         */
    }

    public static void doDistributeMessage(OperConfig.CARRIER oper) {
        synchronized (TxQueue.class) {
            try {
                DBPoolManager cp = new DBPoolManager();
                try {
                    String sql
                            = "  SELECT q.tx_queue_id, q.srvc_main_id"
                            + "  FROM trns_tx_queue q"
                            + (oper == OperConfig.CARRIER.DTAC || oper == OperConfig.CARRIER.DTAC_SDP
                            ? " INNER JOIN srvc_sub s"
                            + "    ON q.srvc_main_id = s.srvc_main_id"
                            + "   AND s.oper_id = " + oper.getId()
                            + "   AND s.srvc_type & " + ServiceElement.SERVICE_TYPE.DDS.getId()
                            : "")
                            + (oper == OperConfig.CARRIER.AIS_LEGACY
                            ? " INNER JOIN srvc_sub s"
                            + "    ON q.srvc_main_id = s.srvc_main_id"
                            + "   AND s.oper_id = " + oper.getId()
                            + "   AND !(s.srvc_type & " + ServiceElement.SERVICE_TYPE.SFTP.getId() + ")"
                            : "")
                            + (oper == OperConfig.CARRIER.AIS
                            ? " INNER JOIN srvc_sub s"
                            + "    ON q.srvc_main_id = s.srvc_main_id"
                            + "   AND s.oper_id = " + oper.getId()
                            + "   AND ( s.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L.getId()
                            + "       OR s.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId()
                            + "       )" : "")
                            + " " + PushMTExtendSql.getInstance(oper).getExsql()
                            + " WHERE q.oper_id=" + oper.getId()
                            + "   AND q.deliver_dt < NOW()"
                            + "   AND q.deliver_dt > DATE_SUB( NOW( ) , INTERVAL 6 HOUR ) "
                            + "   AND q.msisdn IS NULL"
                            + "   AND q.status=" + TX_STATUS.QUEUE.getId()
                            + " " + PushMTExtendSql.getInstance(oper).getTxsql();

                    String deleteSql = "  UPDATE trns_tx_queue"
                            + "    SET status=" + TX_STATUS.SENT.getId()
                            + "     , status_desc='Success'"
                            + " WHERE tx_queue_id=";

                    ResultSet rs = cp.execQuery(sql);

                    while (rs.next()) {
                        int row = cp.execUpdate(deleteSql + rs.getLong(1));

                        if (row == 1) {
                            log.info("Distribute txq found|"
                                    + rs.getLong(1) + "|"
                                    + rs.getInt(2) + "|"
                                    + oper.name()
                                    + " to "
                                    + new TxQueue().add(rs.getLong(1), rs.getInt(2), oper) + " members.");
                        }
                    }
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "exception caught", e);
            }
        }
    }

    public static List<TxQueue> getTxQueueList(String msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, String fdate, String tdate) {
        return getTxQueueList(msisdn, srvc_main_id, oper_id, sort, from, records, fdate, tdate, null);
    }

    public static List<TxQueue> getTxQueueList(String msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, String fdate, String tdate, User user) {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String whereuid = "";
            if (user != null) {
                whereuid = " AND ( 0";
                for (int i = 0; i < user.getChildUid().length; i++) {
                    if (!user.getChildUid()[i].isEmpty()) {
                        whereuid = whereuid + " OR ss.uid=" + user.getChildUid()[i];
                    }
                }
                whereuid = whereuid + " )";
            }

            String order = " ORDER BY deliver_dt ASC";
            if ((sort != null)
                    && (!sort.equals(""))) {
                order = " ORDER BY " + sort;
            }

            String limit = "";
            if (from >= 0) {
                limit = " LIMIT " + from + (records > 0 ? " ," + records : "");
            }

            String whereoper = "";
            if (oper_id > 0) {
                whereoper = " AND q.oper_id=" + oper_id;
            }

            String whereservice = "";
            if (srvc_main_id > 0) {
                whereservice = " AND q.srvc_main_id=" + srvc_main_id;
            }

            String wheredate = "";
            if ((fdate != null) && (tdate != null)) {
                wheredate = " AND DATE(deliver_dt) BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')";
            }

            String sql = "SELECT tx_queue_id"
                    + "  FROM trns_tx_queue q"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON ss.srvc_main_id = q.srvc_main_id"
                    + "   AND ss.oper_id = q.oper_id"
                    + " WHERE 1"
                    + "   AND q.msisdn"
                    + (msisdn != null ? "='" + msisdn + "'" : " IS NULL")
                    + whereoper
                    + whereservice
                    + wheredate
                    + whereuid
                    + order
                    + limit;
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(new TxQueue(rs.getLong(1)));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }

        return list;
    }

    public static long checkTxQueueDuplicate(int srvc_main_id, int oper_id, String msisdn, String msg, Date deliver) {
        long tx_queue_id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheremsisdn = "";
            if ((msisdn == null) || (msisdn.isEmpty())) {
                wheremsisdn = " AND q.msisdn IS NULL";
            } else {
                wheremsisdn = " AND q.msisdn = '" + msisdn + "'";
            }

            String sql
                    = "    SELECT tx_queue_id"
                    + "    FROM trns_tx_queue q"
                    + "   INNER JOIN ctnt_sms_mesg m"
                    + "      ON m.sms_mesg_id = q.ctnt_id"
                    + "     AND m.content = '" + msg + "'"
                    + "   WHERE q.srvc_main_id = " + srvc_main_id
                    + "     AND q.oper_id = " + oper_id
                    + wheremsisdn
                    + "     AND DATE(q.deliver_dt) = DATE('" + DatetimeUtil.print("yyyy-MM-dd", deliver) + "')"
                    + "   ORDER BY tx_queue_id DESC";
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    tx_queue_id = rs.getLong(1);
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }

        return tx_queue_id;
    }

    public static long checkTxQueueDuplicate(int srvc_main_id, int oper_id, String msisdn, int sms_mesg_id, Date deliver) {
        long tx_queue_id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheremsisdn = "";
            if ((msisdn == null) || (msisdn.isEmpty())) {
                wheremsisdn = " AND q.msisdn IS NULL";
            } else {
                wheremsisdn = " AND q.msisdn = '" + msisdn + "'";
            }

            String sql
                    = "    SELECT tx_queue_id"
                    + "    FROM trns_tx_queue q"
                    + "   INNER JOIN ctnt_sms_mesg m"
                    + "      ON m.sms_mesg_id = q.ctnt_id"
                    + "     AND m.content = (SELECT content FROM ctnt_sms_mesg WHERE sms_mesg_id=" + sms_mesg_id + ")"
                    + "   WHERE q.srvc_main_id = " + srvc_main_id
                    + "     AND q.oper_id = " + oper_id + wheremsisdn
                    + "     AND q.deliver_dt = '" + DatetimeUtil.print("yyyy-MM-dd HH:mm:ss", deliver) + "'"
                    + "   ORDER BY tx_queue_id DESC";
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    tx_queue_id = rs.getLong(1);
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }

        return tx_queue_id;
    }

    public final static void checkInvalidTransactions() {
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "    UPDATE trns_tx_queue"
                    + "     SET status=" + TX_STATUS.INVALID.getId()
                    + "   WHERE ctnt_id = 0"
                    + "     AND status <= " + TX_STATUS.SENDING.getId();
            try {
                int row = cp.execUpdate(sql);
                if (row > 0) {
                    log.info("remove invalid TX queue " + row + " row(s).");
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
    }

    public static enum TX_STATUS {

        QUEUE(0),
        SENDING(1),
        SENT(2),
        ERROR(3),
        CANCEL(4),
        INVALID(5);
        private final int id;

        private TX_STATUS(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static TX_STATUS fromId(int id) {
            for (TX_STATUS e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }

    public static enum TX_TYPE {

        BULK(0),
        CONTENT1(1),
        CHARGE_CONTENT1(2),
        CONTENT2(3),
        CHARGE_CONTENT2(4),
        CONTENT3(5),
        CHARGE_CONTENT3(6),
        WARNING(7),
        RECURRING(8),
        INTERACTIVE(9);
        private final int id;

        private TX_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static TX_TYPE fromId(int id) {
            for (TX_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}
