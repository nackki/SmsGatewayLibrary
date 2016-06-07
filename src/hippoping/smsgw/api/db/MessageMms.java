package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class MessageMms extends Message {

    public static final String ROOT_DIR = "ext/mms";
    public static final String MMS_MO_DIR = "ext/mms/mo";
    private static final Logger log = Logger.getLogger(MessageMms.class.getClass().getName());
    private List<MessageMmsSubContent> subcontent;
    private String from;
    private String subject;

    public MessageMms() {
    }

    public MessageMms(int id)
            throws Exception {
        this.content_id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT m.*, IFNULL(sc.mms_sub_ctnt_id, -1) AS mms_sub_ctnt_id  FROM ctnt_mms_mesg AS m  LEFT JOIN ctnt_mms_sub_ctnt AS sc    ON m.mms_mesg_id = sc.mms_mesg_id WHERE m.mms_mesg_id = ? ORDER BY sc.sub_ordr_num ASC";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    setContentType(ServiceContentAction.ACTION_TYPE.MMS);
                    while (rs.next()) {
                        if (this.content_id == -1) {
                            this.content_id = id;
                            this.subject = rs.getString("subject");
                            this.from = rs.getString("from");
                            this.disposable = rs.getInt("disposable");
                        }

                        if (this.subcontent == null) {
                            this.subcontent = new ArrayList();
                        }

                        if (rs.getInt("mms_sub_ctnt_id") != -1) {
                            this.subcontent.add(new MessageMmsSubContent(rs.getInt("mms_sub_ctnt_id")));
                        }
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

    public static int add(String subject, String from, int disposable) throws Exception {
        int id = -1;

        String sql = "INSERT INTO ctnt_mms_mesg (subject, `from`, disposable) VALUES(?, ?, ?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setString(1, subject);
                cp.getPreparedStatement().setString(2, from);
                cp.getPreparedStatement().setInt(3, disposable);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }

                    rs.close();
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return id;
    }

    public int addSubContent(String location, Message.SMS_TYPE type, int order_num) throws Exception {
        int id = -1;

        String sql = "INSERT INTO ctnt_mms_sub_ctnt (mms_mesg_id, sub_ordr_num, ctnt_type, full_path_src) VALUES(?, ?, ?, ?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, this.content_id);
                cp.getPreparedStatement().setInt(2, order_num);
                cp.getPreparedStatement().setInt(3, type.getId());
                cp.getPreparedStatement().setString(4, location);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }

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

        return id;
    }

    public int getMaxSubOrderNumber() throws Exception {
        int id = -1;

        String sql = "  SELECT IFNULL(MAX(sub_ordr_num), 0)  FROM ctnt_mms_sub_ctnt WHERE mms_mesg_id=?";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.content_id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    if (rs.next()) {
                        id = rs.getInt(1);
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

        return id;
    }

    public int remove() throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                for (Iterator iter = this.subcontent.iterator(); iter.hasNext();) {
                    MessageMmsSubContent sc = (MessageMmsSubContent) iter.next();
                    sc.remove();
                }

                String sql = "DELETE  FROM ctnt_mms_mesg WHERE mms_mesg_id = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.content_id);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public void reorganize() throws Exception {
        Queue queue = new LinkedList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT mms_sub_ctnt_id  FROM ctnt_mms_sub_ctnt WHERE mms_mesg_id = ? ORDER BY sub_ordr_num ASC";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.content_id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    while (rs.next()) {
                        queue.add(Integer.valueOf(rs.getInt(1)));
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

        int i = 1;
        for (Iterator iter = queue.iterator(); iter.hasNext();) {
            MessageMmsSubContent sc = new MessageMmsSubContent(((Integer) iter.next()).intValue());
            System.err.print(sc.getFull_path_src());
            sc.setSub_order_number(i++);
            System.out.println("sync " + sc.sync() + " row");
        }
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE ctnt_mms_mesg    SET subject=?      , `from`=?      , disposable=?  WHERE mms_mesg_id=" + getContent_id();

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.subject);
                cp.getPreparedStatement().setString(2, this.from);
                cp.getPreparedStatement().setInt(3, getDisposable());
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

    public boolean isContentBlank(int id) throws Exception {
        boolean isBlank = true;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT *  FROM ctnt_mms_mesg AS m INNER JOIN ctnt_mms_sub_ctnt AS sc    ON m.mms_mesg_id = sc.mms_mesg_id WHERE m.mms_mesg_id = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    if (rs.next()) {
                        isBlank = false;
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

        return isBlank;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<MessageMmsSubContent> getSubcontent() {
        return this.subcontent;
    }
}