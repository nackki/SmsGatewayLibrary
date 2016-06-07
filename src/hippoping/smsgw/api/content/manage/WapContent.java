/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.content.manage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

/**
 *
 * @author ITZONE
 */
public class WapContent {

    protected int wap_push_id;
    protected String url_jar;
    protected String url_jad;
    protected String title;

    public WapContent() {
    }

    public WapContent(int wap_push_id) throws Exception {
        this.wap_push_id = 0;

        String sql =
                "  SELECT *"
                + "  FROM ctnt_sms_wap"
                + " WHERE disposable=0"
                + "   AND wap_push_id=?";

        // get connection pool
        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, wap_push_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.wap_push_id = rs.getInt("wap_push_id");
                this.url_jar = rs.getString("url_jar");
                this.url_jad = rs.getString("url_jad");
                this.title = rs.getString("title");
            } else {
                throw new Exception("wap content not found");
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public int sync() throws Exception {
        int rows = 0;

        try {
            DBPoolManager cp = new DBPoolManager();

            try {
                String sql =
                        "   UPDATE ctnt_sms_wap"
                        + "    SET url_jar=?"
                        + "      , url_jad=?"
                        + "      , title=?"
                        + "  WHERE wap_push_id=" + this.wap_push_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.url_jar);
                cp.getPreparedStatement().setString(2, this.url_jad);
                cp.getPreparedStatement().setString(3, this.title);
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

    public int add(WapContent content) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();

            try {
                String sql =
                        "INSERT INTO ctnt_sms_wap "
                        + "     (url_jar, url_jad, disposable, title)"
                        + " VALUES (?,?,0,?)";

                cp.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                cp.getPreparedStatement().setString(1, this.url_jar);
                cp.getPreparedStatement().setString(2, this.url_jad);
                cp.getPreparedStatement().setString(3, this.title);
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        content.wap_push_id = rs.getInt(1);
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

        return content.wap_push_id;
    }

    /**
     * get all service title map group by service
     * @return
     */
    public static List<WapContent> getAll() {
        List<WapContent> list = new ArrayList<WapContent>();
        try {
            DBPoolManager cp = new DBPoolManager();

            try {
                String sql =
                        "  SELECT wap_push_id"
                        + "  FROM ctnt_sms_wap"
                        + " WHERE disposable=0"
                        + " ORDER BY wap_push_id ASC";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    WapContent content = null;
                    try {
                        content = new WapContent(rs.getInt(1));
                        list.add(content);
                    } catch (Exception e) {}
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

                String sql =
                        "DELETE FROM ctnt_sms_wap"
                        + " WHERE wap_push_id=" + this.wap_push_id;

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

    public String getTitle() {
        return title;
    }

    public String getUrl_jad() {
        return url_jad;
    }

    public String getUrl_jar() {
        return url_jar;
    }

    public int getWap_push_id() {
        return wap_push_id;
    }
}
