package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import lib.common.DBPoolManager;

public class SummaryMonthlyReport extends SummaryReport implements Serializable {

    protected int month;
    protected int year;

    public int getMonth() {
        return this.month;
    }

    public int getYear() {
        return this.year;
    }

    public SummaryMonthlyReport(int year, int month, OperConfig.CARRIER oper, int srvc_main_id)
            throws Exception {
        this.month = month;
        this.year = year;
        this.serviceElement = new ServiceElement(srvc_main_id, oper != null ? oper.getId() : 0, ServiceElement.SERVICE_TYPE.ALL.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereOper = (oper != null) && (oper != OperConfig.CARRIER.ALL) ? " AND r.oper_id=" + oper.getId() : "";
                String whereSrvc = srvc_main_id > 0 ? " AND r.srvc_main_id=" + srvc_main_id : "";
                String sql = "  SELECT s.name"
                        + "     , oper_id"
                        + "     , SUM(sub_total) AS sub_total"
                        + "     , SUM(sub_ft) AS sub_ft"
                        + "     , SUM(sub_balance) AS sub_balance"
                        + "     , SUM(sub_error_nocredit) AS sub_error_nocredit"
                        + "     , SUM(sub_error_nodr) AS sub_error_nodr"
                        + "     , SUM(unsub_total) AS unsub_total"
                        + "     , SUM(unsub_rchg_error) AS unsub_rchg_error"
                        + "     , SUM(unsub_req) AS unsub_req"
                        + "     , SUM(rcur_total) AS rcur_total"
                        + "     , SUM(rcur_balance) AS rcur_balance"
                        + "     , SUM(rcur_error_nocredit) AS rcur_error_nocredit"
                        + "     , SUM(rcur_error_nodr) AS rcur_error_nodr"
                        + "     , SUM(warn_total) AS warn_total"
                        + "     , SUM(mt_chrg_total) AS mt_chrg_total"
                        + "     , SUM(mt_chrg_balance) AS mt_chrg_balance"
                        + "     , SUM(mt_chrg_error_nocredit) AS mt_chrg_error_nocredit"
                        + "     , SUM(mt_chrg_error_nodr) AS mt_chrg_error_nodr"
                        + "     , SUM(mt_non_chrg_total) AS mt_non_chrg_total"
                        + "  FROM rept_trns_sum r INNER JOIN srvc_main s"
                        + "    ON r.srvc_main_id = s.srvc_main_id"
                        + " WHERE MONTH(sum_date) = " + month 
                        + "   AND YEAR(sum_date) = " + year 
                        + whereSrvc 
                        + whereOper 
                        + " GROUP BY MONTH(sum_date)" 
                        + "     , YEAR(sum_date)" 
                        + "     , r.srvc_main_id" 
                        + "     , r.oper_id";

                cp.prepareStatement(sql);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    do {
                        oper = OperConfig.CARRIER.fromId(rs.getInt("oper_id"));
                        if (oper == OperConfig.CARRIER.AIS_LEGACY) {
                            oper = OperConfig.CARRIER.AIS;
                        }
                        this.sub_total[oper.getId()] += rs.getInt("sub_total");
                        this.sub_ft[oper.getId()] += rs.getInt("sub_ft");
                        this.sub_balance[oper.getId()] += rs.getInt("sub_balance");
                        this.sub_error_nocredit[oper.getId()] += rs.getInt("sub_error_nocredit");
                        this.sub_error_nodr[oper.getId()] += rs.getInt("sub_error_nodr");
                        this.unsub_total[oper.getId()] += rs.getInt("unsub_total");
                        this.unsub_rchg_error[oper.getId()] += rs.getInt("unsub_rchg_error");
                        this.unsub_req[oper.getId()] += rs.getInt("unsub_req");
                        this.rcur_total[oper.getId()] += rs.getInt("rcur_total");
                        this.rcur_balance[oper.getId()] += rs.getInt("rcur_balance");
                        this.rcur_error_nocredit[oper.getId()] += rs.getInt("rcur_error_nocredit");
                        this.rcur_error_nodr[oper.getId()] += rs.getInt("rcur_error_nodr");
                        this.warn_total[oper.getId()] += rs.getInt("warn_total");
                        this.mt_chrg_total[oper.getId()] += rs.getInt("mt_chrg_total");
                        this.mt_chrg_balance[oper.getId()] += rs.getInt("mt_chrg_balance");
                        this.mt_chrg_error_nocredit[oper.getId()] += rs.getInt("mt_chrg_error_nocredit");
                        this.mt_chrg_error_nodr[oper.getId()] += rs.getInt("mt_chrg_error_nodr");
                        this.mt_non_chrg_total[oper.getId()] += rs.getInt("mt_non_chrg_total");
                    } while (rs.next());
                } else {
                    throw new Exception("no data found!!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }
}