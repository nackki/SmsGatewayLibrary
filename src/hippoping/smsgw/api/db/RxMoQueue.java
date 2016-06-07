package hippoping.smsgw.api.db;

import hippoping.smsgw.api.content.manage.MessageDetail;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_STATUS;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class RxMoQueue extends RxQueue
        implements MessageDetail {

    private static final Logger log = Logger.getLogger(RxMoQueue.class.getClass().getName());
    protected long rx_mo_id;
    protected int srvc_main_id;
    protected String msisdn;
    protected String content_id;
    protected String content_type;

    public RxMoQueue(long rx_mo_id, long rx_id)
            throws Exception {
        super(rx_id);

        this.rx_id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT * FROM trns_rx_mo  WHERE rx_mo_id=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, rx_mo_id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.rx_mo_id = rx_mo_id;
                this.srvc_main_id = rs.getInt("srvc_main_id");
                this.msisdn = rs.getString("msisdn");
                this.content_type = rs.getString("content_type");
                this.content_id = rs.getString("content_id");
                this.content = rs.getString("content");
            } else {
                throw new Exception("rx mo queue not found " + rx_mo_id);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public Date getTimestamp() {
        Date date = null;
        try {
            date = DatetimeUtil.toDate(this.recv_dt);
        } catch (Exception e) {
        }
        return date;
    }

    public String getContent_Type() {
        return this.content_type;
    }

    public long getId() {
        return getRx_mo_id();
    }

    public static long add(long rx_id, int srvc_main_id, String msisdn, String content_type, String content_id, String content) {
        long qid = -1;

        String sql = "INSERT INTO trns_rx_mo (rx_id, srvc_main_id, msisdn, content_type, content_id, content) VALUES (?,?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setLong(1, rx_id);
                cp.getPreparedStatement().setInt(2, srvc_main_id);
                cp.getPreparedStatement().setString(3, msisdn);
                cp.getPreparedStatement().setString(4, content_type);
                cp.getPreparedStatement().setString(5, content_id);
                cp.getPreparedStatement().setString(6, content);

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

    public static List<RxMoQueue> getRxQueueList(String msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, String fdate, String tdate, User user) {
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

            String order = " ORDER BY recv_dt ASC";
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
                whereservice = " AND mo.srvc_main_id=" + srvc_main_id;
            }

            String wheredate = "";
            if ((fdate != null) && (tdate != null)) {
                wheredate = " AND DATE(recv_dt) BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')";
            }

            String sql
                    = "SELECT rx_mo_id, mo.rx_id"
                    + "  FROM trns_rx_mo mo"
                    + " INNER JOIN trns_rx q"
                    + "    ON q.rx_id = mo.rx_id"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON ss.srvc_main_id = mo.srvc_main_id"
                    + "   AND ss.oper_id = q.oper_id"
                    + " WHERE 1"
                    + "   AND mo.msisdn" + (msisdn != null ? "='" + msisdn + "'" : " IS NULL")
                    + whereoper
                    + whereservice
                    + wheredate
                    + whereuid
                    + order
                    + limit;
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(new RxMoQueue(rs.getInt(1), rs.getInt(2)));
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

    public static RxMoQueue getLatestRxMoQueue(String srvc_id, int oper_id, String msisdn, int before_minutes) {
        return getLatestRxMoQueue(srvc_id, oper_id, msisdn, before_minutes, null);
    }

    public static RxMoQueue getLatestRxMoQueue(String srvc_id, int oper_id, String msisdn, int before_minutes, RX_TYPE type) {
        log.log(Level.INFO, "Find previous Rx_MO_Queue for {0}->{1}...", new Object[]{msisdn, srvc_id});

        RxMoQueue moq = null;
        try {
            DBPoolManager cp = new DBPoolManager();

            String order = " ORDER BY q.recv_dt DESC";
            String limit = " LIMIT 1";

            String wherebefore = "";
            if (before_minutes > 0) {
                wherebefore = " AND q.recv_dt > DATE_SUB(NOW(), INTERVAL " + before_minutes + " MINUTE)";
            }

            String whereoper = "";
            if (oper_id > 0) {
                whereoper = " AND q.oper_id=" + oper_id;
            }

            String whereservice = "";
            if (srvc_id != null && !srvc_id.isEmpty()) {
                whereservice = " AND (( ss.srvc_id='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.ON.getDbId()
                        + " ) OR (ss.srvc_id_mo_test='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.TEST.getDbId() + "))";
            }

            String wheretype = "";
            if (type != null) {
                wheretype = " AND q.type & " + type.getId();
            }

            String sql
                    = "  SELECT rx_mo_id, q.rx_id"
                    + "  FROM trns_rx_mo mo"
                    + " INNER JOIN trns_rx q"
                    + "    ON q.rx_id = mo.rx_id"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON ss.srvc_main_id = mo.srvc_main_id"
                    + "   AND ss.oper_id = q.oper_id"
                    + " WHERE 1"
                    + "   AND mo.msisdn" + (msisdn != null ? "='" + msisdn + "'" : " IS NULL")
                    + wheretype
                    + whereoper
                    + whereservice
                    + wherebefore
                    + order
                    + limit;
            //log.log(Level.INFO, sql);
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    moq = new RxMoQueue(rs.getInt(1), rs.getInt(2));
                    log.log(Level.INFO, "MO found, rx_mo_id={0}|srvc_main_id={1}", new Object[]{moq.rx_mo_id, moq.srvc_main_id});
                } else {
                    log.log(Level.INFO, "MO not found!!");
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

        return moq;
    }

    public String getContent_id() {
        return this.content_id;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public long getRx_mo_id() {
        return this.rx_mo_id;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }
}
