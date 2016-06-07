package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class TxSmsDownload
        implements Comparable<TxSmsDownload> {

    protected long id;
    protected String msisdn;
    protected String keyword;
    protected int srvc_main_id;
    protected OperConfig.CARRIER oper;
    protected String srvc_name;
    protected Timestamp recv_dt;
    protected long rx_id;

    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    public boolean equals(Object obj) {
        TxSmsDownload txSmsDownload = (TxSmsDownload) obj;
        return this.id > txSmsDownload.id;
    }

    public int compareTo(TxSmsDownload txSmsDownload) {
        return Long.valueOf(id).compareTo(Long.valueOf(txSmsDownload.id));
    }

    public TxSmsDownload(long id) throws Exception {
        this.id = id;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT tx.*, s.name  FROM trns_sms_download tx INNER JOIN srvc_main s    ON s.srvc_main_id = tx.srvc_main_id WHERE id=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.msisdn = rs.getString("msisdn");
                this.keyword = rs.getString("keyword");
                this.srvc_main_id = rs.getInt("srvc_main_id");
                this.oper = OperConfig.CARRIER.fromId(rs.getInt("oper_id"));
                this.srvc_name = rs.getString("name");
                this.recv_dt = rs.getTimestamp("datetime");
                this.rx_id = rs.getLong("rx_id");
            }

            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sql error!", e);
        } finally {
            cp.release();
        }
    }

    public static TxSmsDownload add(String msisdn, String keyword, int srvc_main_id, int oper_id, long rx_id) throws Exception {
        TxSmsDownload __instance = null;
        long id = 0;
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "INSERT INTO trns_sms_download (msisdn, keyword, srvc_main_id, oper_id, rx_id) VALUES (?, ?, ?, ?, ?)";

            cp.prepareStatement(sql, 1);
            cp.getPreparedStatement().setString(1, msisdn);
            cp.getPreparedStatement().setObject(2, keyword);
            cp.getPreparedStatement().setInt(3, srvc_main_id);
            cp.getPreparedStatement().setInt(4, oper_id);
            cp.getPreparedStatement().setLong(5, rx_id);

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

        if (id != 0) {
            __instance = new TxSmsDownload(id);
        }

        return __instance;
    }

    public long getRx_id() {
        return this.rx_id;
    }

    public long getId() {
        return this.id;
    }

    public OperConfig.CARRIER getOper() {
        return this.oper;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public String getSrvc_name() {
        return this.srvc_name;
    }

    public Timestamp getRecv_dt() {
        return this.recv_dt;
    }
}