package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class CCT {

    private static final Logger log = Logger.getLogger(CCT.class.getClass().getName());
    protected int ais_cct_id;
    protected int srvc_main_id;
    protected String register;
    protected String cancel;
    protected String warning;
    protected String recurring;
    protected String broadcast;
    protected String invalid;
    protected String charge;
    protected String cpaction_register;

    public CCT()
            throws Exception {
    }

    public CCT(int ais_cct_id)
            throws Exception {
        this.ais_cct_id = ais_cct_id;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM srvc_ais_cct  WHERE ais_cct_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, ais_cct_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.srvc_main_id = rs.getInt("srvc_main_id");
                    this.register = rs.getString("cct_register");
                    this.cancel = rs.getString("cct_cancel");
                    this.warning = rs.getString("cct_warning");
                    this.recurring = rs.getString("cct_recurring");
                    this.broadcast = rs.getString("cct_broadcast");
                    this.invalid = rs.getString("cct_invalid");
                    this.charge = rs.getString("cct_charge");
                    this.cpaction_register = rs.getString("cpaction_register");
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static CCT findBySrvcMainId(int srvc_main_id)
            throws Exception {
        CCT cct = null;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "   SELECT ais_cct_id"
                        + "   FROM srvc_ais_cct"
                        + "  WHERE srvc_main_id=?"
                        + "     OR srvc_main_id=0"
                        + "  ORDER BY srvc_main_id DESC"
                        + "  LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    cct = new CCT(rs.getInt(1));
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

        return cct;
    }

    public static int add(CCT cct) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "   INSERT INTO srvc_ais_cct"
                        + " (srvc_main_id, cct_register, cct_cancel, "
                        + "  cct_warning, cct_recurring, cct_broadcast, "
                        + "  cct_invalid, cct_charge, cpaction_register)"
                        + "  VALUES (?,?,?,?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, cct.srvc_main_id);
                cp.getPreparedStatement().setString(2, cct.register);
                cp.getPreparedStatement().setString(3, cct.cancel);
                cp.getPreparedStatement().setString(4, cct.warning);
                cp.getPreparedStatement().setString(5, cct.recurring);
                cp.getPreparedStatement().setString(6, cct.broadcast);
                cp.getPreparedStatement().setString(7, cct.invalid);
                cp.getPreparedStatement().setString(8, cct.charge);
                cp.getPreparedStatement().setString(9, cct.cpaction_register);
                row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        cct.ais_cct_id = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public static int add(Hashtable<String, String> items)
            throws Exception {
        int id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String[] int_fields = {"srvc_main_id"};

            List int_field_list = Arrays.asList(int_fields);

            if (items.isEmpty()) {
                return 0;
            }

            if ((((String) items.get("srvc_main_id")).isEmpty())
                    || (Integer.parseInt((String) items.get("srvc_main_id")) <= 0)) {
                throw new Exception("srvc_main_id should be positive numeric!!");
            }

            try {
                String FIELDS = "";
                String VALUES = "";

                Iterator iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();

                    FIELDS = FIELDS + key + (iter.hasNext() ? "," : "");
                    VALUES = VALUES + "?" + (iter.hasNext() ? "," : "");
                }

                String sql = "INSERT IGNORE INTO srvc_ais_cct ( " + FIELDS + " )" + " VALUES " + " ( " + VALUES + " )";

                cp.prepareStatement(sql, 1);

                int n = 0;
                iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    n++;
                    String key = (String) iter.next();
                    String value = (String) items.get(key);

                    if (int_field_list.contains(key)) {
                        cp.getPreparedStatement().setInt(n, Integer.parseInt(value));
                    } else {
                        cp.getPreparedStatement().setString(n, value);
                    }

                }

                int rows = cp.execUpdatePrepareStatement();
                if (rows == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return id;
    }

    public int remove() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM srvc_ais_cct WHERE ais_cct_id=" + this.ais_cct_id;

                rows = cp.execUpdate(sql);

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "ais cct {0} row(s) removed.", Integer.valueOf(rows));
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = 
                        "   UPDATE srvc_ais_cct"
                        + "    SET cct_register=?"
                        + "      , cct_cancel=?"
                        + "      , cct_warning=?"
                        + "      , cct_recurring=?"
                        + "      , cct_broadcast=?"
                        + "      , cct_invalid=?"
                        + "      , cct_charge=?"
                        + "      , cpaction_register=?"
                        + "  WHERE ais_cct_id=" + this.ais_cct_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.register);
                cp.getPreparedStatement().setString(2, this.cancel);
                cp.getPreparedStatement().setString(3, this.warning);
                cp.getPreparedStatement().setString(4, this.recurring);
                cp.getPreparedStatement().setString(5, this.broadcast);
                cp.getPreparedStatement().setString(6, this.invalid);
                cp.getPreparedStatement().setString(7, this.charge);
                cp.getPreparedStatement().setString(8, this.cpaction_register);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public static int sync(Hashtable<String, String> items)
            throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String[] int_fields = {"srvc_main_id"};

            if (items.isEmpty()) {
                return 0;
            }

            try {
                String VALUES = "";
                Iterator iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    VALUES = VALUES + (String) iter.next() + "= ?" + (iter.hasNext() ? "," : "");
                }

                String sql = 
                        " UPDATE srvc_ais_cct"
                        + "  SET " 
                        + VALUES 
                        + " WHERE srvc_main_id=" + (String) items.get("srvc_main_id");

                cp.prepareStatement(sql);

                iter = items.keySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    String value = (String) items.get((String) iter.next());
                    if ((value != null) && (value.isEmpty())) {
                        value = null;
                    }
                    cp.getPreparedStatement().setString(++i, value);
                }

                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public String getBroadcast() {
        return this.broadcast;
    }

    public String getCancel() {
        return this.cancel;
    }

    public String getCharge() {
        return this.charge;
    }

    public String getInvalid() {
        return this.invalid;
    }

    public String getRecurring() {
        return this.recurring;
    }

    public String getRegister() {
        return this.register;
    }

    public String getWarning() {
        return this.warning;
    }

    public String getCpaction_register() {
        return cpaction_register;
    }
}