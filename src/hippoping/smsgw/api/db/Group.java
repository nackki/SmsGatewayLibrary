package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class Group {

    protected int gid;
    protected String name;
    protected String homepage;
    protected String allow_pages;
    protected String block_pages;

    public String getName() {
        return this.name;
    }

    public String getHomepage() {
        return this.homepage;
    }

    public String getAllowPages() {
        return this.allow_pages;
    }

    public String getBlockPages() {
        return this.block_pages;
    }

    public Group(int gid) throws Exception {
        this.gid = gid;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   SELECT gid      , name      , IFNULL(homepage, '') AS homepage      , IFNULL(allow_pages, '') AS allow_pages      , IFNULL(block_pages, '') AS block_pages  FROM groups  WHERE gid=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, gid);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.gid = rs.getInt("gid");
                    this.name = rs.getString("name");
                    this.homepage = rs.getString("homepage");
                    this.allow_pages = rs.getString("allow_pages");
                    this.block_pages = rs.getString("block_pages");
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

    public boolean isAllowPages(String page) {
        boolean allow = false;
        try {
            allow = page.matches(this.allow_pages);
        } catch (Exception e) {
        }
        return allow;
    }

    public boolean isBlockPages(String page) {
        boolean block = false;
        try {
            block = page.matches(this.block_pages);
        } catch (Exception e) {
        }
        return block;
    }

    public static int findGroupID(String name) {
        int gid = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT gid FROM groups  WHERE name=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, name);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    gid = rs.getInt(1);
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
        return gid;
    }
}