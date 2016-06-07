package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class RxQueue {

    private static final Logger log = Logger.getLogger(RxQueue.class.getClass().getName());
    protected long rx_id;
    public long rx_cdg_id;
    public int oper_id;
    public Timestamp recv_dt;
    public int type;
    public String content;
    public String ip;

    public RxQueue() {
    }

    public RxQueue(long rx_id)
            throws Exception {
        this.rx_id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT * FROM trns_rx  WHERE rx_id=?;";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, rx_id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.rx_id = rx_id;
                this.rx_cdg_id = rs.getLong("rx_cdg_id");
                this.oper_id = rs.getInt("oper_id");
                this.recv_dt = rs.getTimestamp("recv_dt");
                this.type = rs.getInt("type");
                this.content = rs.getString("content");
                this.ip = rs.getString("ip");
            } else {
                throw new Exception("rx queue not found " + rx_id);
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public int hashCode() {
        return Long.valueOf(rx_id).hashCode();
    }

    public boolean equals(Object obj) {
        if ((obj instanceof RxQueue)) {
            RxQueue txq = (RxQueue) obj;

            return this.rx_id == txq.rx_id;
        }
        return false;
    }

    public long getRx_id() {
        return this.rx_id;
    }

    public int getType() {
        return this.type;
    }

    public static long add(OperConfig.CARRIER oper, RX_TYPE type, String content) {
        long qid = -1;

        String sql = "INSERT INTO trns_rx (oper_id, type, content) VALUES (?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setInt(1, oper.getId());
                cp.getPreparedStatement().setInt(2, type.getId());
                cp.getPreparedStatement().setString(3, content);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        qid = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
        return qid;
    }

    public static long add(OperConfig.CARRIER oper, RX_TYPE type, String content, String ip) {
        long qid = -1;

        String sql = "INSERT INTO trns_rx (oper_id, type, content, ip) VALUES (?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setInt(1, oper.getId());
                cp.getPreparedStatement().setInt(2, type.getId());
                cp.getPreparedStatement().setString(3, content);
                cp.getPreparedStatement().setString(4, ip);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        qid = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
        return qid;
    }

    public static long add(OperConfig.CARRIER oper, RX_TYPE type, String content, String ip, long rx_cdg_id) {
        long qid = -1;

        String sql = "INSERT INTO trns_rx (oper_id, type, content, ip, rx_cdg_id) VALUES (?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setInt(1, oper.getId());
                cp.getPreparedStatement().setInt(2, type.getId());
                cp.getPreparedStatement().setString(3, content);
                cp.getPreparedStatement().setString(4, ip);
                cp.getPreparedStatement().setLong(5, rx_cdg_id);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        qid = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
        return qid;
    }

    public static long add(int from, RX_TYPE type, String content, String ip) {
        long qid = -1;

        String sql = "INSERT INTO trns_rx (oper_id, type, content, ip)"
                + " VALUES (?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setInt(1, from);
                cp.getPreparedStatement().setInt(2, type.getId());
                cp.getPreparedStatement().setString(3, content);
                cp.getPreparedStatement().setString(4, ip);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        qid = rs.getLong(1);
                    }
                    rs.close();
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
        return qid;
    }

    public void setType(RX_TYPE type) {
        String sql = "UPDATE trns_rx"
                + "  SET type=" + type.getId()
                + " WHERE rx_id=" + this.rx_id;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.execUpdate(sql);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
    }

    public void addType(RX_TYPE type) {
        String sql = "UPDATE trns_rx"
                + "  SET type=type|" + type.getId()
                + " WHERE rx_id=" + this.rx_id;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.execUpdate(sql);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
    }

    public static enum RX_TYPE {

        UNKNOWN(0),
        SMS(1),
        IVR(2),
        DR(4),
        SUB(8),
        UNSUB(16),
        HTTP(32),
        MMS(64),
        WAP(128),
        REPLYMSG(256),
        USSD(512),
        CSS_OK(1024),
        LOCAL_OK(2048);
        private final int id;

        private RX_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static RX_TYPE fromId(int id) {
            for (RX_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}