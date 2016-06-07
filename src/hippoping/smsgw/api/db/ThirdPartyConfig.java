package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class ThirdPartyConfig {

    protected int ctnt_3rdp_id;
    protected String company_name;
    protected String url;
    protected String auth_type;
    protected String user;
    protected String password;
    protected String method;

    public ThirdPartyConfig(int id)
            throws Exception {
        this.ctnt_3rdp_id = id;

        String sql = "  SELECT *   FROM ctnt_3rd_prty  WHERE ctnt_3rdp_id=?";

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, id);
            ResultSet rs = cp.execQueryPrepareStatement();
            try {
                if (rs.next()) {
                    this.company_name = rs.getString("cpny_name");
                    this.url = rs.getString("url");
                    this.auth_type = rs.getString("auth_type");
                    this.user = (rs.getString("user") == null ? "" : rs.getString("user"));
                    this.password = (rs.getString("password") == null ? "" : rs.getString("password"));
                    this.method = (rs.getString("method") == null ? "POST" : rs.getString("method"));
                } else {
                    throw new Exception("3rd party not found");
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error", e);
            throw e;
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
    }

    public ThirdPartyConfig() {
    }

    public ThirdPartyConfig(String company_name, String url, String auth_type, String user, String password, String method) {
        this.company_name = company_name;
        this.url = url;
        this.auth_type = auth_type;
        this.user = user;
        this.password = password;
        this.method = method;
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE ctnt_3rd_prty    SET cpny_name=?      , url=?      , auth_type=?      , user=?      , password=?      , method=?  WHERE ctnt_3rdp_id=" + this.ctnt_3rdp_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.company_name);
                cp.getPreparedStatement().setString(2, this.url);
                cp.getPreparedStatement().setString(3, this.auth_type);
                cp.getPreparedStatement().setString(4, this.user);
                cp.getPreparedStatement().setString(5, this.password);
                cp.getPreparedStatement().setString(6, this.method);
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

    public static int add(ThirdPartyConfig tpc) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   INSERT INTO ctnt_3rd_prty  VALUES (NULL, ?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setString(1, tpc.company_name);
                cp.getPreparedStatement().setString(2, tpc.url);
                cp.getPreparedStatement().setString(3, tpc.auth_type);
                cp.getPreparedStatement().setString(4, tpc.user);
                cp.getPreparedStatement().setString(5, tpc.password);
                cp.getPreparedStatement().setString(6, tpc.method);
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        tpc.ctnt_3rdp_id = rs.getInt(1);
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

        return tpc.ctnt_3rdp_id;
    }

    public static List<ThirdPartyConfig> getAll() {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "  SELECT ctnt_3rdp_id  FROM ctnt_3rd_prty";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(new ThirdPartyConfig(rs.getInt(1)));
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
                String sql = "DELETE FROM ctnt_3rd_prty WHERE ctnt_3rdp_id=" + this.ctnt_3rdp_id;

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

    public void setAuth_type(String auth_type) {
        this.auth_type = auth_type;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAuth_type() {
        return this.auth_type;
    }

    public String getCompany_name() {
        return this.company_name;
    }

    public int getId() {
        return this.ctnt_3rdp_id;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUser() {
        return this.user;
    }

    public String getMethod() {
        return this.method;
    }
}