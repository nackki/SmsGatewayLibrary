package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class Tx3rdPartyMo {

    protected long txid;
    protected String msisdn;
    protected OperConfig.CARRIER oper;
    protected String mesg_type;
    protected String mesg;
    protected TxQueue.TX_STATUS status;
    protected int status_code;
    protected String status_desc;
    protected Timestamp issue_dt;
    protected int ctnt_3rdp_id;
    protected long rx_id;
    protected int srvc_main_id;

    public int hashCode() {
        return Long.valueOf(txid).hashCode();
    }

    public boolean equals(Object obj) {
        if ((obj instanceof Tx3rdPartyMo)) {
            Tx3rdPartyMo txq = (Tx3rdPartyMo) obj;

            return this.txid == txq.getTxid();
        }
        return false;
    }

    public Tx3rdPartyMo() {
    }

    public Tx3rdPartyMo(long id) throws Exception {
        this.txid = id;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT *  FROM trns_3rdp_mo WHERE txid=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.msisdn = rs.getString("msisdn");
                this.oper = OperConfig.CARRIER.fromId(rs.getInt("oper_id"));
                this.srvc_main_id = rs.getInt("srvc_main_id");
                this.mesg_type = rs.getString("mesg_type");
                this.mesg = rs.getString("mesg");
                this.status = TxQueue.TX_STATUS.fromId(rs.getInt("status"));
                this.status_code = rs.getInt("status_code");
                this.status_desc = rs.getString("status_desc");
                this.issue_dt = rs.getTimestamp("issue_dt");
                this.ctnt_3rdp_id = rs.getInt("ctnt_3rdp_id");
                this.rx_id = rs.getLong("rx_id");
            }

            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
        } finally {
            cp.release();
        }
    }

    public static Tx3rdPartyMo findByRxId(long id) throws Exception {
        Tx3rdPartyMo mo = null;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT txid  FROM trns_3rdp_mo WHERE rx_id=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                mo = new Tx3rdPartyMo(rs.getInt(1));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }

        return mo;
    }

    public static long add(String msisdn, String mesg_type, String mesg, int srvc_main_id, OperConfig.CARRIER oper, long rx_id, int third_party_id) throws Exception {
        long id = -1;
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "INSERT INTO trns_3rdp_mo (msisdn, srvc_main_id, oper_id, mesg_type, mesg, ctnt_3rdp_id, rx_id, issue_dt) VALUES (?, ?, ?, ?, ?, ?, ?, now())";

            cp.prepareStatement(sql, 1);
            cp.getPreparedStatement().setString(1, msisdn);
            cp.getPreparedStatement().setInt(2, srvc_main_id);
            cp.getPreparedStatement().setInt(3, oper.getId());
            cp.getPreparedStatement().setString(4, mesg_type);
            cp.getPreparedStatement().setString(5, mesg);
            cp.getPreparedStatement().setInt(6, third_party_id);
            cp.getPreparedStatement().setLong(7, rx_id);

            int row = cp.execUpdatePrepareStatement();
            if (row == 1) {
                ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getLong(1);
                }

                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }

        return id;
    }

    public int updateResponse(int status_code, String status_desc) throws Exception {
        int row = 0;
        DBPoolManager cp = new DBPoolManager();

        this.status_code = status_code;
        this.status_desc = status_desc;
        try {
            String sql = 
                    "  UPDATE trns_3rdp_mo"
                    + "   SET status_code=?"
                    + "     , status_desc=?"
                    + " WHERE txid=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, status_code);
            cp.getPreparedStatement().setString(2, status_desc);
            cp.getPreparedStatement().setLong(3, this.txid);

            row = cp.execUpdatePrepareStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }

        return row;
    }

    public int getCtnt_3rdp_id() {
        return this.ctnt_3rdp_id;
    }

    public String getMesg_type() {
        return this.mesg_type;
    }

    public String getMesg() {
        return this.mesg;
    }

    public Timestamp getIssue_dt() {
        return this.issue_dt;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public OperConfig.CARRIER getOper() {
        return this.oper;
    }

    public TxQueue.TX_STATUS getStatus() {
        return this.status;
    }

    public int setStatus(TxQueue.TX_STATUS status) throws Exception {
        int row = 0;
        this.status = status;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "UPDATE trns_3rdp_mo SET status=? WHERE txid=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, status.getId());
            cp.getPreparedStatement().setLong(2, this.txid);

            row = cp.execUpdatePrepareStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
        return row;
    }

    public int getStatus_code() {
        return this.status_code;
    }

    public String getStatus_desc() {
        return this.status_desc;
    }

    public long getTxid() {
        return this.txid;
    }

    public long getRx_id() {
        return this.rx_id;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public static Tx3rdPartyMo next() throws Exception {
        int id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "  SELECT txid  FROM trns_3rdp_mo WHERE status=" + TxQueue.TX_STATUS.QUEUE.getId() + " ORDER BY txid ASC" + " LIMIT 1";

            ResultSet rs = cp.execQuery(sql);
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
        return new Tx3rdPartyMo(id);
    }

    public static void doRetryFlag() throws Exception {
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "  UPDATE trns_3rdp_mo   SET status=0 WHERE ( status=" + TxQueue.TX_STATUS.ERROR.getId() + "    OR status=" + TxQueue.TX_STATUS.INVALID.getId() + " )" + "   AND last_mod_dt < DATE_SUB(NOW(), INTERVAL 5 MINUTE)" + "   AND issue_dt > DATE_SUB(NOW(), INTERVAL 1 DAY)";

            cp.execUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
    }
}