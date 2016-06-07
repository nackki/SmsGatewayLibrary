package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class SubscriptionTracker implements Serializable {

    private static final Logger log = Logger.getLogger(SubscriptionTracker.class.getName());
    protected int sub_track_id;
    protected String msisdn;
    protected int srvc_main_id;
    protected OperConfig.CARRIER oper;
    protected String subtype;
    protected String channel;
    protected Date recv_dt;
    protected int rept_actn_type;

    public SubscriptionTracker(int sub_track_id)
            throws Exception {
        this.sub_track_id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "   SELECT *"
                        + "   FROM trns_sub_track"
                        + "  WHERE sub_track_id = " + sub_track_id;

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    this.sub_track_id = sub_track_id;
                    this.msisdn = rs.getString("msisdn");
                    this.srvc_main_id = rs.getInt("srvc_main_id");
                    this.oper = OperConfig.CARRIER.fromId(rs.getInt("oper_id"));
                    this.subtype = rs.getString("subtype");
                    this.channel = rs.getString("channel");
                    this.recv_dt = DatetimeUtil.toDate(rs.getTimestamp("recv_dt"));
                    this.rept_actn_type = rs.getInt("rept_actn_type");
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static String preventDuplicate(String subtype, Date date) {
        String sql =
                " AND t.sub_track_id NOT IN ("
                + "   SELECT a.sub_track_id"
                + "     FROM trns_sub_track a"
                + "    INNER JOIN"
                + "             (SELECT msisdn,"
                + "                     sub_track_id,"
                + "                     COUNT(*) AS c"
                + "                FROM trns_sub_track"
                + "               WHERE DATE(recv_dt) = '" + DatetimeUtil.print("yyyy-MM-dd", date) + "'"
                + "                     AND subtype = '" + subtype + "'"
                + "              GROUP BY msisdn"
                + "              HAVING c > 1) AS b"
                + "          ON a.msisdn = b.msisdn"
                + "             AND a.sub_track_id != b.sub_track_id"
                + "             AND DATE(recv_dt) = '" + DatetimeUtil.print("yyyy-MM-dd", date) + "'"
                + "             AND subtype = '" + subtype + "'"
                + "   ORDER BY a.msisdn, a.sub_track_id)";

        return sql;
    }

    public int updateReportType(int type) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "   UPDATE trns_sub_track"
                        + "    SET rept_actn_type = rept_actn_type | " + Math.pow(2.0D, type)
                        + "  WHERE sub_track_id = " + this.sub_track_id;

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public int updateRelateReportId(int id) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "   UPDATE trns_sub_track"
                        + "    SET trns_sum_id = " + id
                        + "  WHERE sub_track_id = " + this.sub_track_id;

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public OperConfig.CARRIER getOper() {
        return this.oper;
    }

    public void setOper(OperConfig.CARRIER oper) {
        this.oper = oper;
    }

    public Date getRecv_dt() {
        return this.recv_dt;
    }

    public void setRecv_dt(Date recv_dt) {
        this.recv_dt = recv_dt;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public void setSrvc_main_id(int srvc_main_id) {
        this.srvc_main_id = srvc_main_id;
    }

    public String getSubtype() {
        return this.subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public int getRept_actn_type() {
        return this.rept_actn_type;
    }

    public void setRept_actn_type(int rept_actn_type) {
        this.rept_actn_type = rept_actn_type;
    }
}