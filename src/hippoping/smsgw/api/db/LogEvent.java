/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

/**
 *
 * @author nack_ki
 */
public class LogEvent {

    public final static int _EVENT_TYPE_BITWISE = 6;
    public final static int _EVENT_ACTION_BITWISE = 0;

    public static enum LOG_LEVEL {

        ALL(0),
        DEBUG(1),
        ERROR(2),
        WARNING(4),
        INFO(8);
        private final int id;

        private LOG_LEVEL(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static LOG_LEVEL fromId(int id) {
            for (LOG_LEVEL e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        public static String where(int level, String field) {
            String where = "";
            if (level > ALL.getId()) {
                LOG_LEVEL[] LEVEL = {DEBUG, ERROR, WARNING, INFO};
                where = " AND ( 0";
                for (LOG_LEVEL l : LEVEL) {
                    if ((l.getId() & level) > 0) {
                        where = where + " OR (" + field + "&" + l.getId() + ")";
                    }
                }
                where = where + ")";
            }

            return where;
        }
    }

    public static enum EVENT_ACTION {

        NONE(0),
        ADD(1),
        DELETE(2),
        MODIFY(4),
        SEARCH(8),
        EXPORT(16),
        IMPORT(32);
        private final int id;

        private EVENT_ACTION(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static EVENT_ACTION fromId(int id) {
            for (EVENT_ACTION e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        public static String where(int action, String field) {
            String where = "";
            if (action > NONE.getId()) {
                EVENT_ACTION[] ACTION = {ADD, DELETE, MODIFY, SEARCH, EXPORT, IMPORT};
                where = " AND ( 1";
                for (EVENT_ACTION a : ACTION) {
                    if ((a.getId() & action) > 0) {
                        where = where + " AND (" + field + "&" + a.getId() + ")";
                    }
                }
                where = where + ")";
            }

            return where;
        }
    }

    public static enum EVENT_TYPE {

        ALL(0),
        SERVICE(1),
        LINK(2),
        SUBSCRIBER(4),
        SMS(8),
        WAP(16),
        MMS(32),
        BLOCK_LIST(64),
        REPORT_DAILY(128),
        REPORT_SUMMARY(256),
        REPORT_MONTHLY(512),
        REPORT_SMSDOWNLOAD(1024),
        LOG_IN(2048),
        LOG_OUT(4096),
        MESSAGE_HISTORY(8192),
        THIRD_PARTY(16384);
        private final int id;

        private EVENT_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static EVENT_TYPE fromId(int id) {
            for (EVENT_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        public static String where(int type, String field) {
            String where = "";
            if (type > ALL.getId()) {
                EVENT_TYPE[] TYPE = {SERVICE, LINK, SUBSCRIBER, SMS, WAP, MMS, BLOCK_LIST, REPORT_DAILY, REPORT_SUMMARY, REPORT_MONTHLY, REPORT_SMSDOWNLOAD, LOG_IN, LOG_OUT};

                where = " AND ( 1";
                for (EVENT_TYPE t : TYPE) {
                    if ((t.getId() & type) > 0) {
                        where = where + " AND (" + field + "&" + (t.getId() << 6) + ")";
                    }
                }
                where = where + ")";
            }

            return where;
        }
    }
    public int log_evnt_id;
    public User user;
    public EVENT_TYPE event_type;
    public EVENT_ACTION event_action;
    public String event_desc = null;
    public String msisdn = null;
    public OperConfig.CARRIER oper;
    public String serviceName = null;
    public OperConfig operConfig;
    public TxQueue txQueue;
    public Date timestamp;
    public LOG_LEVEL log_level;

    public LogEvent() {
    }

    public LogEvent(int log_evnt_id) throws Exception {
        this.log_evnt_id = log_evnt_id;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql =
                    "  SELECT l.*, sm.name"
                    + "  FROM log_evnt l"
                    + "  LEFT JOIN srvc_main sm"
                    + "    ON l.srvc_main_id = sm.srvc_main_id"
                    + " WHERE log_evnt_id=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, log_evnt_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.user = UserFactory.getUser(rs.getInt("uid"));

                int evnt_id = rs.getInt("evnt_id");
                this.event_type = EVENT_TYPE.fromId(evnt_id >> 6);
                this.event_action = EVENT_ACTION.fromId(evnt_id & 0x3F);

                this.event_desc = rs.getString("evnt_desc");
                this.msisdn = rs.getString("msisdn");
                this.log_level = LOG_LEVEL.fromId(rs.getInt("level"));

                int oper_id = rs.getInt("oper_id");
                int srvc_main_id = rs.getInt("srvc_main_id");
                int link_conf_id = rs.getInt("link_conf_id");
                long tx_queue_id = rs.getLong("tx_queue_id");

                this.oper = OperConfig.CARRIER.fromId(oper_id);
                this.serviceName = rs.getString("name");

                if (link_conf_id > 0) {
                    this.operConfig = new OperConfig(link_conf_id);
                }

                if (tx_queue_id > 0) {
                    this.txQueue = new TxQueue(tx_queue_id);
                }

                this.timestamp = rs.getTimestamp("last_mod_dt");
            } else {
                throw new Exception("there are no event log for log_evnt_id:" + log_evnt_id + "!!");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
    }

    public static List<LogEvent> get(int level, User user, int lastid) throws Exception {
        return get(level, user, null, null, null, null, null, lastid);
    }

    public static List<LogEvent> get(int level, User user, Date from, Date to, EVENT_TYPE type, EVENT_ACTION action, int lastid) throws Exception {
        return get(level, user, null, null, null, null, null, lastid);
    }

    public static List<LogEvent> get(int level, User user, String msisdn, Date from, Date to, EVENT_TYPE type, EVENT_ACTION action, int lastid) throws Exception {
        List logs = null;

        String wherelevel = LOG_LEVEL.where(level, "level");
        String whereaction = action != null ? EVENT_ACTION.where(action.getId(), "evnt_id") : "";
        String wheretype = type != null ? EVENT_TYPE.where(type.getId(), "evnt_id") : "";

        String whereuid = "";
        if (user != null) {
            whereuid = " AND ( 0";
            for (int i = 0; i < user.getChildUid().length; i++) {
                if (!user.getChildUid()[i].isEmpty()) {
                    whereuid = whereuid + " OR uid=" + user.getChildUid()[i];
                }
            }
            whereuid = whereuid + " )";
        }

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql =
                    "  SELECT log_evnt_id"
                    + "  FROM log_evnt WHERE 1"
                    + wherelevel
                    + whereaction
                    + wheretype
                    + whereuid
                    + (lastid > 0
                    ? "   AND log_evnt_id > " + lastid
                    : "")
                    + (from != null
                    ? "   AND DATE(last_mod_dt) >= " + DatetimeUtil.print("''yyyy-MM-dd''", from)
                    : "")
                    + (to != null
                    ? "   AND DATE(last_mod_dt) <= " + DatetimeUtil.print("''yyyy-MM-dd''", to)
                    : "")
                    + ((msisdn != null) && (!msisdn.isEmpty())
                    ? "   AND ( msisdn = '" + msisdn + "' OR MATCH (evnt_desc) AGAINST ('" + msisdn + "') )"
                    : "")
                    + "   ORDER BY log_evnt_id DESC LIMIT 100 ";

            //System.out.println(sql);
            ResultSet rs = cp.execQuery(sql);
            while (rs.next()) {
                if (logs == null) {
                    logs = new ArrayList();
                }

                logs.add(new LogEvent(rs.getInt(1)));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }

        return logs;
    }

    public String getDetails() {
        String detail = "";

        if (this.msisdn != null) {
            detail = detail + "MSISDN:" + this.msisdn + ",";
        }

        if (this.serviceName != null) {
            detail = detail + "Service:" + this.serviceName + ",";
        }

        if (this.oper != null) {
            detail = detail + "Operator:" + this.oper + ",";
        }

        if (this.operConfig != null) {
            detail = detail + "Link Config:" + this.operConfig.conf_name + "(" + this.operConfig.conf_id + ")";
        }

        if (this.txQueue != null) {
            detail = detail + "tx_queue_id:" + this.txQueue.getTx_queue_id();
        }

        return detail;
    }

    public static int log(EVENT_TYPE type, EVENT_ACTION action, String desc, User user, String msisdn, OperConfig.CARRIER oper, int srvc_main_id, int link_conf_id, long tx_queue_id, LOG_LEVEL level) {
        int id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "  INSERT INTO log_evnt ("
                        + "uid, "
                        + "evnt_id, "
                        + "evnt_desc, "
                        + "msisdn, "
                        + "oper_id, "
                        + "srvc_main_id, "
                        + "link_conf_id, "
                        + "tx_queue_id, "
                        + "level)"
                        + " VALUES (?,?,?,?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, user != null ? user.uid : 0);
                cp.getPreparedStatement().setInt(2, type.getId() << 6 | action.getId() << 0);
                cp.getPreparedStatement().setString(3, desc);
                cp.getPreparedStatement().setString(4, msisdn);
                cp.getPreparedStatement().setInt(5, oper != null ? oper.getId() : 0);
                cp.getPreparedStatement().setInt(6, srvc_main_id);
                cp.getPreparedStatement().setInt(7, link_conf_id);
                cp.getPreparedStatement().setLong(8, tx_queue_id);
                cp.getPreparedStatement().setInt(9, level.getId());

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    try {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    } finally {
                        rs.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }
}
