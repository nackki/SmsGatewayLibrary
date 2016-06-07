package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceCharge;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_STATUS;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_TYPE;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class SummarizeDtacCdr {

    private static final Logger log = Logger.getLogger(SummarizeDtacCdr.class.getName());

    public void process(Date fdate, Date tdate, int srvc_main_id) {
        process(fdate, tdate, null, srvc_main_id);
    }

    public void process(Date fdate, Date tdate, String dbcp_config, int srvc_main_id) {
        synchronized (SummarizeDtacCdr.class) {
            try {
                DBPoolManager cp;
                if ((dbcp_config == null) || (dbcp_config.isEmpty())) {
                    cp = new DBPoolManager();
                } else {
                    cp = new DBPoolManager(dbcp_config);
                }
                try {
                    int status_code;
                    int value;
                    String chrg_flg;
                    Timestamp timestamp;

                    if (tdate == null) {
                        tdate = fdate;
                    }

                    String sql = 
                            "    SELECT DATE( deliver_dt ), qq.oper_id, qq.srvc_main_id, qq.status_code, qq.chrg, COUNT( * )"
                            + "     FROM ("
                            + "         SELECT q.srvc_main_id, q.oper_id, q.deliver_dt"
                            + "              , IF(dr.status_code IS NULL, 0, IF(dr.status_code = 4, 4, 669)) AS status_code"
                            + "              , q.chrg_flg AS chrg"
                            + "           FROM trns_tx_queue q"
                            + "           LEFT JOIN trns_dlvr_rept dr"
                            + "             ON q.txid = dr.txid"
                            + "            AND q.msisdn = dr.msisdn"
                            + "            AND q.oper_id = dr.oper_id"
                            + "          WHERE q.oper_id IN (" + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                            + (fdate != null 
                            ? "            AND ( q.deliver_dt BETWEEN '" + DatetimeUtil.print("yyyy-MM-dd", fdate) + "'" 
                            + "                  AND '" + DatetimeUtil.print("yyyy-MM-dd", tdate) + " 23:59:59'" 
                            + "                )" 
                            : "") 
                            + (srvc_main_id > 0 
                            ? "            AND q.srvc_main_id = " + srvc_main_id 
                            : "") 
                            + "            AND q.msisdn IS NOT NULL" 
                            + "            AND q.txid IS NOT NULL"
                            + "          GROUP BY q.txid, q.msisdn" 
                            + "         ) qq" 
                            + "   GROUP BY qq.status_code, DATE( deliver_dt ), qq.oper_id , qq.srvc_main_id, qq.chrg" 
                            + "   ORDER BY qq.oper_id, qq.srvc_main_id, DATE( deliver_dt ) , qq.chrg";

                    ResultSet rs = cp.execQuery(sql);
                    int last_srvc_main_id = 0;
                    String last_date = "";
                    ServiceElement se = null;
                    int oper_id;
                    while (rs.next()) {
                        timestamp = rs.getTimestamp(1);
                        oper_id = rs.getInt(2);
                        srvc_main_id = rs.getInt(3);
                        status_code = rs.getInt(4);
                        chrg_flg = rs.getString(5);
                        value = rs.getInt(6);

                        if ((se == null) || (last_srvc_main_id != srvc_main_id)) {
                            se = new ServiceElement(srvc_main_id, oper_id, SERVICE_TYPE.ALL.getId(), 
                                    SERVICE_STATUS.ON.getId() | SERVICE_STATUS.TEST.getId());
                        }

                        if (se.srvc_main_id == 0) {
                            log.log(Level.WARNING, "Service unknown srvc_main_id:{0}!!", Integer.valueOf(srvc_main_id));
                        } else {
                            if ((last_srvc_main_id != se.srvc_main_id) || (!last_date.equals(DatetimeUtil.toDate(timestamp).toString()))) {
                                last_srvc_main_id = se.srvc_main_id;
                                last_date = DatetimeUtil.toDate(timestamp).toString();

                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_total", 0);
                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "rcur_total", 0);
                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_non_chrg_total", 0);
                            }

                            if ((se.srvc_type & ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()) > 0) {
                                if (se.srvc_chrg_type_id == ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()) {
                                    if (chrg_flg.equals("MT")) {
                                        SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_total", value);
                                        switch (status_code) {
                                            case 4:
                                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_balance", value);
                                                break;
                                            case 669:
                                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_error_nocredit", value);
                                                break;
                                            default:
                                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_error_nodr", value);
                                                break;
                                        }
                                    } else {
                                        SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_non_chrg_total", value);
                                    }
                                } else if ((se.srvc_chrg_type_id != ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()) && ((se.srvc_type & ServiceElement.SERVICE_TYPE.NOCSS.getId()) > 0)) {
                                    if (chrg_flg.equals("MT")) {
                                        SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "rcur_total", value);
                                        switch (status_code) {
                                            case 4:
                                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "rcur_balance", value);
                                                break;
                                            case 669:
                                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "rcur_error_nocredit", value);
                                                break;
                                            default:
                                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "rcur_error_nodr", value);
                                                break;
                                        }
                                    } else {
                                        SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_non_chrg_total", value);
                                    }
                                }
                            } else if (chrg_flg.equals("MT")) {
                                SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_total", value);
                                switch (status_code) {
                                    case 4:
                                        SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_balance", value);
                                        break;
                                    case 669:
                                        SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_error_nocredit", value);
                                        break;
                                    default:
                                        SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_error_nodr", value);
                                        break;
                                }
                            } else {
                                SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_non_chrg_total", value);
                            }
                        }
                    }
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "DTAC summarize report failed!!", e);
            }
        }
    }

    public void process_new(Date fdate, Date tdate, int srvc_main_id) {
        synchronized (SummarizeDtacCdr.class) {
            try {
                DBPoolManager cp = new DBPoolManager();
                try {
                    int status_code;
                    int value;
                    Timestamp timestamp;

                    if (tdate == null) {
                        tdate = fdate;
                    }

                    String sql = 
                            "  SELECT a.d, a.oper_id, a.srvc_main_id, a.status_code, SUM( a.c ) AS c"
                            + "  FROM ( SELECT DATE( dr_timestamp ) AS d, dr.oper_id, dr.srvc_main_id"
                            + "     , IF(dr.status_code IS NULL, 0, IF(dr.status_code = 4, 4, 669)) AS status_code"
                            + "     , COUNT( * ) AS c"
                            + "   FROM trns_dlvr_rept dr INNER JOIN srvc_sub ss"
                            + "    ON dr.srvc_main_id = ss.srvc_main_id" 
                            + (srvc_main_id > 0 
                            ? "   AND ss.srvc_main_id = " + srvc_main_id 
                            : "") 
                            + "   AND ss.oper_id = dr.oper_id" 
                            + "   AND ( ss.srvc_type & " + ServiceElement.SERVICE_TYPE.DDS.getId() + " ) = 0" 
                            + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId() 
                            + " INNER JOIN srvc_main sm" 
                            + "    ON ss.srvc_main_id = sm.srvc_main_id" 
                            + "   AND sm.srvc_chrg_type_id = " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId() 
                            + " WHERE dr.oper_id IN (" + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                            + (fdate != null 
                            ? "   AND ( dr_timestamp BETWEEN '" + DatetimeUtil.print("yyyy-MM-dd", fdate) + "'" 
                            + "         AND '" + DatetimeUtil.print("yyyy-MM-dd", tdate) + " 23:59:59'" 
                            + "       )" 
                            : "") 
                            + " GROUP BY DATE( dr_timestamp ), dr.oper_id, dr.srvc_main_id, dr.status_code" 
                            + " HAVING c > 1" 
                            + " ) a" 
                            + " GROUP BY a.d, a.oper_id, a.srvc_main_id, a.status_code" 
                            + " ORDER BY a.oper_id, a.srvc_main_id, a.d";
                    
                    ResultSet rs = cp.execQuery(sql);
                    int last_srvc_main_id = 0;
                    String last_date = "";
                    ServiceElement se = null;
                    int oper_id;
                    while (rs.next()) {
                        timestamp = rs.getTimestamp(1);
                        oper_id = rs.getInt(2);
                        srvc_main_id = rs.getInt(3);
                        status_code = rs.getInt(4);
                        value = rs.getInt(5);

                        if ((se == null) || (last_srvc_main_id != srvc_main_id)) {
                            se = new ServiceElement(srvc_main_id, oper_id, 
                                    SERVICE_TYPE.ALL.getId(), 
                                    SERVICE_STATUS.ON.getId() | SERVICE_STATUS.TEST.getId());
                        }

                        if (se.srvc_main_id == 0) {
                            log.log(Level.WARNING, "Service unknown srvc_main_id:{0}!!", Integer.valueOf(srvc_main_id));
                        } else if (((se.srvc_type & ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()) > 0)
                                && (se.srvc_chrg_type_id == ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId())) {
                            if ((last_srvc_main_id != se.srvc_main_id) || (!last_date.equals(DatetimeUtil.toDate(timestamp).toString()))) {
                                last_srvc_main_id = se.srvc_main_id;
                                last_date = DatetimeUtil.toDate(timestamp).toString();
                                SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_total", 0);
                            }
                            SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_total", value);
                            switch (status_code) {
                                case 4:
                                    SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_balance", value);
                                    break;
                                case 669:
                                    SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_error_nocredit", value);
                                    break;
                                default:
                                    SummaryReport.replaceSummary(se.srvc_main_id, CARRIER.fromId(oper_id), DatetimeUtil.toDate(timestamp), "mt_chrg_error_nodr", value);
                            }
                        }
                    }
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "DTAC summarize report (new process) failed!!", e);
            }
        }
    }
}