package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class SubscriberBlocked
        implements Comparable<SubscriberBlocked> {

    protected String msisdn;
    protected Timestamp create_dt;
    protected boolean block_flg;

    public int hashCode() {
        return Integer.parseInt(this.msisdn);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof SubscriberBlocked)) {
            SubscriberBlocked sub = (SubscriberBlocked) obj;

            return getMsisdn().equals(sub.getMsisdn());
        }
        return false;
    }

    public int compareTo(SubscriberBlocked sub) {
        return (int) (Double.parseDouble(getMsisdn()) - Double.parseDouble(sub.getMsisdn()));
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public Timestamp getCreate_dt() {
        return this.create_dt;
    }

    public boolean isBlocked() {
        return this.block_flg;
    }

    public int setBlocked(boolean block_flg) {
        int row = 0;

        this.block_flg = block_flg;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE mmbr_blck_list   SET block_flg=? WHERE msisdn=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setBoolean(1, block_flg);
                cp.getPreparedStatement().setString(2, this.msisdn);
                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return row;
    }

    public int remove() {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM mmbr_blck_list WHERE msisdn='" + this.msisdn + "'";

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return row;
    }

    public SubscriberBlocked(String msisdn) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM mmbr_blck_list  WHERE msisdn=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, msisdn);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.msisdn = rs.getString("msisdn");
                    this.create_dt = rs.getTimestamp("create_dt");
                    this.block_flg = rs.getBoolean("block_flg");
                } else {
                    throw new Exception("blocked subscriber not found!!");
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

    public static int add(String[] msisdn) {
        int row = 0;
        for (int i = 0; i < msisdn.length; i++) {
            try {
                row += add(msisdn[i]);
            } catch (Exception e) {
            }
        }
        return row;
    }

    public static int add(String msisdn) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "INSERT INTO mmbr_blck_list (msisdn, create_dt) VALUES ('" + msisdn + "', NOW())";

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
        return row;
    }

    public static boolean isBlocked(String msisdn) {
        boolean blocked = false;
        try {
            if ((msisdn == null) || (msisdn.isEmpty())) {
                return blocked;
            }

            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "  SELECT msisdn  FROM mmbr_blck_list WHERE 1   AND block_flg = 1   AND msisdn = '" + msisdn + "'";

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    blocked = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blocked;
    }

    public static List<SubscriberBlocked> getBlockedList(String msisdn, Boolean blocked, Date from, Date to) {
        List blockedList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String whereMsisdn = "";
            if ((msisdn != null) && (!msisdn.isEmpty())) {
                whereMsisdn = " AND msisdn REGEXP '" + msisdn + "'";
            }

            String whereBlock = "";
            if (blocked != null) {
                whereBlock = " AND block_flg = " + blocked;
            }

            String whereDate = "";
            if (from != null) {
                if (to == null) {
                    to = from;
                }
                whereDate = " AND DATE(create_dt) BETWEEN " + DatetimeUtil.print("''yyyy-MM-dd''", from) + " AND " + DatetimeUtil.print("''yyyy-MM-dd''", to);
            }

            try {
                String sql = "  SELECT msisdn  FROM mmbr_blck_list WHERE 1" + whereMsisdn + whereBlock + whereDate;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    try {
                        SubscriberBlocked sub = new SubscriberBlocked(rs.getString("msisdn"));
                        blockedList.add(sub);
                    } catch (Exception e) {
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blockedList;
    }
}