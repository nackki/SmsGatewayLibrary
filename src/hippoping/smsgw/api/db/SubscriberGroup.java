package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class SubscriberGroup {

    public static String[] sub_status_detail = {"new", "recharging", "active", "remove", "removing", "inactive"};
    private List<Subscriber> msisdnList;
    private String register_date;
    private String expired_date;
    protected int srvc_main_id;

    public SubscriberGroup() {
        this.msisdnList = new ArrayList();
    }

    public SubscriberGroup(int srvc_main_id) {
        this.srvc_main_id = srvc_main_id;
        this.msisdnList = new ArrayList();
    }

    public SubscriberGroup(int srvc_main_id, String oper_name) throws Exception {
        this.srvc_main_id = srvc_main_id;
        this.msisdnList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    this.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "create sub group failed!!");
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public SubscriberGroup(int srvc_main_id, String oper_name, SUB_STATUS status) throws Exception {
        this.srvc_main_id = srvc_main_id;
        this.msisdnList = new ArrayList();
        try {
            String wherestatus = "";
            if (status != null) {
                wherestatus = " AND state=" + status.getId();
            }

            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id=?" + wherestatus;
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    this.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "create sub group failed!!");
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public SubscriberGroup(int srvc_main_id, String oper_name, String expired_date, SUB_STATUS status, int non_expired) throws Exception {
        this.srvc_main_id = srvc_main_id;
        this.expired_date = expired_date;
        this.msisdnList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id=?" + "   AND expired_date = ?" + "   AND state = ?" + "   AND non_expired = ?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, expired_date);
                cp.getPreparedStatement().setInt(3, status.getId());
                cp.getPreparedStatement().setInt(4, non_expired);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    this.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "create sub group failed!!");
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public SubscriberGroup(String register_date, int srvc_main_id, String oper_name) throws Exception {
        this.srvc_main_id = srvc_main_id;
        this.register_date = register_date;
        this.msisdnList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id=?" + "   AND register_date = ?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, register_date);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    this.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "create sub group failed!!");
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public SubscriberGroup(String register_date, int srvc_main_id, String oper_name, SUB_STATUS status) throws Exception {
        this.srvc_main_id = srvc_main_id;
        this.register_date = register_date;
        this.msisdnList = new ArrayList();
        DBPoolManager cp = new DBPoolManager();

        String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id=?" + "   AND register_date = ?" + "   AND state = ?";
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, srvc_main_id);
            cp.getPreparedStatement().setString(2, register_date);
            cp.getPreparedStatement().setInt(3, status.getId());
            ResultSet rs = cp.execQueryPrepareStatement();
            while (rs.next()) {
                this.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
            }

            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "create sub group failed!!");
        } finally {
            cp.release();
        }
    }

    public static SubscriberGroup getWarningSubGroup(int srvc_main_id, String oper_name, String expired_date, SUB_STATUS status, int non_expired) throws Exception {
        SubscriberGroup sg = new SubscriberGroup(srvc_main_id);

        sg.expired_date = DatetimeUtil.changeDateFormat(expired_date, "yyyy-MM-dd", "dd/MM/yy");
        sg.msisdnList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE CURDATE() = DATE_SUB(expired_date, INTERVAL rmdr_ctr DAY)" + "   AND srvc_main_id=?" + "   AND expired_date = ?" + "   AND state = ?" + "   AND non_expired = ?" + "   AND rmdr_ctr > 0";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, expired_date);
                cp.getPreparedStatement().setInt(3, status.getId());
                cp.getPreparedStatement().setInt(4, non_expired);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    sg.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return sg;
    }

    public static SubscriberGroup getRecurringSubGroup(int srvc_main_id, String oper_name, String expired_date, int non_expired) throws Exception {
        SubscriberGroup sg = new SubscriberGroup(srvc_main_id);

        sg.expired_date = expired_date;
        sg.msisdnList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id = ?" + "   AND expired_date = ?" + "   AND (state = " + SUB_STATUS.REGISTER.getId() + " or state = " + SUB_STATUS.PREPARE2REGISTER.getId() + ")" + "   AND non_expired = ?" + "   AND extd_ctr > 0";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, expired_date);
                cp.getPreparedStatement().setInt(3, non_expired);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    sg.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, OperConfig.CARRIER.valueOf(oper_name.toUpperCase())));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return sg;
    }

    public static SubscriberGroup getSyncSubGroup(int srvc_main_id, OperConfig.CARRIER oper) throws Exception {
        return getSyncSubGroup(srvc_main_id, oper, null);
    }

    public static SubscriberGroup getSyncSubGroup(int srvc_main_id, OperConfig.CARRIER oper, SUB_STATUS status) throws Exception {
        SubscriberGroup sg = new SubscriberGroup(srvc_main_id);

        sg.msisdnList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT msisdn  FROM mmbr_" + oper.toString().toLowerCase() + " WHERE srvc_main_id = ?" + (status == null ? "" : new StringBuilder().append("   AND state = ").append(status.getId()).toString());
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    sg.msisdnList.add(new Subscriber(rs.getString(1), srvc_main_id, oper));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return sg;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public void setSrvc_main_id(int srvc_main_id) {
        this.srvc_main_id = srvc_main_id;
    }

    public String getRegisterDate() {
        return this.register_date;
    }

    public void setRegisterDate(String date) {
        this.register_date = date;
    }

    public String getExpiredDate() {
        return this.expired_date;
    }

    public void setExpiredDate(String date) {
        this.expired_date = date;
    }

    public void setMsisdnList(Subscriber[] list) {
        this.msisdnList = new ArrayList(Arrays.asList(list));
    }

    public Subscriber[] getMsisdnList() {
        return (Subscriber[]) this.msisdnList.toArray(new Subscriber[0]);
    }

    private boolean isValid(String msisdn) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "check duplicated for " + msisdn);
        if (this.msisdnList == null) {
            return false;
        }
        if (this.msisdnList.size() == 0) {
            return false;
        }

        boolean dup = Boolean.FALSE.booleanValue();
        for (int i = 0; i < this.msisdnList.size(); i++) {
            if (((Subscriber) this.msisdnList.get(i)).getMsisdn().equals(msisdn)) {
                dup = Boolean.TRUE.booleanValue();
                break;
            }
        }
        return dup;
    }

    public boolean add(Subscriber sub) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "add msisdn:" + sub.getMsisdn());
        if (isValid(sub.getMsisdn())) {
            return Boolean.FALSE.booleanValue();
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "not duplicate");
        return this.msisdnList.add(sub);
    }

    public void remove(String msisdn) {
        this.msisdnList.removeAll(Arrays.asList(new String[]{msisdn}));
    }

    public static enum SUB_STATUS {

        INVALID(-1),
        NEW(0),
        PREPARE2REGISTER(1),
        REGISTER(2),
        DELETE(3),
        PREPARE2UNREGISTER(4),
        UNREGISTER(5),
        BARRING(7);
        private final int id;

        private SUB_STATUS(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static SUB_STATUS fromId(int id) {
            for (SUB_STATUS e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}