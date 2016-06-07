package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class TxHybridQueue {

    private static final Logger log = Logger.getLogger(TxHybridQueue.class.getClass().getName());
    protected int tx_hybd_id;
    protected ServiceElement se;
    protected String msisdn;
    protected String srvc_num;
    protected String ctnt_id;
    protected HybridConfig link;
    protected Date deliver_dt;
    protected User owner;

    public int hashCode() {
        return this.tx_hybd_id;
    }

    public boolean equals(Object obj) {
        if ((obj instanceof TxHybridQueue)) {
            TxHybridQueue txq = (TxHybridQueue) obj;

            return this.tx_hybd_id == txq.tx_hybd_id;
        }
        return false;
    }

    public TxHybridQueue() {
    }

    public TxHybridQueue(int tx_hybd_id) throws Exception {
        this.tx_hybd_id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT * FROM trns_tx_hybd  WHERE tx_hybd_id=?;";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, tx_hybd_id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.tx_hybd_id = tx_hybd_id;
                this.se = new ServiceElement(rs.getInt("srvc_main_id"), OperConfig.CARRIER.AIS.getId(), ServiceElement.SERVICE_TYPE.ALL.getDbId(), ServiceElement.SERVICE_STATUS.ALL.getId());

                this.msisdn = rs.getString("msisdn");
                this.srvc_num = rs.getString("srvc_num");
                this.ctnt_id = rs.getString("ctnt_id");
                this.link = new HybridConfig(rs.getInt("link_hybd_id"));
                this.deliver_dt = rs.getTimestamp("deliver_dt");
                this.owner = new User(rs.getInt("uid"));
            } else {
                throw new Exception("Hybrid tx queue not found!!");
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public int add(int srvc_main_id, String msisdn, String srvc_num, String ctnt_id, HybridConfig link, Date deliver_dt, User user) {
        synchronized (TxHybridQueue.class) {
            int qid = -1;

            if ((msisdn == null) || (msisdn.isEmpty()) || (srvc_num.isEmpty()) || (ctnt_id.isEmpty()) || (link == null) || (!srvc_num.equals(ctnt_id))) {
                log.log(Level.WARNING, "incorrect information, rejected!!");
                return qid;
            }

            String sql = "INSERT INTO  trns_tx_hybd (srvc_main_id, msisdn, srvc_num, ctnt_id, link_hybd_id, uid, deliver_dt) VALUES (?,?,?,?,?,?, " + (deliver_dt != null ? "'" + DatetimeUtil.print("yyyy-MM-dd HH:mm:ss", deliver_dt) + "'" : "NOW()") + " );";
            try {
                DBPoolManager cp = new DBPoolManager();
                try {
                    cp.prepareStatement(sql, 1);

                    cp.getPreparedStatement().setInt(1, srvc_main_id);
                    cp.getPreparedStatement().setString(2, msisdn);
                    cp.getPreparedStatement().setString(3, srvc_num);
                    cp.getPreparedStatement().setString(4, ctnt_id);
                    cp.getPreparedStatement().setInt(5, link.link_hybd_id);
                    cp.getPreparedStatement().setInt(6, user.uid);

                    int row = cp.execUpdatePrepareStatement();
                    if (row == 1) {
                        ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                        try {
                            if (rs.next()) {
                                qid = rs.getInt(1);
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

    public int get()
            throws Exception {
        synchronized (TxHybridQueue.class) {
            int id = -1;
            int status = ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId();
            String wherestatus = ServiceElement.SERVICE_STATUS.where(status, "ss.status");
            try {
                DBPoolManager cp = new DBPoolManager();

                String sql = "    SELECT q.tx_hybd_id  FROM trns_tx_hybd q INNER JOIN srvc_sub ss    ON ss.srvc_main_id = q.srvc_main_id   AND ss.oper_id = " + OperConfig.CARRIER.AIS.getId() + wherestatus + " WHERE q.msisdn IS NOT NULL" + "   AND q.deliver_dt <= NOW()" + "   AND q.status=" + TxQueue.TX_STATUS.QUEUE.getId() + " ORDER BY q.deliver_dt ASC, q.tx_hybd_id ASC" + " LIMIT 1";
                try {
                    ResultSet rs = cp.execQuery(sql);
                    if (rs.next()) {
                        id = rs.getInt(1);
                        new TxHybridQueue(id).setStatus(TxQueue.TX_STATUS.SENDING);
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

    public int setStatus(TxQueue.TX_STATUS status) {
        int istatus = status.getId();
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_tx_hybd  SET status = ? WHERE tx_hybd_id = ?";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, istatus);
                cp.getPreparedStatement().setInt(2, this.tx_hybd_id);
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

    public int cancel() {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_tx_hybd  SET status = ? WHERE tx_hybd_id = ?   AND ( status=" + TxQueue.TX_STATUS.QUEUE.getId() + " OR status=" + TxQueue.TX_STATUS.ERROR.getId() + ")";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, TxQueue.TX_STATUS.CANCEL.getId());
                cp.getPreparedStatement().setInt(2, this.tx_hybd_id);
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

    public int remove() {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM trns_tx_hybd WHERE tx_hybd_id = ? ";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.tx_hybd_id);
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

    public int updateResponse(String txid, String status_desc) {
        int istatus = 0;
        int row = 0;

        istatus = TxQueue.TX_STATUS.SENT.getId();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_tx_hybd    SET status = ?     , txid = ?     , status_desc = ? WHERE tx_queue_id = ?";

                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, istatus);
                cp.getPreparedStatement().setString(2, txid);
                cp.getPreparedStatement().setString(3, status_desc);
                cp.getPreparedStatement().setInt(4, this.tx_hybd_id);
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

    public static void doRetryFlag() {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_tx_hybd   SET status=" + TxQueue.TX_STATUS.QUEUE.getId() + " WHERE last_mod_dt <= DATE_SUB( NOW( ) , INTERVAL 5 MINUTE )" + "   AND deliver_dt > DATE_SUB( NOW( ) , INTERVAL 1 HOUR ) " + "   AND (" + "        status=" + TxQueue.TX_STATUS.ERROR.getId() + "        OR status=" + TxQueue.TX_STATUS.SENDING.getId() + "       )";

                cp.execUpdate(sql);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
    }

    public String getCtnt_id() {
        return this.ctnt_id;
    }

    public void setCtnt_id(String ctnt_id) {
        this.ctnt_id = ctnt_id;
    }

    public Date getDeliver_dt() {
        return this.deliver_dt;
    }

    public void setDeliver_dt(Date deliver_dt) {
        this.deliver_dt = deliver_dt;
    }

    public HybridConfig getLink() {
        return this.link;
    }

    public void setLink(HybridConfig link) {
        this.link = link;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ServiceElement getSe() {
        return this.se;
    }

    public void setSe(ServiceElement se) {
        this.se = se;
    }

    public String getSrvc_num() {
        return this.srvc_num;
    }

    public void setSrvc_num(String srvc_num) {
        this.srvc_num = srvc_num;
    }

    public int getTx_hybd_id() {
        return this.tx_hybd_id;
    }

    public void setTx_hybd_id(int tx_hybd_id) {
        this.tx_hybd_id = tx_hybd_id;
    }
}