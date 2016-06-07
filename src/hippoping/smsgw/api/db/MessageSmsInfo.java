package hippoping.smsgw.api.db;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import lib.common.DBPoolManager;

public class MessageSmsInfo implements Serializable {

    protected int sms_info_id;
    protected String category = "";
    protected String owner = "";
    protected String purpose = "";
    protected String keywords = "";
    protected Date issue_date;
    protected String title = "";

    public MessageSmsInfo() {
    }

    public MessageSmsInfo(int sms_info_id) throws Exception {
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT  * FROM ctnt_sms_info WHERE  sms_info_id = ? ";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, sms_info_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.sms_info_id = rs.getInt("sms_info_id");
                this.category = rs.getString("category");
                this.owner = rs.getString("owner");
                this.purpose = rs.getString("purpose");
                this.keywords = rs.getString("keywords");
                this.issue_date = rs.getDate("issue_date");
                this.title = rs.getString("title");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
    }

    public int getSms_info_id() {
        return this.sms_info_id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String type) {
        this.title = type;
    }

    public Date getIssue_date() {
        return this.issue_date;
    }

    public void setIssue_date(Date issue_date) {
        this.issue_date = issue_date;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public static int add(MessageSmsInfo info) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   INSERT INTO ctnt_sms_info  VALUES (NULL, ?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setString(1, info.getCategory());
                cp.getPreparedStatement().setString(2, info.getOwner());
                cp.getPreparedStatement().setString(3, info.getPurpose());
                cp.getPreparedStatement().setString(4, info.getKeywords());
                cp.getPreparedStatement().setTimestamp(5, new Timestamp(info.getIssue_date().getTime()));
                cp.getPreparedStatement().setString(6, info.getTitle());
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        info.sms_info_id = rs.getInt(1);
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

        return info.sms_info_id;
    }

    public int remove() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM ctnt_sms_info WHERE sms_info_id=" + this.sms_info_id;

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
}