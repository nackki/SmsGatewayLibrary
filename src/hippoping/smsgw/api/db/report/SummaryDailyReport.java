package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import lib.common.DBPoolManager;

public class SummaryDailyReport extends SummaryReport
        implements Serializable {

    protected java.util.Date date;

    public java.util.Date getDate() {
        return this.date;
    }

    public SummaryDailyReport() {
    }

    public SummaryDailyReport(int srvc_main_id, java.util.Date date, OperConfig.CARRIER oper)
            throws Exception {
        this.date = date;
        this.serviceElement = new ServiceElement(srvc_main_id, oper != null ? oper.getId() : 0, ServiceElement.SERVICE_TYPE.ALL.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereOper = (oper != null) && (oper != OperConfig.CARRIER.ALL) ? " AND oper_id=" + oper.getId() : "";
                String whereSrvc = srvc_main_id > 0 ? " AND r.srvc_main_id=" + srvc_main_id : "";
                String sql = "SELECT r.*, s.name  FROM rept_trns_sum r INNER JOIN srvc_main s    ON r.srvc_main_id = s.srvc_main_id WHERE sum_date = DATE(?)" + whereOper + whereSrvc;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setDate(1, new java.sql.Date(date.getTime()));
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