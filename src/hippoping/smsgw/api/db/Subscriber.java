package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class Subscriber
        implements Comparable<Subscriber>, Serializable {

    private static final Logger log = Logger.getLogger(Subscriber.class.getName());
    protected String msisdn;
    protected int srvc_main_id;
    protected int ctnt_ctr;
    protected int free_trial;
    protected int rmdr_ctr;
    protected int rchg_ctr;
    protected java.util.Date register_date;
    protected java.util.Date unregister_date;
    protected java.util.Date expired_date;
    protected java.util.Date balanced_date;
    protected int non_expired;
    protected int srvc_chrg_type_id;
    protected int srvc_chrg_amnt;
    protected int state;
    protected int oper_id;
    protected String oper_name;
    protected String srvc_name;
    protected String shortcode;

    @Override
    public int hashCode() {
        return this.srvc_main_id * 100000000 + Integer.parseInt(this.msisdn.substring(3));
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Subscriber)) {
            Subscriber sub = (Subscriber) obj;
            return (this.msisdn.equals(sub.msisdn)) && (this.srvc_main_id == sub.srvc_main_id);
        }
        return false;
    }

    public String getShortcode() {
        return this.shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public int compareTo(Subscriber sub) {
        return Integer.parseInt(this.msisdn) - Integer.parseInt(sub.msisdn);
    }

    public Subscriber(String msisdn, int srvc_main_id, CARRIER oper) throws Exception {
        this.oper_name = oper.name();
        DBPoolManager cp = new DBPoolManager();

        String sql
                = "SELECT m.ctnt_ctr"
                + "     , DATEDIFF(DATE_ADD(m.register_date, INTERVAL m.free_trial DAY), CURDATE()) AS ft"
                + "     , m.rmdr_ctr"
                + "     , m.extd_ctr"
                + "     , m.register_date"
                + "     , m.unregister_date"
                + "     , m.expired_date"
                + "     , m.non_expired"
                + "     , m.balanced_date"
                + "     , m.srvc_chrg_type_id"
                + "     , m.srvc_chrg_amnt"
                + "     , m.state"
                + "     , s.name"
                + "     , ss.srvc_id"
                + "  FROM mmbr_" + oper.toString().toLowerCase() + " m "
                + " INNER JOIN srvc_main s"
                + "    ON s.srvc_main_id = m.srvc_main_id"
                + " INNER JOIN srvc_sub ss"
                + "    ON m.srvc_main_id = ss.srvc_main_id"
                + "   AND ss.oper_id = " + oper.getId()
                + " WHERE m.srvc_main_id=?"
                + "   AND msisdn=?";
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, srvc_main_id);
            cp.getPreparedStatement().setString(2, msisdn);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.msisdn = msisdn;
                this.srvc_main_id = srvc_main_id;
                this.oper_id = oper.getId();
                this.ctnt_ctr = rs.getInt("ctnt_ctr");
                this.free_trial = (rs.getInt("ft") < 0 ? 0 : rs.getInt("ft"));
                this.rmdr_ctr = rs.getInt("rmdr_ctr");
                this.rchg_ctr = rs.getInt("extd_ctr");
                try {
                    this.register_date = rs.getDate("register_date");
                } catch (SQLException e) {
                    this.register_date = null;
                }
                try {
                    this.unregister_date = rs.getDate("unregister_date");
                } catch (SQLException e) {
                    this.unregister_date = null;
                }
                try {
                    this.expired_date = rs.getDate("expired_date");
                } catch (SQLException e) {
                    this.expired_date = null;
                }
                try {
                    this.balanced_date = rs.getDate("balanced_date");
                } catch (SQLException e) {
                    this.balanced_date = null;
                }

                this.state = rs.getInt("state");
                this.srvc_name = rs.getString("name");
                this.shortcode = rs.getString("srvc_id");
            } else {
                rs.close();
                throw new Exception("subscriber not found");
            }

            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Subscriber [{0},{1}, srvc_main_id:{2}] not found!!", new Object[]{msisdn, oper.name(), srvc_main_id});

            throw e;
        } finally {
            cp.release();
        }
    }

    public int getState() {
        return this.state;
    }

    public int setState(int state) {
        int row = 0;
        this.state = state;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "update mmbr_" + CARRIER.fromId(this.oper_id).toString().toLowerCase() + " SET state=?" + (state == SubscriberGroup.SUB_STATUS.UNREGISTER.getId() ? ", unregister_date=CURDATE()" : "") + (state == SubscriberGroup.SUB_STATUS.REGISTER.getId() ? ", unregister_date=NULL" : "") + " WHERE srvc_main_id=?" + " AND msisdn=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, state);
                cp.getPreparedStatement().setInt(2, this.srvc_main_id);
                cp.getPreparedStatement().setString(3, this.msisdn);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.severe(e.getMessage());
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public String getSrvc_name() {
        return this.srvc_name;
    }

    public int getSrvc_chrg_amnt() {
        return this.srvc_chrg_amnt;
    }

    public void setSrvc_chrg_amnt(int srvc_chrg_amnt) {
        this.srvc_chrg_amnt = srvc_chrg_amnt;
    }

    public int getSrvc_chrg_type_id() {
        return this.srvc_chrg_type_id;
    }

    public void setSrvc_chrg_type_id(int srvc_chrg_type_id) {
        this.srvc_chrg_type_id = srvc_chrg_type_id;
    }

    public int getNon_expired() {
        return this.non_expired;
    }

    public void setNon_expired(int non_expired) {
        this.non_expired = non_expired;
    }

    public java.util.Date getBalanced_date() {
        return this.balanced_date;
    }

    public String getBalanced_date(String format) {
        return this.balanced_date != null ? DatetimeUtil.print(format, this.balanced_date) : "";
    }

    public int setBalanced_date(java.util.Date date) {
        this.balanced_date = date;
        return setDate("balanced_date", date);
    }

    public java.util.Date getExpired_date() {
        return this.expired_date;
    }

    public String getExpired_date(String format) {
        return this.expired_date != null ? DatetimeUtil.print(format, this.expired_date) : "";
    }

    public int setExpired_date(java.util.Date date) {
        this.expired_date = date;
        return setDate("expired_date", date);
    }

    public java.util.Date getUnregister_date() {
        return this.unregister_date;
    }

    public String getUnregister_date(String format) {
        return this.unregister_date != null ? DatetimeUtil.print(format, this.unregister_date) : "";
    }

    public int setUnregister_date(java.util.Date date) {
        this.unregister_date = date;
        return setDate("unregister_date", date);
    }

    public java.util.Date getRegister_date() {
        return this.register_date;
    }

    public String getRegister_date(String format) {
        return this.register_date != null ? DatetimeUtil.print(format, this.register_date) : "";
    }

    public int setRegister_date(java.util.Date date) {
        this.register_date = date;
        return setDate("register_date", date);
    }

    private int setDate(String field, java.util.Date date) {
        int row = 0;
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "update mmbr_" + CARRIER.fromId(this.oper_id).toString().toLowerCase() + " SET " + field + "=?" + " WHERE srvc_main_id=?" + " AND msisdn=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, formatter.format(date));
                cp.getPreparedStatement().setInt(2, this.srvc_main_id);
                cp.getPreparedStatement().setString(3, this.msisdn);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.severe(e.getMessage());
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public int getRchg_ctr() {
        return this.rchg_ctr;
    }

    public void setRchg_ctr(int rchg_ctr) {
        this.rchg_ctr = rchg_ctr;
    }

    public int getRmdr_ctr() {
        return this.rmdr_ctr;
    }

    public void setRmdr_ctr(int rmdr_ctr) {
        this.rmdr_ctr = rmdr_ctr;
    }

    public int getFree_trial() {
        return this.free_trial;
    }

    public void setFree_trial(int free_trial) {
        this.free_trial = free_trial;
    }

    public int getCtnt_ctr() {
        return this.ctnt_ctr;
    }

    public void setCtnt_ctr(int ctnt_ctr) {
        this.ctnt_ctr = ctnt_ctr;
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

    public String getOper_name() {
        return this.oper_name;
    }

    public int getOper_id() {
        return this.oper_id;
    }

    public Map<String, java.util.Date> getDateKeys() {
        Map map = new HashMap();

        map.put("RegisterDate", this.register_date);
        map.put("UnregisterDate", this.unregister_date);
        map.put("ExpiredDate", this.state == SubscriberGroup.SUB_STATUS.REGISTER.getId() ? this.expired_date : new java.util.Date());
        map.put("BalancedDate", this.balanced_date);

        return map;
    }

    public String getDateKeys(String key, String format, int add) {
        java.util.Date date = (java.util.Date) getDateKeys().get(key);

        date = DatetimeUtil.add(date, 5, add);

        return DatetimeUtil.print(format, date);
    }

    private Map<String, String> getMessageKeys() {
        Map map = new HashMap();
        try {
            map.put("msisdn", this.msisdn);
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }

        return map;
    }

    public String fillVariables(String message) {
        String buff = "";

        if (message.indexOf("${") < 0) {
            return message;
        }
        try {
            String tmp;

            int pos = 0;
            int spos, epos;
            int len = message.length();
            while ((pos < len) && (message.indexOf("${", pos) >= 0)) {
                spos = message.indexOf("${", pos);
                epos = message.indexOf("}", spos);

                buff = buff + message.substring(pos, spos);

                tmp = message.substring(spos, epos + 1);

                // choose the scan pattern
                int ntokens = tmp.split(",").length;
                String pattern = "(\\w+\\s*)";
                if (ntokens == 3) {
                    pattern = "(\\w+\\s*),(\\s*[^\\cx]*\\s*),(\\s*\\d+)";
                }

                Scanner sc = new Scanner(tmp);
                try {
                    sc.findInLine(pattern);
                    MatchResult token = sc.match();

                    if (ntokens == 3) { // date variable
                        buff = buff + getDateKeys(token.group(1).trim(), token.group(2).trim(), Integer.parseInt(token.group(3).trim()));
                    } else { // generic variable
                        buff = buff + getMessageKeys().get(token.group(1).trim());
                    }
                } catch (IllegalStateException e) {
                    buff = buff + tmp;
                } finally {
                    sc.close();
                }

                pos = epos + 1;
            }

            buff = buff + message.substring(pos);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        return buff;
    }

    public SubscriptionServices.SUB_RESULT doUnsub() throws Exception {
        return new SubscriptionServices().doUnsub(this.msisdn, this.srvc_main_id, CARRIER.fromId(this.oper_id));
    }

    public int sync()
            throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE mmbr_" + CARRIER.fromId(this.oper_id).toString().toLowerCase()
                        + "    SET ctnt_ctr=?"
                        + "      , free_trial=?"
                        + "      , rmdr_ctr=?"
                        + "      , extd_ctr=?"
                        + "      , register_date=?"
                        + "      , unregister_date="
                        + (this.unregister_date != null ? DatetimeUtil.print("''yyyy-MM-dd''", this.unregister_date) : "null")
                        + "      , expired_date="
                        + (this.expired_date != null ? DatetimeUtil.print("''yyyy-MM-dd''", this.expired_date) : "null")
                        + "      , balanced_date="
                        + (this.balanced_date != null ? DatetimeUtil.print("''yyyy-MM-dd''", this.balanced_date) : "null")
                        + "      , state=?"
                        + "  WHERE msisdn="
                        + this.msisdn
                        + "    AND srvc_main_id=" + this.srvc_main_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.ctnt_ctr);
                cp.getPreparedStatement().setInt(2, this.free_trial);
                cp.getPreparedStatement().setInt(3, this.rmdr_ctr);
                cp.getPreparedStatement().setInt(4, this.rchg_ctr);
                cp.getPreparedStatement().setDate(5, new java.sql.Date(this.register_date.getTime()));
                cp.getPreparedStatement().setInt(6, this.state);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.severe(e.getMessage());
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public String print() {
        String tmp = "";

        tmp = tmp + String.format("%s|%d|%d|%d|%d|%d|%d|%s|%s|%s|%s|%d|%d|%d|%d",
                new Object[]{
                    this.msisdn, 
                    Integer.valueOf(this.srvc_main_id), 
                    Integer.valueOf(this.oper_id), 
                    Integer.valueOf(this.ctnt_ctr), 
                    Integer.valueOf(this.free_trial), 
                    Integer.valueOf(this.rmdr_ctr), 
                    Integer.valueOf(this.rchg_ctr), 
                    this.register_date != null ? DatetimeUtil.print("yyyy-MM-dd", this.register_date) : this.register_date, 
                    this.unregister_date != null ? DatetimeUtil.print("yyyy-MM-dd", this.unregister_date) : this.unregister_date, 
                    this.expired_date != null ? DatetimeUtil.print("yyyy-MM-dd", this.expired_date) : this.expired_date, 
                    this.balanced_date != null ? DatetimeUtil.print("yyyy-MM-dd HH:mm:ss", this.balanced_date) : this.balanced_date, 
                    Integer.valueOf(this.non_expired), 
                    Integer.valueOf(this.srvc_chrg_type_id), 
                    Integer.valueOf(this.srvc_chrg_amnt), 
                    Integer.valueOf(this.state)});

        return tmp;
    }
}
