package hippoping.smsgw.api.db;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class User
        implements Serializable {

    protected int uid;
    protected int suid;
    protected int gid;
    protected String name;
    protected USER_TYPE type;
    protected String fname;
    protected String lname;
    protected String email;
    protected String tel;
    protected String mobile;
    protected String fax;
    protected USER_STATUS status;
    protected Date register_dt;
    protected String[] childUid;
    protected int dr_link_id;

    public int getDr_link_id() {
        return this.dr_link_id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFax() {
        return this.fax;
    }

    public String getFname() {
        return this.fname;
    }

    public String getLname() {
        return this.lname;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getName() {
        return this.name;
    }

    public Date getRegister_dt() {
        return this.register_dt;
    }

    public USER_STATUS getStatus() {
        return this.status;
    }

    public int getSuid() {
        return this.suid;
    }

    public String getTel() {
        return this.tel;
    }

    public USER_TYPE getType() {
        return this.type;
    }

    public int getGid() {
        return this.gid;
    }

    public int getUid() {
        return this.uid;
    }

    public String[] getChildUid() {
        return this.childUid;
    }

    public User(int uid) throws Exception {
        this.uid = uid;
        try {
            DBPoolManager cp = new DBPoolManager();
            Statement stmt = null;
            try {
                String sql = "SELECT * FROM users  WHERE uid=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, uid);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.uid = rs.getInt("uid");
                    this.suid = rs.getInt("suid");
                    this.gid = rs.getInt("gid");
                    this.name = rs.getString("name");
                    this.type = USER_TYPE.fromId(rs.getInt("type"));
                    this.fname = rs.getString("fname");
                    this.lname = rs.getString("lname");
                    this.email = rs.getString("email");
                    this.tel = rs.getString("tel");
                    this.mobile = rs.getString("mobile");
                    this.fax = rs.getString("fax");
                    this.status = USER_STATUS.fromId(rs.getInt("status"));
                    this.register_dt = DatetimeUtil.toDate(rs.getTimestamp("register_dt"));
                    this.dr_link_id = rs.getInt("dr_link_id");
                } else {
                    throw new Exception("uid not found");
                }

                rs.close();

                String pssql = "{call childPriviledgeSearch(?, ?)}";
                CallableStatement cstmt = cp.prepareCall(pssql);
                cstmt.setInt(1, uid);
                cstmt.registerOutParameter(2, java.sql.Types.VARCHAR);
                cstmt.executeUpdate();
                String child_uid = cstmt.getString(2);
                if ((child_uid != null) && (!child_uid.isEmpty())) {
                    this.childUid = child_uid.replaceAll(",", " ").trim().split(" ");
                }
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static int findUserID(String name) throws Exception {
        int uid = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT uid FROM users  WHERE name=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, name);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    uid = rs.getInt(1);
                } else {
                    throw new Exception("invalid username");
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
        return uid;
    }

    public static List<User> getAllUser()
            throws Exception {
        List users = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT uid FROM users ";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    try {
                        User user = UserFactory.getUser(rs.getInt("uid"));

                        users.add(user);
                    } catch (Exception e) {
                    }
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return users;
    }

    public static enum USER_STATUS {

        INACTIVE(0),
        ACTIVE(1),
        TEMPORARY(2);
        private final int id;

        private USER_STATUS(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static USER_STATUS fromId(int id) {
            for (USER_STATUS e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }

    public static enum USER_TYPE {

        ADMIN(0),
        SENIOR(1),
        SUPERVISOR(2),
        USER(3),
        GUEST(4);
        private final int id;

        private USER_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static USER_TYPE fromId(int id) {
            for (USER_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}