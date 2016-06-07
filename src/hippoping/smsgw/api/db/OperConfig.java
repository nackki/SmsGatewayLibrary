package hippoping.smsgw.api.db;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class OperConfig
        implements Serializable {

    public int conf_id;
    public String conf_name;
    public String user;
    public String password;
    public String register_url;
    public String unregister_url;
    public String sub_stat_url;
    public String sms_link_url;
    public String mms_link_url;
    public String ivr_link_url;
    public String thrd_prty_url;
    public String thrd_prty_auth;
    public int oper_id;
    public int use_default;
    public String sftp_cust;
    public String sftp_host;
    public int sftp_port;
    public String sftp_user;
    public String sftp_password;
    public String sftp_remote_dir;
    protected int srvc_main_id;
    public HybridConfig hybrid;

    public OperConfig() {
    }

    public OperConfig(int srvc_main_id, CARRIER oper)
            throws Exception {
        this.srvc_main_id = srvc_main_id;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "  SELECT c.*  FROM conf_link AS c INNER JOIN srvc_sub AS s    ON c.conf_id = s.conf_id   AND s.srvc_main_id=?   AND s.oper_id=? LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, srvc_main_id);
            cp.getPreparedStatement().setInt(2, oper.getId());

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.conf_id = rs.getInt("conf_id");
                this.conf_name = rs.getString("conf_name");
                this.user = rs.getString("user");
                this.password = rs.getString("password");
                this.register_url = rs.getString("register_url");
                this.unregister_url = rs.getString("unregister_url");
                this.sub_stat_url = rs.getString("sub_stat_url");
                this.sms_link_url = rs.getString("sms_link_url");
                this.mms_link_url = rs.getString("mms_link_url");
                this.ivr_link_url = rs.getString("ivr_link_url");
                this.thrd_prty_url = rs.getString("thrd_prty_url");
                this.thrd_prty_auth = rs.getString("thrd_prty_auth");

                this.sftp_cust = rs.getString("sftp_cust");
                this.sftp_host = rs.getString("sftp_host");
                this.sftp_port = rs.getInt("sftp_port");
                this.sftp_user = rs.getString("sftp_user");
                this.sftp_password = rs.getString("sftp_password");
                this.sftp_remote_dir = rs.getString("sftp_remote_dir");

                this.use_default = rs.getInt("default_flg");
                this.oper_id = oper.getId();

                this.hybrid = new HybridConfig(rs.getInt("link_hybd_id"));
            } else {
                throw new Exception("there are no config for service main ID:" + srvc_main_id + ", " + oper.name() + "!!");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
    }

    public OperConfig(int conf_id) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM conf_link WHERE conf_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, conf_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.conf_id = rs.getInt("conf_id");
                    this.conf_name = rs.getString("conf_name");
                    this.user = rs.getString("user");
                    this.password = rs.getString("password");
                    this.register_url = rs.getString("register_url");
                    this.unregister_url = rs.getString("unregister_url");
                    this.sub_stat_url = rs.getString("sub_stat_url");
                    this.sms_link_url = rs.getString("sms_link_url");
                    this.mms_link_url = rs.getString("mms_link_url");
                    this.ivr_link_url = rs.getString("ivr_link_url");
                    this.thrd_prty_url = rs.getString("thrd_prty_url");
                    this.thrd_prty_auth = rs.getString("thrd_prty_auth");

                    this.sftp_cust = rs.getString("sftp_cust");
                    this.sftp_host = rs.getString("sftp_host");
                    this.sftp_port = rs.getInt("sftp_port");
                    this.sftp_user = rs.getString("sftp_user");
                    this.sftp_password = rs.getString("sftp_password");
                    this.sftp_remote_dir = rs.getString("sftp_remote_dir");

                    this.use_default = rs.getInt("default_flg");

                    this.hybrid = new HybridConfig(rs.getInt("link_hybd_id"));
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

    public static CARRIER whichOper(String msisdn) {
        CARRIER oper = null;

        String wherenumber = "";

        if (msisdn.startsWith("66")) {
            msisdn = msisdn.replaceFirst("66", "");
        }

        if (msisdn.isEmpty()) {
            System.err.print("Blank msisdn is not allowed!!");
            return oper;
        }

        int len = msisdn.length();
        for (int i = len; i > 0; i--) {
            wherenumber = wherenumber + " OR number = '" + msisdn.substring(0, i) + "'";
        }
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "  SELECT oper_id  FROM nmbr_plan WHERE status=1   AND (0" + wherenumber + "     )" + " ORDER BY LENGTH(number) DESC" + " LIMIT 1";

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    oper = CARRIER.fromId(rs.getInt(1));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return oper;
    }

    public static boolean isOper(int oper_id, String oper_name) {
        return CARRIER.valueOf(oper_name.toUpperCase()).getId() == oper_id;
    }

    public static int getOperId(String name) {
        return CARRIER.valueOf(name.toUpperCase()).getId();
    }

    public static int getOperId(String oper_name, String platform) {
        int res = -1;

        String sql = "SELECT oper_id  FROM conf_oper  WHERE oper_name=?   and platform=?;";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setString(1, oper_name);
                cp.getPreparedStatement().setString(2, platform);

                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    res = rs.getInt("oper_id");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return res;
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE conf_link    SET conf_name=?      , user=?      , password=?      , register_url=?      , unregister_url=?      , sub_stat_url=?      , sms_link_url=?      , mms_link_url=?      , ivr_link_url=?      , thrd_prty_url=?      , thrd_prty_auth=?      , sftp_cust=?      , sftp_host=?      , sftp_port=?      , sftp_user=?      , sftp_password=?      , sftp_remote_dir=?      , link_hybd_id=?  WHERE conf_id=" + this.conf_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.conf_name);
                cp.getPreparedStatement().setString(2, this.user);
                cp.getPreparedStatement().setString(3, this.password);
                cp.getPreparedStatement().setString(4, this.register_url);
                cp.getPreparedStatement().setString(5, this.unregister_url);
                cp.getPreparedStatement().setString(6, this.sub_stat_url);
                cp.getPreparedStatement().setString(7, this.sms_link_url);
                cp.getPreparedStatement().setString(8, this.mms_link_url);
                cp.getPreparedStatement().setString(9, this.ivr_link_url);
                cp.getPreparedStatement().setString(10, this.thrd_prty_url);
                cp.getPreparedStatement().setString(11, this.thrd_prty_auth);
                cp.getPreparedStatement().setString(12, this.sftp_cust);
                cp.getPreparedStatement().setString(13, this.sftp_host);
                cp.getPreparedStatement().setInt(14, this.sftp_port);
                cp.getPreparedStatement().setString(15, this.sftp_user);
                cp.getPreparedStatement().setString(16, this.sftp_password);
                cp.getPreparedStatement().setString(17, this.sftp_remote_dir);
                cp.getPreparedStatement().setInt(18, this.hybrid != null ? this.hybrid.getLink_hybd_id() : 0);
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

    public static int add(OperConfig link) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   INSERT INTO conf_link (conf_name, user, password, register_url, unregister_url, sub_stat_url  , sms_link_url, mms_link_url, ivr_link_url, thrd_prty_url, thrd_prty_auth  , sftp_cust, sftp_host, sftp_port, sftp_user, sftp_password, sftp_remote_dir  , link_hybd_id)  VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setString(1, link.conf_name);
                cp.getPreparedStatement().setString(2, link.user);
                cp.getPreparedStatement().setString(3, link.password);
                cp.getPreparedStatement().setString(4, link.register_url);
                cp.getPreparedStatement().setString(5, link.unregister_url);
                cp.getPreparedStatement().setString(6, link.sub_stat_url);
                cp.getPreparedStatement().setString(7, link.sms_link_url);
                cp.getPreparedStatement().setString(8, link.mms_link_url);
                cp.getPreparedStatement().setString(9, link.ivr_link_url);
                cp.getPreparedStatement().setString(10, link.thrd_prty_url);
                cp.getPreparedStatement().setString(11, link.thrd_prty_auth);
                cp.getPreparedStatement().setString(12, link.sftp_cust);
                cp.getPreparedStatement().setString(13, link.sftp_host);
                cp.getPreparedStatement().setInt(14, link.sftp_port);
                cp.getPreparedStatement().setString(15, link.sftp_user);
                cp.getPreparedStatement().setString(16, link.sftp_password);
                cp.getPreparedStatement().setString(17, link.sftp_remote_dir);
                cp.getPreparedStatement().setInt(18, link.hybrid != null ? link.hybrid.getLink_hybd_id() : 0);
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        link.conf_id = rs.getInt(1);
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

        return link.conf_id;
    }

    public static List<OperConfig> getAll() {
        return getAll(null);
    }

    public static List<OperConfig> getAll(String search) {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheresearch = "";
            if ((search != null) && (!search.isEmpty())) {
                wheresearch = " AND conf_name LIKE '%" + search + "%'";
            }
            try {
                String sql = "  SELECT conf_id  FROM conf_link WHERE 1" + wheresearch;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(new OperConfig(rs.getInt(1)));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            return null;
        }

        return list;
    }

    public int remove() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM conf_link WHERE conf_id=" + this.conf_id;

                rows = cp.execUpdate(sql);
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

    public static enum CARRIER {

        ALL(0),
        DTAC(1),
        TRUE(2),
        AIS_LEGACY(3),
        AIS(4),
        TRUEH(5),
        DTAC_SDP(6),
        CAT(7),
        PARTNER(1024);
        private final int id;

        private CARRIER(int id) {
            this.id = id;
        }

        public static int length() {
            return CAT.getId();
        }

        public int getId() {
            return this.id;
        }

        public static CARRIER fromId(int id) {
            for (CARRIER e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        @Override
        /**
         * to DB table name
         */
        public String toString() {
            if ((equals(AIS)) || (equals(AIS_LEGACY))) {
                return AIS.name();
            }
            if ((equals(DTAC)) || (equals(DTAC_SDP))) {
                return DTAC.name();
            }
            return name();
        }
    }
}