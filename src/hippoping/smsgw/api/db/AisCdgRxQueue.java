package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class AisCdgRxQueue extends RxQueue {

    private static final Logger log = Logger.getLogger(AisCdgRxQueue.class.getClass().getName());
    public String LinkedID;
    public String Service_ID;
    public String Sender;
    public String Recipients;
    public String MMSRelayServerID;
    public String msisdn;
    public String Channel;
    public String nType;

    public AisCdgRxQueue() {
    }

    public AisCdgRxQueue(long rx_id)
            throws Exception {
        this.rx_id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT * FROM trns_rx_cdg  WHERE rx_cdg_id=?;";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, rx_id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.rx_id = rx_id;
                this.LinkedID = rs.getString("LinkedID");
                this.Service_ID = rs.getString("MIBIdentification");
                this.Sender = rs.getString("Sender");
                this.Recipients = rs.getString("Recipients");
                this.oper_id = OperConfig.CARRIER.AIS.getId();
                this.content = rs.getString("Content");
                this.MMSRelayServerID = rs.getString("MMSRelayServerID");
                this.msisdn = rs.getString("msisdn");
                this.recv_dt = rs.getTimestamp("recv_dt");
                this.ip = rs.getString("ip");
                this.Channel = rs.getString("Channel");
                this.nType = rs.getString("NType");
            }

            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public static long add(String LinkedID, String Service_ID, String Sender, String Recipients, String Content, String MMSRelayServerID, String msisdn, String ip, String Channel, String nType, String timestamp) {
        long qid = -1;
        
        Timestamp ts = null;
        try {
            String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
            ts = DatetimeUtil.toTimestamp(DatetimeUtil.toDate(timestamp, format));
        } catch (ParseException e) {
            log.log(Level.WARNING, "parse timestamp[" + timestamp + "] error!!");
        }

        String sql = "INSERT INTO  trns_rx_cdg (LinkedID, MIBIdentification, Sender, Recipients, Content, MMSRelayServerID, msisdn, ip, Channel, Ntype, timestamp) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setString(1, LinkedID);
                cp.getPreparedStatement().setString(2, Service_ID);
                cp.getPreparedStatement().setString(3, Sender);
                cp.getPreparedStatement().setString(4, Recipients);
                cp.getPreparedStatement().setString(5, Content);
                cp.getPreparedStatement().setString(6, MMSRelayServerID);
                cp.getPreparedStatement().setString(7, msisdn);
                cp.getPreparedStatement().setString(8, ip);
                cp.getPreparedStatement().setString(9, Channel);
                cp.getPreparedStatement().setString(10, nType);
                cp.getPreparedStatement().setTimestamp(11, ts);

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
            log.log(Level.SEVERE, "exception caught", e);
        }
        return qid;
    }
}