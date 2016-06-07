/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.content.manage;

import hippoping.smsgw.api.db.Message.CHARACTER_TYPE;
import hippoping.smsgw.api.db.Message.SMS_TYPE;
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
public class TextContent {

    protected int sms_mesg_id;
    protected CHARACTER_TYPE encoding;
    protected String content;

    public TextContent() {
    }

    public TextContent(int sms_mesg_id) throws Exception {
        this.sms_mesg_id = 0;

        String sql =
                "  SELECT *"
                + "  FROM ctnt_sms_mesg"
                + " WHERE type=" + SMS_TYPE.TEXT.getId()
                + "   AND disposable=0"
                + "   AND sms_mesg_id=?";

        // get connection pool
        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, sms_mesg_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.sms_mesg_id = rs.getInt("sms_mesg_id");
                this.encoding = CHARACTER_TYPE.fromId(rs.getInt("encoding"));
                this.content = rs.getString("content");
            } else {
                throw new Exception("text content not found");
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
                        "   UPDATE ctnt_sms_mesg"
                        + "    SET content=?"
                        + "      , encoding=?"
                        + "  WHERE sms_mesg_id=" + this.sms_mesg_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.content);
                cp.getPreparedStatement().setInt(2, this.encoding.getId());
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

    public int add(TextContent content) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();

            try {
                String sql =
                        "INSERT INTO ctnt_sms_mesg "
                        + "     (type, content, disposable, encoding)"
                        + " VALUES (?,?,0,?)";

                cp.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                cp.getPreparedStatement().setInt(1, SMS_TYPE.TEXT.getId());
                cp.getPreparedStatement().setString(2, content.content);
                cp.getPreparedStatement().setInt(3, content.encoding.getId());
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        content.sms_mesg_id = rs.getInt(1);
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

        return content.sms_mesg_id;
    }

    /**
     * get all service content map group by service
     * @return
     */
    public static List<TextContent> getAll() {
        List<TextContent> list = new ArrayList<TextContent>();
        try {
            DBPoolManager cp = new DBPoolManager();

            try {
                String sql =
                        "  SELECT sms_mesg_id"
                        + "  FROM ctnt_sms_mesg"
                        + " WHERE type=" + SMS_TYPE.TEXT.getId()
                        + "   AND disposable=0"
                        + " ORDER BY sms_mesg_id ASC";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    TextContent content = null;
                    try {
                        content = new TextContent(rs.getInt(1));
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
                        "DELETE FROM ctnt_sms_mesg"
                        + " WHERE sms_mesg_id=" + this.sms_mesg_id;

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

    public String getContent() {
        return content;
    }

    public CHARACTER_TYPE getEncoding() {
        return encoding;
    }

    public int getSms_mesg_id() {
        return sms_mesg_id;
    }
}
