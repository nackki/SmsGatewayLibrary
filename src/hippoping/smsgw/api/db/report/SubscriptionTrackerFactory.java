package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.RxQueue.RX_TYPE;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.SubscriberGroup;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class SubscriptionTrackerFactory {

    private static final Logger log = Logger.getLogger(SubscriptionTrackerFactory.class.getName());

    public static List<SubscriptionTracker> get(int srvc_main_id, OperConfig.CARRIER oper, SummaryReport.COMMAND command, Date date) {
        List tracks = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            String whereoper = "";
            if (oper != OperConfig.CARRIER.ALL) {
                whereoper = "    AND r.oper_id "
                        + (oper == OperConfig.CARRIER.AIS
                        ? " IN (" + OperConfig.CARRIER.AIS.getId() + "," + OperConfig.CARRIER.AIS_LEGACY.getId() + ")"
                        : new StringBuilder().append("=").append(oper.getId()).toString());
            }

            String wheresrvcid = "";
            if (srvc_main_id > 0) {
                wheresrvcid = "    AND r.srvc_main_id=" + srvc_main_id;
            }
            try {
                String sql = "   SELECT t.sub_track_id"
                        + "   FROM rept_trns_sum r"
                        + "  INNER JOIN trns_sub_track t"
                        + "     ON r.trns_sum_id = t.trns_sum_id"
                        + "    AND t.trns_sum_id != 0"
                        + wheresrvcid
                        + whereoper
                        + "    AND YEAR(r.sum_date)=?"
                        + "    AND MONTH(r.sum_date)=?"
                        + "  WHERE t.rept_actn_type&?"
                        + "  ORDER BY t.recv_dt ASC";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, Integer.parseInt(DatetimeUtil.print("yyyy", date)));
                cp.getPreparedStatement().setInt(2, Integer.parseInt(DatetimeUtil.print("MM", date)));
                cp.getPreparedStatement().setInt(3, (int) Math.pow(2.0D, Double.parseDouble(String.valueOf(command.getId()))));

                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    try {
                        SubscriptionTracker tracker = new SubscriptionTracker(rs.getInt(1));

                        tracks.add(tracker);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Error finding subscription tracker:{0}", Integer.valueOf(rs.getInt(1)));
                    }
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Find tracking error!!", e);
        }

        return tracks;
    }

    public static List<SubscriptionTracker> get(int trns_sum_id, SummaryReport.COMMAND command) {
        List tracks = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   SELECT t.sub_track_id"
                        + "   FROM trns_sub_track t"
                        + "  WHERE t.trns_sum_id=?"
                        + "    AND t.rept_actn_type&?"
                        + "  ORDER BY t.recv_dt ASC";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, trns_sum_id);
                cp.getPreparedStatement().setInt(2, (int) Math.pow(2.0D, Double.parseDouble(String.valueOf(command.getId()))));

                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    try {
                        SubscriptionTracker tracker = new SubscriptionTracker(rs.getInt(1));

                        tracks.add(tracker);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Error finding subscription tracker:{0}", Integer.valueOf(rs.getInt(1)));
                    }
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Find tracking error!!", e);
        }

        return tracks;
    }

    public static List<SubscriptionTracker> find(int srvc_main_id, OperConfig.CARRIER oper, Date date, int trns_sum_id, SummaryReport.COMMAND command) {
        List tracks = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String extra_sql = "";
                switch (command) {
                    case SUB_TOTAL:
                        extra_sql
                                = " INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m "
                                + "  ON m.msisdn = t.msisdn"
                                + " AND m.srvc_main_id = t.srvc_main_id"
                                + " AND t.subtype = 'SUB'"
                                + " AND DATE(t.recv_dt) = DATE(m.last_mod_dt)"
                                + ((oper == CARRIER.TRUEH || oper == CARRIER.DTAC_SDP || oper == CARRIER.CAT)
                                ? " INNER JOIN trns_rx_mo mo ON t.rx_mo_id = mo.rx_mo_id"
                                + " INNER JOIN trns_rx rx ON mo.rx_id = rx.rx_id"
                                + " AND rx.type & " + RX_TYPE.LOCAL_OK.getId()
                                : "");

                        break;
                    case SUB_FT:
                        extra_sql
                                = " INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m "
                                + "  ON m.msisdn = t.msisdn"
                                + " AND m.srvc_main_id = t.srvc_main_id"
                                + " AND m.free_trial > 0"
                                + " AND m.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                                + " AND t.subtype = 'SUB'"
                                + " AND DATE(t.recv_dt) = DATE(m.last_mod_dt)"
                                + ((oper == CARRIER.TRUEH || oper == CARRIER.DTAC_SDP || oper == CARRIER.CAT)
                                ? " INNER JOIN trns_rx_mo mo ON t.rx_mo_id = mo.rx_mo_id"
                                + " INNER JOIN trns_rx rx ON mo.rx_id = rx.rx_id"
                                + " AND rx.type & " + RX_TYPE.LOCAL_OK.getId()
                                : "");

                        break;
                    case SUB_ERROR_NOCREDIT:
                        extra_sql = " INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m "
                                + "  ON m.msisdn = t.msisdn"
                                + " AND m.srvc_main_id = t.srvc_main_id"
                                + " AND m.state != " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                                + " AND t.subtype = 'SUB'"
                                + " AND DATE(t.recv_dt) = DATE(m.last_mod_dt)"
                                + ((oper == CARRIER.TRUEH || oper == CARRIER.DTAC_SDP || oper == CARRIER.CAT)
                                ? " INNER JOIN trns_rx_mo mo ON t.rx_mo_id = mo.rx_mo_id"
                                + " INNER JOIN trns_rx rx ON mo.rx_id = rx.rx_id"
                                + " AND (rx.type & " + RX_TYPE.LOCAL_OK.getId() + ") = 0"
                                : "");

                        break;
                    case UNSUB_TOTAL:
                        extra_sql = " INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m "
                                + "  ON m.msisdn = t.msisdn"
                                + " AND m.srvc_main_id = t.srvc_main_id"
                                + " AND t.subtype = 'UNSUB'"
                                + " AND DATE(t.recv_dt) = DATE(m.last_mod_dt)"
                                + ((oper == CARRIER.TRUEH || oper == CARRIER.DTAC_SDP || oper == CARRIER.CAT)
                                ? " INNER JOIN trns_rx_mo mo ON t.rx_mo_id = mo.rx_mo_id"
                                + " INNER JOIN trns_rx rx ON mo.rx_id = rx.rx_id"
                                + " AND rx.type & " + RX_TYPE.LOCAL_OK.getId()
                                : "");

                        break;
                    case UNSUB_REQUEST:
                        extra_sql = " INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m "
                                + "  ON m.msisdn = t.msisdn"
                                + " AND m.srvc_main_id = t.srvc_main_id"
                                + " AND t.subtype = 'UNSUB'"
                                + " AND t.rx_mo_id != 0"
                                + " AND DATE(t.recv_dt) = DATE(m.last_mod_dt)"
                                + ((oper == CARRIER.TRUEH || oper == CARRIER.DTAC_SDP || oper == CARRIER.CAT)
                                ? " INNER JOIN trns_rx_mo mo ON t.rx_mo_id = mo.rx_mo_id"
                                + " INNER JOIN trns_rx rx ON mo.rx_id = rx.rx_id"
                                + " AND rx.type & " + RX_TYPE.LOCAL_OK.getId()
                                : "");

                        break;
                    case UNSUB_RCHG_ERROR:
                        extra_sql = " INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m "
                                + "  ON m.msisdn = t.msisdn"
                                + " AND m.srvc_main_id = t.srvc_main_id"
                                + " AND t.subtype = 'UNSUB'"
                                + " AND DATE(t.recv_dt) = DATE(m.last_mod_dt)"
                                + ((oper == CARRIER.TRUEH || oper == CARRIER.DTAC_SDP || oper == CARRIER.CAT)
                                ? " INNER JOIN trns_rx_mo mo ON t.rx_mo_id = mo.rx_mo_id"
                                + " INNER JOIN trns_rx rx ON mo.rx_id = rx.rx_id"
                                + " AND (rx.type & " + RX_TYPE.LOCAL_OK.getId() + ") = 0"
                                : "");

                        break;
                    default:
                        return tracks;
                }

                String sql
                        = "   SELECT t.sub_track_id"
                        + "   FROM trns_sub_track t"
                        + extra_sql
                        + "  WHERE t.srvc_main_id=?"
                        + "    AND t.oper_id=?"
                        + "    AND DATE(t.recv_dt)=?"
                        + "  GROUP BY t.msisdn"
                        + "  ORDER BY t.recv_dt ASC";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setInt(2, oper.getId());
                cp.getPreparedStatement().setString(3, DatetimeUtil.print("yyyy-MM-dd", date));

                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    try {
                        SubscriptionTracker tracker = new SubscriptionTracker(rs.getInt(1));

                        if (tracker.sub_track_id > 0) {
                            log.log(Level.INFO, "found sub_track:{0}", Integer.valueOf(tracker.sub_track_id));

                            log.log(Level.INFO, "update sub_track report id {0} row(s).", Integer.valueOf(tracker.updateRelateReportId(trns_sum_id)));
                            log.log(Level.INFO, "update sub_track report type {0} row(s).", Integer.valueOf(tracker.updateReportType(command.getId())));

                            tracks.add(tracker);
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Error finding subscription tracker:{0}", Integer.valueOf(rs.getInt(1)));
                    }
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Find tracking error!!", e);
        }

        return tracks;
    }
}
