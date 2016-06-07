package hippoping.smsgw.api.db;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class MessageMmsSubContent {

    private static final Logger log = Logger.getLogger(MessageMmsSubContent.class.getClass().getName());
    private int content_id;
    private int mms_mesg_id;
    private int sub_order_number;
    private Message.SMS_TYPE ctnt_type;
    private String full_path_src;

    public MessageMmsSubContent(int id)
            throws Exception {
        this.content_id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT *  FROM ctnt_mms_sub_ctnt WHERE mms_sub_ctnt_id = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    while (rs.next()) {
                        if (this.content_id == -1) {
                            this.content_id = id;
                        }

                        this.full_path_src = rs.getString("full_path_src");
                        this.sub_order_number = rs.getInt("sub_ordr_num");
                        this.ctnt_type = Message.SMS_TYPE.fromId(rs.getInt("ctnt_type"));
                        this.mms_mesg_id = rs.getInt("mms_mesg_id");
                    }
                } finally {
                    rs.close();
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public int editSubContentOrderNumber(int mms_sub_ctnt_id, int order_num) throws Exception {
        int row = 0;

        String sql = "UPDATE ctnt_mms_sub_ctnt SET sub_ordr_num=? WHERE mms_sub_ctnt_id=?";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, order_num);
                cp.getPreparedStatement().setInt(2, mms_sub_ctnt_id);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public int remove() throws Exception {
        int row = 0;
        try {
            File file = new File(this.full_path_src);
            if ((file != null) && (file.exists())
                    && (file.delete())) {
                log.log(Level.SEVERE, "file deleted");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "permanent delete error!!", e);
        }
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE  FROM ctnt_mms_sub_ctnt WHERE mms_sub_ctnt_id = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.content_id);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public int remove(int mms_mesg_id) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE  FROM ctnt_mms_sub_ctnt WHERE mms_mesg_id = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, mms_mesg_id);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE ctnt_mms_sub_ctnt    SET sub_ordr_num=?      , ctnt_type=?      , full_path_src=?  WHERE mms_sub_ctnt_id=" + this.content_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.sub_order_number);
                cp.getPreparedStatement().setInt(2, this.ctnt_type.getId());
                cp.getPreparedStatement().setString(3, this.full_path_src);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public int getContent_id() {
        return this.content_id;
    }

    public void setContent_id(int content_id) {
        this.content_id = content_id;
    }

    public int getMms_mesg_id() {
        return this.mms_mesg_id;
    }

    public void setMms_mesg_id(int mms_mesg_id) {
        this.mms_mesg_id = mms_mesg_id;
    }

    public Message.SMS_TYPE getCtnt_type() {
        return this.ctnt_type;
    }

    public void setCtnt_type(Message.SMS_TYPE ctnt_type) {
        this.ctnt_type = ctnt_type;
    }

    public String getFull_path_src() {
        return this.full_path_src;
    }

    public void setFull_path_src(String full_path_src) {
        this.full_path_src = full_path_src;
    }

    public int getSub_order_number() {
        return this.sub_order_number;
    }

    public void setSub_order_number(int sub_order_number) {
        this.sub_order_number = sub_order_number;
    }
}