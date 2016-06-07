package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ObjectNotFoundException;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class AisCdgDrFactory {

    private static final Logger log = Logger.getLogger(AisCdgDrFactory.class.getName());
    private static final String[] columns = {"leg", "MMStatus", "MessageId", "Recipient", "Sender", "StatusCode", "StatusText", "NType",
        "Channel", "MSISDN", "LinkedID", "SSSActionReport", "GMessageId", "UserServiceNo", "MessageSequenceId",
        "Bearer", "BillInfo", "ClassofService", "vpkgId", "timestamp", "CCT"};

    public static long add(Hashtable param, int process_flg) {
        long qid = -1;

        String sql = "INSERT INTO"
                + "  trns_dr_cdg (leg, MessageId, Recipient, Sender, MMStatus"
                + ", StatusCode, StatusText, NType, Channel, MSISDN"
                + ", LinkedID, SSSActionReport, GMessageId, UserServiceNo, MessageSequenceId"
                + ", Bearer, BillInfo, ClassofService, vpkgId, timestamp"
                + ", CCT, process_flg)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                String timestamp = (String) param.get("timestamp");
                if ((timestamp == null) || (timestamp.isEmpty())) {
                    timestamp = DatetimeUtil.getDateTime("yyyy-MM-dd HH:mm:ss");
                }

                String cct = null;
                if (!((String) param.get("CCT")).isEmpty()) {
                    cct = (String) param.get("CCT");
                }

                cp.getPreparedStatement().setString(1, (String) param.get("leg"));
                cp.getPreparedStatement().setString(2, (String) param.get("MessageId"));
                cp.getPreparedStatement().setString(3, (String) param.get("Recipient"));
                cp.getPreparedStatement().setString(4, (String) param.get("Sender"));
                cp.getPreparedStatement().setString(5, (String) param.get("MMStatus"));
                cp.getPreparedStatement().setString(6, (String) param.get("StatusCode"));
                cp.getPreparedStatement().setString(7, (String) param.get("StatusText"));
                cp.getPreparedStatement().setString(8, (String) param.get("NType"));
                cp.getPreparedStatement().setString(9, (String) param.get("Channel"));
                cp.getPreparedStatement().setString(10, (String) param.get("MSISDN"));
                cp.getPreparedStatement().setString(11, (String) param.get("LinkedID"));
                cp.getPreparedStatement().setString(12, (String) param.get("SSSActionReport"));
                cp.getPreparedStatement().setString(13, (String) param.get("GMessageId"));
                cp.getPreparedStatement().setString(14, (String) param.get("UserServiceNo"));
                cp.getPreparedStatement().setString(15, (String) param.get("MessageSequenceId"));
                cp.getPreparedStatement().setString(16, (String) param.get("Bearer"));
                cp.getPreparedStatement().setString(17, (String) param.get("BillInfo"));
                cp.getPreparedStatement().setString(18, (String) param.get("ClassofService"));
                cp.getPreparedStatement().setString(19, (String) param.get("vpkgId"));
                cp.getPreparedStatement().setString(20, timestamp);
                cp.getPreparedStatement().setString(21, cct);
                cp.getPreparedStatement().setInt(22, process_flg);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        qid = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return qid;
    }

    public static Hashtable get(long id) throws ObjectNotFoundException {
        Hashtable param = null;

        String sql = "SELECT *"
                + "     FROM trns_dr_cdg"
                + "    WHERE dr_cdg_id = ?";

        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setLong(1, id);

                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    param = new Hashtable();
                    for (String col : columns) {
                        param.put(col, rs.getString(col) == null ? "" : rs.getString(col));
                    }
                } else {
                    throw new ObjectNotFoundException("RX number " + id + " not found in trns_dr_cdg!!");
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

        return param;
    }

    public static Hashtable get(Hashtable vars, Set int_fields) throws ObjectNotFoundException {
        Hashtable param = null;
        String where = "";

        // prepared WHERE conditions
        for (Iterator iter = vars.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String cover = ((int_fields != null && !int_fields.isEmpty() && int_fields.contains(key)) ? "" : "'");
            if (!key.matches("(?i)^extra.*")) {
                where += " AND " + key + "="
                        + cover
                        + (String) vars.get(key)
                        + cover;
            } else {
                where += " AND " + (String) vars.get(key);
            }

        }

        String sql =
                "  SELECT *"
                + "  FROM trns_dr_cdg"
                + " WHERE 1"
                + where
                + " ORDER BY timestamp DESC"
                + " LIMIT 1";

        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                //log.info(sql);

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    param = new Hashtable();
                    for (String col : columns) {
                        param.put(col, rs.getString(col) == null ? "" : rs.getString(col));
                    }
                } else {
                    throw new ObjectNotFoundException(where + " not found in trns_dr_cdg!!");
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

        return param;
    }
}