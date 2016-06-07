package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import lib.common.DBPoolManager;

public class TxDrQueue {

    protected long dr_queue_id;
    protected long tx_queue_id;
    protected long rx_id;
    protected int uid;
    protected int status;
    protected Date create_dt;
    protected Date last_mod_dt;

    public TxDrQueue() {
    }

    public TxDrQueue(long dr_queue_id)
            throws Exception {
        this.dr_queue_id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM trns_dr_queue  WHERE dr_queue_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setLong(1, dr_queue_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.dr_queue_id = rs.getLong("dr_queue_id");
                    this.tx_queue_id = rs.getLong("tx_queue_id");
                    this.rx_id = rs.getLong("rx_id");
                    this.uid = rs.getInt("uid");
                    this.status = rs.getInt("status");
                    this.create_dt = rs.getTimestamp("create_dt");
                    this.last_mod_dt = rs.getTimestamp("last_mod_dt");
                } else {
                    throw new Exception("DR queue not found");
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
    }

    public int setStatus(int status) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = 
                        "   UPDATE trns_dr_queue"
                        + "    SET Status=? WHERE dr_queue_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, status);
                cp.getPreparedStatement().setLong(2, this.dr_queue_id);
                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public static TxDrQueue get(long dr_queue_id) {
        TxDrQueue dr = null;
        try {
            dr = new TxDrQueue(dr_queue_id);
            if (dr == null) {
                throw new Exception("error, cannot get DR");
            }

            dr.setStatus(TxQueue.TX_STATUS.SENDING.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dr;
    }

    public static TxDrQueue find(long tx_queue_id) throws Exception {
        TxDrQueue dr = null;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = 
                        "  SELECT dr_queue_id"
                        + "  FROM trns_dr_queue"
                        + " WHERE tx_queue_id=?"
                        + " LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setLong(1, tx_queue_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    dr = new TxDrQueue(rs.getLong(1));
                } else {
                    throw new Exception("DR queue not found");
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

        return dr;
    }

    public static int add(long tx_queue_id, long rx_id, int uid) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "INSERT INTO trns_dr_queue (tx_queue_id, rx_id, uid, create_dt)   VALUES (?,?,?, NOW())";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setLong(1, tx_queue_id);
                cp.getPreparedStatement().setLong(2, rx_id);
                cp.getPreparedStatement().setInt(3, uid);
                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public Date getCreate_dt() {
        return this.create_dt;
    }

    public long getDr_queue_id() {
        return this.dr_queue_id;
    }

    public Date getLast_mod_dt() {
        return this.last_mod_dt;
    }

    public long getRx_id() {
        return this.rx_id;
    }

    public int getStatus() {
        return this.status;
    }

    public long getTx_queue_id() {
        return this.tx_queue_id;
    }

    public int getUid() {
        return this.uid;
    }
}