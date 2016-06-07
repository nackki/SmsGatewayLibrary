package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lib.common.DBPoolManager;

public class SummaryMonthlyReportFactory {

    protected int from_month;
    protected int from_year;
    protected int to_month;
    protected int to_year;
    protected List<SummaryMonthlyReport> SummaryMonthlyReportList;

    public List<SummaryMonthlyReport> getSummaryMonthlyReportList() {
        return this.SummaryMonthlyReportList;
    }

    public SummaryMonthlyReportFactory(int from_y, int from_m, int to_y, int to_m, int srvc_main_id, ServiceElement.SERVICE_TYPE type, User user) throws Exception {
        this.from_year = from_y;
        this.from_month = from_m;
        this.to_year = to_y;
        this.to_month = to_m;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereuid = "";
                if (user != null) {
                    whereuid = " AND ( 0";
                    for (int i = 0; i < user.getChildUid().length; i++) {
                        if (!user.getChildUid()[i].isEmpty()) {
                            whereuid = whereuid + " OR ss.uid=" + user.getChildUid()[i];
                        }
                    }
                    whereuid = whereuid + " )";
                }

                String whereSrvc = srvc_main_id > 0 ? " AND r.srvc_main_id=" + srvc_main_id : "";
                String wheretype = ServiceElement.SERVICE_TYPE.where(type.getId(), "ss.srvc_type");
                String sql = "  SELECT YEAR(sum_date)     , MONTH(sum_date)" + (srvc_main_id > 0 ? "" : ", r.srvc_main_id") + "  FROM rept_trns_sum r" + " INNER JOIN srvc_sub ss" + "    ON ss.srvc_main_id = r.srvc_main_id" + "   AND ss.oper_id = r.oper_id" + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId() + wheretype + whereuid + " WHERE 1" + "   AND (YEAR(sum_date)*100 + MONTH(sum_date)) >= ?" + "   AND (YEAR(sum_date)*100 + MONTH(sum_date)) <= ?" + whereSrvc + " GROUP BY YEAR(sum_date), MONTH(sum_date)" + (srvc_main_id > 0 ? "" : ", r.srvc_main_id");

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, from_y * 100 + from_m);
                cp.getPreparedStatement().setInt(2, to_y * 100 + to_m);
                ResultSet rs = cp.execQueryPrepareStatement();
                this.SummaryMonthlyReportList = new ArrayList();
                while (rs.next()) {
                    try {
                        SummaryMonthlyReport report = new SummaryMonthlyReport(rs.getInt(1), rs.getInt(2), null, srvc_main_id > 0 ? srvc_main_id : rs.getInt("srvc_main_id"));

                        if (report != null) {
                            this.SummaryMonthlyReportList.add(report);
                        }
                    } catch (Exception e) {
                    }
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

    public SummaryMonthlyReportFactory(int from_y, int from_m, int to_y, int to_m, OperConfig.CARRIER oper, int srvc_main_id, ServiceElement.SERVICE_TYPE type, User user) throws Exception {
        this.from_year = from_y;
        this.from_month = from_m;
        this.to_year = to_y;
        this.to_month = to_m;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereuid = "";
                if (user != null) {
                    whereuid = " AND ( 0";
                    for (String childUid : user.getChildUid()) {
                        if (!childUid.isEmpty()) {
                            whereuid = whereuid + " OR ss.uid=" + childUid;
                        }
                    }
                    whereuid = whereuid + " )";
                }

                String whereOper = (oper != null) && (oper != OperConfig.CARRIER.ALL) ? " AND r.oper_id=" + oper.getId() : "";
                String whereSrvc = srvc_main_id > 0 ? " AND r.srvc_main_id=" + srvc_main_id : "";
                String wheretype = ServiceElement.SERVICE_TYPE.where(type.getId(), "ss.srvc_type");
                String sql
                        = "  SELECT YEAR(sum_date)"
                        + "     , MONTH(sum_date)"
                        + (srvc_main_id > 0 ? "" : ", r.srvc_main_id")
                        + "  FROM rept_trns_sum r"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = r.srvc_main_id"
                        + "   AND ss.oper_id = r.oper_id"
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + wheretype
                        + whereuid
                        + " WHERE 1"
                        + "   AND (YEAR(sum_date)*100 + MONTH(sum_date)) >= ?"
                        + "   AND (YEAR(sum_date)*100 + MONTH(sum_date)) <= ?"
                        + whereSrvc
                        + whereOper
                        + " GROUP BY YEAR(sum_date), MONTH(sum_date)"
                        + (srvc_main_id > 0 ? "" : ", r.srvc_main_id");

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, from_y * 100 + from_m);
                cp.getPreparedStatement().setInt(2, to_y * 100 + to_m);
                ResultSet rs = cp.execQueryPrepareStatement();
                this.SummaryMonthlyReportList = new ArrayList();
                while (rs.next()) {
                    try {
                        SummaryMonthlyReport report = new SummaryMonthlyReport(
                                rs.getInt(1), rs.getInt(2),
                                oper,
                                srvc_main_id > 0 ? srvc_main_id : rs.getInt("srvc_main_id"));

                        this.SummaryMonthlyReportList.add(report);
                    } catch (Exception e) {
                    }
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
