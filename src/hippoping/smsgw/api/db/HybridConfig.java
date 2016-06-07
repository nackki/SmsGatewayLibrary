package hippoping.smsgw.api.db;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lib.common.DBPoolManager;

public class HybridConfig
        implements Serializable {

    protected int link_hybd_id;
    protected String name;
    protected String url;
    protected String user;
    protected String password;
    protected SGWID sgwid;

    public HybridConfig() {
    }

    public HybridConfig(int link_hybd_id)
            throws Exception {
        this.link_hybd_id = link_hybd_id;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "  SELECT *  FROM conf_link_hybd WHERE link_hybd_id=? LIMIT 1";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, link_hybd_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.name = rs.getString("name");
                this.user = rs.getString("user");
                this.password = rs.getString("password");
                this.url = rs.getString("url");
                this.sgwid = SGWID.fromId(rs.getString("sgwid"));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE conf_link_hybd    SET name=?      , user=?      , password=?      , url=?      , sgwid=?  WHERE link_hybd_id=" + this.link_hybd_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.name);
                cp.getPreparedStatement().setString(2, this.user);
                cp.getPreparedStatement().setString(3, this.password);
                cp.getPreparedStatement().setString(4, this.url);
                cp.getPreparedStatement().setString(5, this.sgwid.getId());
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

    public static int add(HybridConfig hb) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   INSERT INTO conf_link_hybd (name, user, password, url, sgwid)  VALUES (?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setString(1, hb.name);
                cp.getPreparedStatement().setString(2, hb.user);
                cp.getPreparedStatement().setString(3, hb.password);
                cp.getPreparedStatement().setString(4, hb.url);
                cp.getPreparedStatement().setString(5, hb.sgwid.getId());
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        hb.link_hybd_id = rs.getInt(1);
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

        return hb.link_hybd_id;
    }

    public static List<HybridConfig> getAll() {
        return getAll(null);
    }

    public static List<HybridConfig> getAll(String search) {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheresearch = "";
            if ((search != null) && (!search.isEmpty())) {
                wheresearch = " AND name LIKE '%" + search + "%'";
            }
            try {
                String sql = "  SELECT link_hybd_id  FROM conf_link_hybd WHERE 1" + wheresearch;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(new HybridConfig(rs.getInt(1)));
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
                String sql = "DELETE FROM conf_link_hybd WHERE link_hybd_id=" + this.link_hybd_id;

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

    public int getLink_hybd_id() {
        return this.link_hybd_id;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public SGWID getSgwid() {
        return this.sgwid;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUser() {
        return this.user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSgwid(SGWID sgwid) {
        this.sgwid = sgwid;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static enum SGWID {

        AD_Send2Friend("051"),
        AD_IVR("052"),
        CC_Send2Friend("061"),
        CC_IVR("062"),
        CC_WAPx("063");
        private final String id;

        private SGWID(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public static SGWID fromId(String id) {
            for (SGWID e : values()) {
                if (e.id.equals(id)) {
                    return e;
                }
            }
            return null;
        }
    }
}