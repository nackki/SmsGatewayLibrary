package hippoping.smsgw.api.db;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class AisLegacyCommand
        implements Serializable {

    private static final Logger logger = Logger.getLogger(AisLegacyCommand.class.getClass().getName());
    protected int srvc_main_id;
    protected String mt_chrg_cmd;
    protected String mt_non_chrg_cmd;
    protected String mt_warn_cmd;
    protected String mt_sub_cmd;
    protected String mt_unsub_cmd;

    public AisLegacyCommand(int srvc_main_id)
            throws Exception {
        this.srvc_main_id = srvc_main_id;

        String sql = "SELECT *  FROM srvc_ais_lgcy_cmd WHERE srvc_main_id=" + srvc_main_id;

        DBPoolManager cp = new DBPoolManager();
        try {
            ResultSet rs = cp.execQuery(sql);
            if (rs.next()) {
                this.mt_chrg_cmd = rs.getString("mt_chrg_cmd");
                this.mt_non_chrg_cmd = rs.getString("mt_non_chrg_cmd");
                this.mt_warn_cmd = rs.getString("mt_warn_cmd");
                this.mt_sub_cmd = rs.getString("mt_sub_cmd");
                this.mt_unsub_cmd = rs.getString("mt_unsub_cmd");
            } else {
                throw new Exception("AIS Legacy command for service main ID " + srvc_main_id + " not found!!");
            }
            rs.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error!!", e);
            throw e;
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
    }

    public static int add(AisLegacyCommand cmd) throws Exception {
        int rows = 0;

        String sql = "INSERT INTO srvc_ais_lgcy_cmd (srvc_main_id, mt_chrg_cmd, mt_non_chrg_cmd, mt_warn_cmd, mt_sub_cmd, mt_unsub_cmd) VALUES (?,?,?,?,?,?)";

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, cmd.srvc_main_id);
            cp.getPreparedStatement().setString(2, cmd.mt_chrg_cmd);
            cp.getPreparedStatement().setString(3, cmd.mt_non_chrg_cmd);
            cp.getPreparedStatement().setString(4, cmd.mt_warn_cmd);
            cp.getPreparedStatement().setString(5, cmd.mt_sub_cmd);
            cp.getPreparedStatement().setString(6, cmd.mt_unsub_cmd);

            rows = cp.execUpdatePrepareStatement();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error!!", e);
            throw e;
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }

        return rows;
    }

    public static int add(Hashtable<String, String> items)
            throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String[] int_fields = {"srvc_main_id"};

            List int_field_list = Arrays.asList(int_fields);

            if (items.isEmpty()) {
                return 0;
            }

            if ((((String) items.get("srvc_main_id")).isEmpty()) || (Integer.parseInt((String) items.get("srvc_main_id")) <= 0)) {
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

                String sql = "INSERT IGNORE INTO srvc_ais_lgcy_cmd ( " + FIELDS + " )" + " VALUES " + " ( " + VALUES + " )";

                cp.prepareStatement(sql);

                int n = 0;
                iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    n++;
                    String key = (String) iter.next();
                    String value = (String) items.get(key);

                    if (int_field_list.contains(key)) {
                        cp.getPreparedStatement().setInt(n, Integer.parseInt(value));
                    } else if (value.isEmpty()) {
                        cp.getPreparedStatement().setNull(n, 12);
                    } else {
                        cp.getPreparedStatement().setString(n, value);
                    }

                }

                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
                throw e;
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

                String sql = "UPDATE srvc_ais_lgcy_cmd  SET " + VALUES + " WHERE srvc_main_id=" + (String) items.get("srvc_main_id");

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
                logger.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public String getMt_unsub_cmd() {
        return this.mt_unsub_cmd;
    }

    public String getMt_sub_cmd() {
        return this.mt_sub_cmd;
    }

    public String getMt_warn_cmd() {
        return this.mt_warn_cmd;
    }

    public String getMt_non_chrg_cmd() {
        return this.mt_non_chrg_cmd;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public String getMt_chrg_cmd() {
        return this.mt_chrg_cmd;
    }
}