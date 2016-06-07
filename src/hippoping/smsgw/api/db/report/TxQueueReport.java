package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.content.manage.MessageDetail;
import hippoping.smsgw.api.db.DeliveryReport;
import hippoping.smsgw.api.db.Message;
import hippoping.smsgw.api.db.MessageMms;
import hippoping.smsgw.api.db.MessageSms;
import hippoping.smsgw.api.db.MessageWap;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.ServiceElement.SSS_TYPE;
import hippoping.smsgw.api.db.TxQueue;
import hippoping.smsgw.api.db.User;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class TxQueueReport
        implements MessageDetail, Serializable {

    private static final Logger log = Logger.getLogger(TxQueueReport.class.getClass().getName());
    protected TxQueue txQueue;
    protected List<DeliveryReport> txQueueDr;
    protected Message message;

    public static List<TxQueueReport> getMessageHistory(String msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, String fdate, String tdate) {
        return getMessageHistory(msisdn, srvc_main_id, oper_id, sort, from, records, fdate, tdate, null);
    }

    public static List<TxQueueReport> getMessageHistory(String msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, String fdate, String tdate, User user) {
        List txqList = new ArrayList();
        List contentList = new ArrayList();

        switch (CARRIER.fromId(oper_id)) {
            case AIS_LEGACY:
            case TRUE:
            case TRUEH:
                txqList.addAll(TxQueue.getTxQueueList(msisdn, srvc_main_id, oper_id, sort, from, records, fdate, tdate, user));
                break;
            case AIS:
            case DTAC:
            case DTAC_SDP:
            default:
                txqList.addAll(TxQueue.getTxQueueList(msisdn, srvc_main_id, oper_id, sort, from, records, fdate, tdate, user));

                Iterator iter = txqList.iterator();
                while (iter.hasNext()) {
                    TxQueue txq = (TxQueue) iter.next();
                    if (!contentList.contains(Integer.valueOf(txq.content_id))) {
                        contentList.add(Integer.valueOf(txq.content_id));
                    }
                }

                try {
                    ServiceElement se = new ServiceElement(srvc_main_id, oper_id,
                            ServiceElement.SERVICE_TYPE.ALL.getId(),
                            ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

                    SSS_TYPE sss_type = se.getSSSType();

                    if (((CARRIER.fromId(oper_id) == CARRIER.AIS)
                            && ((sss_type == SSS_TYPE.TYPE_A) || (sss_type == SSS_TYPE.TYPE_B)))
                            || ((CARRIER.fromId(oper_id) == CARRIER.DTAC || CARRIER.fromId(oper_id) == CARRIER.DTAC_SDP)
                            && ((se.srvc_type & ServiceElement.SERVICE_TYPE.DDS.getId()) == 0))) {
                        List tmplist = TxQueue.getTxQueueList(null, srvc_main_id, oper_id, sort, from, records, fdate, tdate, user);
                        iter = tmplist.iterator();
                        while (iter.hasNext()) {
                            TxQueue txq = (TxQueue) iter.next();

                            if (!contentList.contains(Integer.valueOf(txq.content_id))) {
                                txqList.add(txq);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "find service error!!", e);
                }

        }

        List tqrList = new ArrayList();

        for (int i = 0; i < txqList.size(); i++) {
            TxQueue txq = (TxQueue) txqList.get(i);
            TxQueueReport tqr = new TxQueueReport();
            tqr.txQueue = txq;
            if (txq != null) {
                try {
                    if (txq.txid != null && msisdn != null) {
                        //log.info("find dr[" + txq.txid + "] for tx[" + txq.getTx_queue_id() + "],oper_id=" + txq.oper_id);
                        tqr.setTxQueueDeliveryReport(DeliveryReport.get(txq, msisdn));
                    }
                    switch (txq.content_type) {
                        case SMS:
                            tqr.message = new MessageSms(txq.content_id);
                            break;
                        case WAP:
                            tqr.message = new MessageWap(txq.content_id);
                            break;
                        case MMS:
                            tqr.message = new MessageMms(txq.content_id);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "find DR error!!", e);
                }
            }
            tqrList.add(tqr);
        }

        return tqrList;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getTxQueue().getTx_queue_id()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof TxQueueReport)) {
            TxQueueReport txQueueReport = (TxQueueReport) obj;
            return getTxQueue().getTx_queue_id() > txQueueReport.getTxQueue().getTx_queue_id();
        }
        return false;
    }

    public long compareTo(TxQueueReport txQueueReport) {
        return getTxQueue().getTx_queue_id() - txQueueReport.getTxQueue().getTx_queue_id();
    }

    public TxQueue getTxQueue() {
        return this.txQueue;
    }

    public Message getMessage() {
        return this.message;
    }

    public List<DeliveryReport> getTxQueueDeliveryReport() {
        return this.txQueueDr;
    }

    public void setTxQueueDeliveryReport(Collection c) {
        if (this.txQueueDr == null) {
            this.txQueueDr = new ArrayList();
        }
        this.txQueueDr.addAll(c);
    }

    public void setTxQueueDeliveryReport(DeliveryReport dr) {
        if (this.txQueueDr == null) {
            this.txQueueDr = new ArrayList();
        }
        this.txQueueDr.add(dr);
    }

    public static String getLastTimeSent(CARRIER oper) throws Exception {
        String time = "N/A";
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT last_mod_dt  FROM trns_tx_queue WHERE oper_id = ?   AND status = ? ORDER BY last_mod_dt DESC LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper.getId());
            cp.getPreparedStatement().setInt(2, TxQueue.TX_STATUS.SENT.getId());

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                time = rs.getString(1);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
        return time;
    }

    public static int getMaxThroughput(CARRIER oper) throws Exception {
        int speed = 0;
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT COUNT( tx_queue_id ) AS speed  FROM trns_tx_queue WHERE oper_id = ?   AND STATUS = ?   AND DATE( last_mod_dt ) = CURDATE( ) GROUP BY last_mod_dt ORDER BY speed DESC  LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper.getId());
            cp.getPreparedStatement().setInt(2, TxQueue.TX_STATUS.SENT.getId());

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                speed = rs.getInt(1);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
        return speed;
    }

    public static long getTxCount(CARRIER oper, TxQueue.TX_STATUS status) throws Exception {
        long count = 0L;
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT COUNT( tx_queue_id )  FROM trns_tx_queue WHERE oper_id = ?   AND STATUS = ?   AND DATE( last_mod_dt ) = CURDATE( ) LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper.getId());
            cp.getPreparedStatement().setInt(2, status.getId());

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                count = rs.getLong(1);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
        return count;
    }

    public static long getTxCount(CARRIER oper, TxQueue.TX_STATUS status, TxQueue.TX_TYPE type) throws Exception {
        long count = 0L;
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT COUNT( tx_queue_id )  FROM trns_tx_queue WHERE oper_id = ?   AND STATUS = ?   AND piority = ?   AND DATE( last_mod_dt ) = CURDATE( ) LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper.getId());
            cp.getPreparedStatement().setInt(2, status.getId());
            cp.getPreparedStatement().setInt(3, type.getId());

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                count = rs.getLong(1);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
        return count;
    }

    public static long getTxCount(CARRIER oper, TxQueue.TX_TYPE type) throws Exception {
        long count = 0L;
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT COUNT( tx_queue_id )  FROM trns_tx_queue WHERE oper_id = ?   AND piority = ?   AND DATE( last_mod_dt ) = CURDATE( ) LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper.getId());
            cp.getPreparedStatement().setInt(2, type.getId());

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                count = rs.getLong(1);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
        return count;
    }

    public Date getTimestamp() {
        Date date = null;
        try {
            date = DatetimeUtil.toDate(this.txQueue.deliver_dt, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
        }
        return date;
    }

    public String getContent_Type() {
        return this.txQueue.content_type.toString();
    }

    public long getId() {
        return this.txQueue.getTx_queue_id();
    }
}