package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class SummaryDailyReportFactory {

    private static final Logger log = Logger.getLogger(SubscriptionTrackerFactory.class.getName());
    protected Date from;
    protected Date to;
    protected List<SummaryDailyReport> summaryDailyReportList;

    public List<SummaryDailyReport> getSubscriptionSummaryDailyReportList() {
        return this.summaryDailyReportList;
    }

    public SummaryDailyReportFactory(Date from, Date to, ServiceElement.SERVICE_TYPE type, User user) throws Exception {
        this.from = from;
        this.to = to;
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

                String date_fmt = "yyyy-MM-dd";
                String sfrom = new SimpleDateFormat(date_fmt).format(from);
                String sto = new SimpleDateFormat(date_fmt).format(to);
                String wheretype = ServiceElement.SERVICE_TYPE.where(type.getId(), "ss.srvc_type");
                String sql = "SELECT r.srvc_main_id     , r.sum_date  FROM rept_trns_sum r INNER JOIN srvc_sub ss    ON ss.srvc_main_id = r.srvc_main_id   AND ss.oper_id = r.oper_id   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId() + wheretype + whereuid + " WHERE 1" + "   AND r.sum_date BETWEEN DATE(?) AND DATE(?)" + " GROUP BY r.srvc_main_id, r.sum_date";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, sfrom);
                cp.getPreparedStatement().setString(2, sto);
                ResultSet rs = cp.execQueryPrepareStatement();
                this.summaryDailyReportList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.summaryDailyReportList.add(new SummaryDailyReport(rs.getInt("srvc_main_id"), rs.getDate("sum_date"), null));
                    } catch (Exception e) {
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public SummaryDailyReportFactory(Date from, Date to, int srvc_main_id, ServiceElement.SERVICE_TYPE type, User user) throws Exception {
        this.from = from;
        this.to = to;
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

                String date_fmt = "yyyy-MM-dd";
                String sfrom = new SimpleDateFormat(date_fmt).format(from);
                String sto = new SimpleDateFormat(date_fmt).format(to);
                String whereSrvc = srvc_main_id > 0 ? " AND r.srvc_main_id=" + srvc_main_id : "";
                String wheretype = ServiceElement.SERVICE_TYPE.where(type.getId(), "ss.srvc_type");
                String sql = "SELECT r.srvc_main_id     , r.sum_date  FROM rept_trns_sum r INNER JOIN srvc_sub ss    ON ss.srvc_main_id = r.srvc_main_id   AND ss.oper_id = r.oper_id   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId() + wheretype + whereuid + " WHERE 1" + "   AND r.sum_date BETWEEN DATE(?) AND DATE(?)" + whereSrvc + " GROUP BY r.srvc_main_id, r.sum_date";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, sfrom);
                cp.getPreparedStatement().setString(2, sto);
                ResultSet rs = cp.execQueryPrepareStatement();
                this.summaryDailyReportList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.summaryDailyReportList.add(new SummaryDailyReport(rs.getInt("srvc_main_id"), rs.getDate("sum_date"), null));
                    } catch (Exception e) {
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public SummaryDailyReportFactory(Date from, Date to, OperConfig.CARRIER oper, int srvc_main_id, ServiceElement.SERVICE_TYPE type, User user) throws Exception {
        this.from = from;
        this.to = to;
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

                String date_fmt = "yyyy-MM-dd";
                String sfrom = new SimpleDateFormat(date_fmt).format(from);
                String sto = new SimpleDateFormat(date_fmt).format(to);
                String whereOper = (oper != null) && (oper != OperConfig.CARRIER.ALL) ? " AND r.oper_id=" + oper.getId() : "";
                String whereSrvc = srvc_main_id > 0 ? " AND r.srvc_main_id=" + srvc_main_id : "";
                String wheretype = ServiceElement.SERVICE_TYPE.where(type.getId(), "ss.srvc_type");
                String sql = "SELECT r.srvc_main_id     , r.sum_date  FROM rept_trns_sum r INNER JOIN srvc_sub ss    ON ss.srvc_main_id = r.srvc_main_id   AND ss.oper_id = r.oper_id   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId() + wheretype + whereuid + " WHERE 1" + "   AND r.sum_date BETWEEN DATE(?) AND DATE(?)" + whereOper + whereSrvc + " GROUP BY r.srvc_main_id, r.sum_date";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, sfrom);
                cp.getPreparedStatement().setString(2, sto);
                ResultSet rs = cp.execQueryPrepareStatement();
                this.summaryDailyReportList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.summaryDailyReportList.add(new SummaryDailyReport(rs.getInt("srvc_main_id"), rs.getDate("sum_date"), oper));
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "cannot get summaryDailyReport object!!", e);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }
}