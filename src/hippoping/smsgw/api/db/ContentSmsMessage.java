package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class ContentSmsMessage {

    protected int content_id;
    protected ServiceContentAction.ACTION_TYPE content_type;

    public int getContent_id() {
        return this.content_id;
    }

    public ServiceContentAction.ACTION_TYPE getContent_type() {
        return this.content_type;
    }

    public ContentSmsMessage(long tx_queue_id) throws Exception {
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT ctnt_id     , ctnt_type  FROM trns_tx_queue WHERE tx_queue_id = ? ";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, tx_queue_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.content_type = ServiceContentAction.ACTION_TYPE.values()[rs.getInt("ctnt_type")];
                this.content_id = rs.getInt("ctnt_id");
            } else {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "tx queue[" + tx_queue_id + "] not found!!");
                throw new Exception();
            }

            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!");
        } catch (Exception e) {
            throw e;
        } finally {
            cp.release();
        }
    }

    public static int createDuplicate(int ctnt_id, int disposable) {
        int cid = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "INSERT INTO ctnt_sms_mesg SELECT NULL, sms_info_id, type, content, ?, encoding, owner  FROM ctnt_sms_mesg  WHERE sms_mesg_id=?  LIMIT 1";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, disposable);
                cp.getPreparedStatement().setInt(2, ctnt_id);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        cid = rs.getInt(1);
                    }

                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }

        return cid;
    }

    public static boolean isContentBlank(int id) {
        boolean blank = false;

        String sql = "SELECT LENGTH(content) as len FROM ctnt_sms_mesg  WHERE sms_mesg_id=?";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, id);

                ResultSet rs = cp.execQueryPrepareStatement();
                if ((rs.next())
                        && (rs.getInt("len") == 0)) {
                    blank = true;
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return blank;
    }
}