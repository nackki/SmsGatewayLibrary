package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceCharge;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.SubscriberGroup;
import hippoping.smsgw.api.db.TxQueue;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class SummaryReport implements Serializable {

    private static final Logger log = Logger.getLogger(SummaryReport.class.getName());
    protected ServiceElement serviceElement;
    protected int[] sub_total;
    protected int[] sub_ft;
    protected int[] sub_balance;
    protected int[] sub_error_nocredit;
    protected int[] sub_error_nodr;
    protected int[] unsub_total;
    protected int[] unsub_rchg_error;
    protected int[] unsub_req;
    protected int[] rcur_total;
    protected int[] rcur_balance;
    protected int[] rcur_error_nocredit;
    protected int[] rcur_error_nodr;
    protected int[] warn_total;
    protected int[] mt_chrg_total;
    protected int[] mt_chrg_balance;
    protected int[] mt_chrg_error_nocredit;
    protected int[] mt_chrg_error_nodr;
    protected int[] mt_non_chrg_total;
    private CARRIER[] ALL_OPER = {CARRIER.DTAC, CARRIER.TRUE, CARRIER.AIS_LEGACY, CARRIER.AIS, CARRIER.TRUEH, CARRIER.DTAC_SDP, CARRIER.CAT};

    public SummaryReport() {
        int size = this.ALL_OPER.length + 1;
        this.sub_total = new int[size];
        this.sub_ft = new int[size];
        this.sub_balance = new int[size];
        this.sub_error_nocredit = new int[size];
        this.sub_error_nodr = new int[size];
        this.unsub_total = new int[size];
        this.unsub_rchg_error = new int[size];
        this.unsub_req = new int[size];
        this.rcur_total = new int[size];
        this.rcur_balance = new int[size];
        this.rcur_error_nocredit = new int[size];
        this.rcur_error_nodr = new int[size];
        this.warn_total = new int[size];
        this.mt_chrg_total = new int[size];
        this.mt_chrg_balance = new int[size];
        this.mt_chrg_error_nocredit = new int[size];
        this.mt_chrg_error_nodr = new int[size];
        this.mt_non_chrg_total = new int[size];

        for (CARRIER oper : this.ALL_OPER) {
            this.sub_total[oper.getId()] = 0;
            this.sub_ft[oper.getId()] = 0;
            this.sub_balance[oper.getId()] = 0;
            this.sub_error_nocredit[oper.getId()] = 0;
            this.sub_error_nodr[oper.getId()] = 0;
            this.unsub_total[oper.getId()] = 0;
            this.unsub_rchg_error[oper.getId()] = 0;
            this.unsub_req[oper.getId()] = 0;
            this.rcur_total[oper.getId()] = 0;
            this.rcur_balance[oper.getId()] = 0;
            this.rcur_error_nocredit[oper.getId()] = 0;
            this.rcur_error_nodr[oper.getId()] = 0;
            this.warn_total[oper.getId()] = 0;
            this.mt_chrg_total[oper.getId()] = 0;
            this.mt_chrg_balance[oper.getId()] = 0;
            this.mt_chrg_error_nocredit[oper.getId()] = 0;
            this.mt_chrg_error_nodr[oper.getId()] = 0;
            this.mt_non_chrg_total[oper.getId()] = 0;
        }
    }

    public ServiceElement getServiceElement() {
        return this.serviceElement;
    }

    public int getSub_total(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.sub_total[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.sub_total[c.getId()];
        }
        return sum;
    }

    public int getSub_ft(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.sub_ft[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.sub_ft[c.getId()];
        }
        return sum;
    }

    public int getSub_balance(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.sub_balance[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.sub_balance[c.getId()];
        }
        return sum;
    }

    public int getSub_error_nocredit(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.sub_error_nocredit[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.sub_error_nocredit[c.getId()];
        }
        return sum;
    }

    public int getSub_error_nodr(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.sub_error_nodr[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.sub_error_nodr[c.getId()];
        }
        return sum;
    }

    public int getUnsub_total(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.unsub_total[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.unsub_total[c.getId()];
        }
        return sum;
    }

    public int getUnsub_rchg_error(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.unsub_rchg_error[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.unsub_rchg_error[c.getId()];
        }
        return sum;
    }

    public int getUnsub_req(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.unsub_req[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.unsub_req[c.getId()];
        }
        return sum;
    }

    public int getMt_chrg_balance(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.mt_chrg_balance[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.mt_chrg_balance[c.getId()];
        }
        return sum;
    }

    public int getMt_chrg_error_nocredit(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.mt_chrg_error_nocredit[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.mt_chrg_error_nocredit[c.getId()];
        }
        return sum;
    }

    public int getMt_chrg_error_nodr(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.mt_chrg_error_nodr[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.mt_chrg_error_nodr[c.getId()];
        }
        return sum;
    }

    public int getMt_chrg_total(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.mt_chrg_total[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.mt_chrg_total[c.getId()];
        }
        return sum;
    }

    public int getMt_non_chrg_total(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.mt_non_chrg_total[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.mt_non_chrg_total[c.getId()];
        }
        return sum;
    }

    public int getRcur_balance(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.rcur_balance[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.rcur_balance[c.getId()];
        }
        return sum;
    }

    public int getRcur_error_nocredit(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.rcur_error_nocredit[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.rcur_error_nocredit[c.getId()];
        }
        return sum;
    }

    public int getRcur_error_nodr(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.rcur_error_nodr[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.rcur_error_nodr[c.getId()];
        }
        return sum;
    }

    public int getRcur_total(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.rcur_total[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.rcur_total[c.getId()];
        }
        return sum;
    }

    public int getWarn_total(CARRIER oper) {
        int sum = 0;
        if (oper != null) {
            return this.warn_total[oper.getId()];
        }
        for (CARRIER c : this.ALL_OPER) {
            sum += this.warn_total[c.getId()];
        }
        return sum;
    }

    private static int createSummary(int srvc_main_id, CARRIER oper, java.util.Date date)
            throws Exception {
        int id = -1;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "INSERT IGNORE INTO rept_trns_sum (srvc_main_id, oper_id, sum_date) VALUES (?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setInt(2, oper.getId());
                cp.getPreparedStatement().setDate(3, new java.sql.Date(date.getTime()));

                int rows = cp.execUpdatePrepareStatement();
                if (rows == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    try {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    } finally {
                        rs.close();
                    }
                } else {
                    id = getId(srvc_main_id, oper, date);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "create summary error!!", e);
            throw e;
        }

        return id;
    }

    public static int replaceSummary(int srvc_main_id, CARRIER oper, java.util.Date date, String field, int value) throws Exception {
        log.log(Level.INFO, "replaceSummary: srvc_main_id={0}|{1}|{2}|{3}|value={4}",
                new Object[]{srvc_main_id, oper, DatetimeUtil.print("yyyy-MM-dd", date), field, value});
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "  UPDATE rept_trns_sum"
                        + "   SET " + field + "= " + value
                        + " WHERE srvc_main_id=?"
                        + "   AND oper_id=?"
                        + "   AND sum_date=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setInt(2, oper.getId());
                cp.getPreparedStatement().setString(3, d);

                row = cp.getPreparedStatement().executeUpdate();
                log.log(Level.INFO, "summary updated {0} row(s).", row);

                if (row == 0) {
                    createSummary(srvc_main_id, oper, date);

                    row = cp.getPreparedStatement().executeUpdate();
                    log.log(Level.INFO, "summary inserted {0} row(s).", row);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public static int updateSummary(int srvc_main_id, CARRIER oper, java.util.Date date, String field, int value) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql = "UPDATE rept_trns_sum"
                        + "   SET " + field + "= " + field + "+" + value
                        + " WHERE srvc_main_id=?"
                        + "   AND oper_id=?"
                        + "   AND sum_date=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setInt(2, oper.getId());
                cp.getPreparedStatement().setString(3, d);

                row = cp.getPreparedStatement().executeUpdate();

                if (row == 0) {
                    createSummary(srvc_main_id, oper, date);

                    row = cp.getPreparedStatement().executeUpdate();
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    public static int getId(int srvc_main_id, CARRIER oper, java.util.Date date) {
        int id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql = "  SELECT trns_sum_id  FROM rept_trns_sum rs"
                        + (oper == CARRIER.AIS
                        ? " INNER JOIN srvc_sub ss"
                        + "    ON rs.oper_id = ss.oper_id"
                        + "   AND rs.srvc_main_id = ss.srvc_main_id"
                        : "")
                        + " WHERE rs.srvc_main_id="
                        + srvc_main_id
                        + "   AND rs.oper_id"
                        + (oper == CARRIER.AIS
                        ? " IN (" + CARRIER.AIS.getId() + "," + CARRIER.AIS_LEGACY.getId() + ")"
                        : new StringBuilder().append("=").append(oper.getId()).toString())
                        + "   AND sum_date='" + d + "'";

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        return id;
    }

    public static void summarize(java.util.Date date, COMMAND command) throws Exception {
        if (command.isDisabled()) {
            return;
        }

        switch (command) {
            case SUB_TOTAL:
                summarizeSubTotal(date);
                break;
            case SUB_FT:
                summarizeSubFreetrail(date);
                break;
            case SUB_BALANCE:
                summarizeSubBalance(date);
                break;
            case SUB_ERROR_NOCREDIT:
                summarizeSubErrorNocredit(date);
                break;
            case SUB_ERROR_NODR:
                summarizeSubErrorNoDR(date);
                break;
            case UNSUB_TOTAL:
                summarizeUnsubTotal(date);
                break;
            case UNSUB_RCHG_ERROR:
                summarizeUnsubRechargeError(date);
                break;
            case UNSUB_REQUEST:
                summarizeUnsubRequest(date);
                break;
            case RCUR_TOTAL:
                summarizeRecurringTotal(date);
                break;
            case RCUR_BALANCE:
                summarizeRecurringBalance(date);
                break;
            case RCUR_ERROR_NOCREDIT:
                summarizeRecurringErrorNocredit(date);
                break;
            case RCUR_ERROR_NODR:
                summarizeRecurringErrorNoDR(date);
                break;
            case WARN_TOTAL:
                summarizeWarningTotal(date);
                break;
            case MT_CHRG_TOTAL:
                summarizeMTChargeTotal(date);
                break;
            case MT_CHRG_BALANCE:
                summarizeMTChargeBalance(date);
                break;
            case MT_CHRG_ERROR_NOCREDIT:
                summarizeMTChargeErrorNoCredit(date);
                break;
            case MT_CHRG_ERROR_NODR:
                summarizeMTChargeErrorNoDR(date);
                break;
            case MT_NON_CHRG_TOTAL:
                summarizeMTNonChargeTotal(date);
        }
    }

    private static void summarizeWithTracker(java.util.Date date, COMMAND command)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), "ss.srvc_type");
                String sql = "  SELECT ss.srvc_main_id, ss.oper_id  FROM srvc_sub ss WHERE ss.status != 0" + wheretype;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    CARRIER oper = CARRIER.fromId(rs.getInt(2));
                    int srvc_main_id = rs.getInt(1);
                    int id = createSummary(srvc_main_id, oper, date);
                    List tracks = SubscriptionTrackerFactory.find(srvc_main_id, oper, date, id, command);
                    log.log(Level.INFO, "find tracking for service main ID:{0}[{1}] => found {2} row(s)"
                            , new Object[]{Integer.valueOf(srvc_main_id), oper, Integer.valueOf(tracks.size())});

                    replaceSummary(srvc_main_id, oper, date, command.getFieldName(), tracks.size());
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on summarizeWithTracker!!", e);
            throw e;
        }
    }

    private static void summarizeSubTotal(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.SUB_TOTAL);
    }

    private static void summarizeSubFreetrail(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.SUB_FT);
    }

    private static void summarizeSubBalance(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "  SELECT ss.srvc_main_id, ss.oper_id AS oper, COUNT(msisdn) AS c"
                        + "  FROM mmbr_" + CARRIER.DTAC.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id IN ( " + CARRIER.DTAC.getId() + " , " + CARRIER.DTAC_SDP.getId() + " )"
                        + "   AND ss.status != 0"
                        + " WHERE state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + " GROUP BY m.srvc_main_id"
                        + " UNION "
                        + "SELECT srvc_main_id, " + CARRIER.TRUE.getId() + " AS oper, COUNT( msisdn ) AS c"
                        + "  FROM mmbr_" + CARRIER.TRUE.name().toLowerCase()
                        + " WHERE state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "    OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + " AND extd_ctr>0)" + " GROUP BY srvc_main_id"
                        + " UNION "
                        + "SELECT srvc_main_id, " + CARRIER.TRUEH.getId() + " AS oper, COUNT( msisdn ) AS c"
                        + "  FROM mmbr_" + CARRIER.TRUEH.name().toLowerCase()
                        + " WHERE state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "    OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0)"
                        + " GROUP BY srvc_main_id"
                        + " UNION "
                        + "SELECT ss.srvc_main_id, ss.oper_id AS oper, COUNT(msisdn) AS c"
                        + "  FROM mmbr_" + CARRIER.AIS.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id IN ( " + CARRIER.AIS_LEGACY.getId() + " , " + CARRIER.AIS.getId() + " )"
                        + "   AND ss.status != 0"
                        + " WHERE state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "    OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0)"
                        + " GROUP BY m.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "sub_balance", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeSubErrorNocredit(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.SUB_ERROR_NOCREDIT);
    }

    private static void summarizeSubErrorNoDR(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.SUB_ERROR_NODR);
    }

    public static void summarizeUnsubTotal(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.UNSUB_TOTAL);
    }

    private static void summarizeUnsubRechargeError(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.UNSUB_RCHG_ERROR);
    }

    private static void summarizeUnsubRequest(java.util.Date date)
            throws Exception {
        summarizeWithTracker(date, COMMAND.UNSUB_REQUEST);
    }

    private static void summarizeRecurringTotal(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql = "SELECT q.srvc_main_id, q.oper_id, COUNT( q.tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = q.srvc_main_id"
                        + "   AND ss.oper_id = q.oper_id"
                        + "   AND ss.srvc_type&1"
                        + " INNER JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = ss.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id!=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00'"
                        + "   AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_total", rs.getInt(3));
                }

                sql = "SELECT m.srvc_main_id, ss.oper_id, COUNT( msisdn ) AS c"
                        + "  FROM mmbr_" + CARRIER.DTAC.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = m.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id!=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + "   AND ss.oper_id IN ( " + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                        + "   AND ss.srvc_type&1"
                        + " WHERE m.balanced_date = '" + d + "'"
                        + "    OR m.expired_date < '" + d + "'"
                        + "   AND m.state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + " GROUP BY m.srvc_main_id, ss.oper_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_total", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeRecurringBalance(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "   SELECT a.srvc_main_id, a.oper_id, COUNT( * )"
                        + "    FROM ("
                        + "        SELECT dr.*"
                        + "           FROM trns_tx_queue q"
                        + "         INNER JOIN trns_dlvr_rept dr ON q.txid = dr.txid"
                        + "           AND q.oper_id = dr.oper_id"
                        + "           AND q.srvc_main_id = dr.srvc_main_id"
                        + "         WHERE piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "           AND q.oper_id IN (" + CARRIER.TRUE.getId()
                        + ", " + CARRIER.TRUEH.getId() + ")"
                        + "           AND q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "           AND dr.status_code =0"
                        + "         GROUP BY dr.txid, dr.status_code"
                        + "        ) a"
                        + "  GROUP BY a.srvc_main_id, a.oper_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_balance", rs.getInt(3));
                }

                sql = "SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q WHERE q.deliver_dt >= '" + d + " 00:00:00'"
                        + "   AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.oper_id = " + CARRIER.AIS_LEGACY.getId()
                        + "   AND q.status_desc = 'OK'"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_balance", rs.getInt(3));
                }

                sql = "SELECT m.srvc_main_id, ss.oper_id, COUNT( msisdn ) AS c"
                        + "  FROM mmbr_" + CARRIER.DTAC.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = m.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id!=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + "   AND ss.oper_id IN ( " + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                        + " WHERE m.balanced_date = '" + d + "'"
                        + "   AND m.state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + " GROUP BY m.srvc_main_id, ss.oper_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_balance", rs.getInt(3));
                }

                // SSS_TYPEL+
                sql = "SELECT q.srvc_main_id, COUNT( * ) AS c"
                        + "  FROM  trns_dr_cdg dr"
                        + " INNER JOIN trns_tx_queue q"
                        + "    ON dr.GMessageID = q.txid"
                        + "   AND dr.MSISDN = q.msisdn"
                        + "   AND q.oper_id = " + CARRIER.AIS.getId()
                        + " INNER JOIN srvc_main sm"
                        + "    ON q.srvc_main_id = sm.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id !=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00'"
                        + "   AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND SSSActionReport  IN (  'RECURRING', 'REGISTER' )"
                        + "   AND StatusText = 'external:success'"
                        + " GROUP BY  q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.AIS, date, "rcur_balance", rs.getInt(2));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeRecurringErrorNocredit(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "   SELECT a.srvc_main_id, a.oper_id, COUNT( * )"
                        + "   FROM ("
                        + "        SELECT dr.*"
                        + "          FROM trns_tx_queue q"
                        + "         INNER JOIN trns_dlvr_rept dr"
                        + "            ON q.txid = dr.txid"
                        + "           AND q.oper_id = dr.oper_id"
                        + "           AND q.srvc_main_id = dr.srvc_main_id"
                        + "         WHERE piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "           AND q.oper_id IN (" + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId() + ")"
                        + "           AND q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "           AND dr.status_code !=0"
                        + "         GROUP BY dr.txid, dr.status_code"
                        + "        ) a"
                        + "  GROUP BY a.srvc_main_id, a.oper_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_error_nocredit", rs.getInt(3));
                }

                sql = "SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.oper_id = " + CARRIER.AIS_LEGACY.getId()
                        + "   AND q.status_desc = 'ERR|INSUFFICIENT_BALANCE'"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_error_nocredit", rs.getInt(3));
                }

                sql = "SELECT m.srvc_main_id, ss.oper_id, COUNT( msisdn ) AS c"
                        + "  FROM mmbr_" + CARRIER.DTAC.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = m.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id!=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + "   AND ss.oper_id IN ( " + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                        + " WHERE m.expired_date < DATE( '" + d + "' )"
                        + "   AND m.state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + " GROUP BY m.srvc_main_id, ss.oper_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_error_nocredit", rs.getInt(3));
                }

                // SSS_TYPEL+
                sql = "SELECT q.srvc_main_id, COUNT( * ) AS c"
                        + "  FROM  trns_dr_cdg dr"
                        + " INNER JOIN trns_tx_queue q"
                        + "    ON dr.GMessageID = q.txid"
                        + "   AND dr.MSISDN = q.msisdn"
                        + "   AND q.oper_id = " + CARRIER.AIS.getId()
                        + " INNER JOIN srvc_main sm"
                        + "    ON q.srvc_main_id = sm.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id !=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00'"
                        + "   AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND SSSActionReport  IN (  'RECURRING', 'REG_SUCCESS' )"
                        + "   AND StatusText != 'external:success'"
                        + " GROUP BY  q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.AIS, date, "rcur_balance", rs.getInt(2));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeRecurringErrorNoDR(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql = "SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + "  LEFT JOIN trns_dlvr_rept dr"
                        + "    ON q.txid = dr.txid"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.oper_id IN (" + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId() + ")"
                        + "   AND dr.txid IS NULL"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "rcur_error_nodr", rs.getInt(3));
                }

                // SSS_TYPEL+
                sql = "SELECT q.srvc_main_id, COUNT( * ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " INNER JOIN srvc_main sm"
                        + "    ON q.srvc_main_id = sm.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id !=" + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + "  LEFT JOIN trns_dr_cdg dr"
                        + "    ON q.txid = dr.GMessageID"
                        + "   AND q.msisdn = dr.msisdn"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00'"
                        + "   AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority = " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.oper_id = " + CARRIER.AIS.getId()
                        + "   AND dr.GMessageID IS NULL"
                        + " GROUP BY  q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.AIS, date, "rcur_balance", rs.getInt(2));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeWarningTotal(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql = "SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND piority = " + TxQueue.TX_TYPE.WARNING.getId()
                        + "   AND q.oper_id IN ("
                        + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId()
                        + ", " + CARRIER.DTAC.getId() + ", " + CARRIER.DTAC_SDP.getId()
                        + ", " + CARRIER.AIS_LEGACY.getId() + ")"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "warn_total", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeMTChargeTotal(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "  SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND piority != " + TxQueue.TX_TYPE.WARNING.getId()
                        + "   AND chrg_flg = 'MT'"
                        + "   AND msisdn IS NOT NULL"
                        + "   AND q.oper_id IN ("
                        + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId()
                        + ", " + CARRIER.DTAC.getId() + ", " + CARRIER.DTAC_SDP.getId()
                        + ")"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_total", rs.getInt(3));
                }

                sql = "    SELECT q.srvc_main_id"
                        + "     , q.oper_id"
                        + "     , COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00'"
                        + "   AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.piority != " + TxQueue.TX_TYPE.WARNING.getId()
                        + "   AND q.chrg_flg = 'MT'"
                        + "   AND q.msisdn IS NOT NULL"
                        + "   AND q.oper_id = " + CARRIER.AIS_LEGACY.getId()
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_total", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeMTChargeBalance(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "   SELECT a.srvc_main_id, a.oper_id, COUNT( * )"
                        + "    FROM ("
                        + "        SELECT dr.*"
                        + "           FROM trns_tx_queue q"
                        + "         INNER JOIN trns_dlvr_rept dr"
                        + "             ON q.txid = dr.txid"
                        + "           AND q.oper_id = dr.oper_id"
                        + "           AND q.srvc_main_id = dr.srvc_main_id"
                        + "         WHERE piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "           AND q.chrg_flg = 'MT'"
                        + "           AND q.oper_id IN (" + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId() + ")"
                        + "           AND q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "           AND dr.status_code =0"
                        + "         GROUP BY dr.txid, dr.status_code"
                        + "        ) a" + "  GROUP BY a.srvc_main_id, a.oper_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_balance", rs.getInt(3));
                }

                sql =
                        "     SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "   FROM trns_tx_queue q"
                        + "  INNER JOIN trns_dlvr_rept dr"
                        + "     ON q.txid = dr.txid"
                        + "    AND q.oper_id = dr.oper_id"
                        + "    AND q.srvc_main_id = dr.srvc_main_id"
                        + "  WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "    AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "    AND q.chrg_flg = 'MT'"
                        + "    AND q.msisdn IS NOT NULL"
                        + "    AND q.oper_id IN (" + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                        + "    AND dr.status_code = 4"
                        + "  GROUP BY q.srvc_main_id, q.oper_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_balance", rs.getInt(3));
                }

                sql = "    SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.chrg_flg = 'MT'"
                        + "   AND q.msisdn IS NOT NULL"
                        + "   AND q.oper_id = " + CARRIER.AIS_LEGACY.getId()
                        + "   AND q.status_desc = 'OK'"
                        + " GROUP BY q.srvc_main_id, q.oper_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_balance", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeMTChargeErrorNoCredit(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "   SELECT a.srvc_main_id, a.oper_id, COUNT( * )"
                        + "   FROM ("
                        + "        SELECT dr.*"
                        + "          FROM trns_tx_queue q"
                        + "         INNER JOIN trns_dlvr_rept dr"
                        + "            ON q.txid = dr.txid"
                        + "           AND q.oper_id = dr.oper_id"
                        + "           AND q.srvc_main_id = dr.srvc_main_id"
                        + "         WHERE piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "           AND q.chrg_flg = 'MT'"
                        + "           AND q.oper_id IN (" + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId() + ")"
                        + "           AND q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "           AND dr.status_code IN (3,4)"
                        + "         GROUP BY dr.txid"
                        + "        ) a"
                        + "  GROUP BY a.srvc_main_id, a.oper_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_error_nocredit", rs.getInt(3));
                }

                sql = "     SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "   FROM trns_tx_queue q"
                        + "  INNER JOIN trns_dlvr_rept dr"
                        + "     ON q.txid = dr.txid"
                        + "    AND q.oper_id = dr.oper_id"
                        + "    AND q.srvc_main_id = dr.srvc_main_id"
                        + "  WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "    AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "    AND q.chrg_flg = 'MT'"
                        + "    AND q.msisdn IS NOT NULL"
                        + "    AND q.oper_id IN (" + CARRIER.DTAC.getId() + "," + CARRIER.DTAC_SDP.getId() + ")"
                        + "    AND dr.status_code != 4"
                        + "  GROUP BY q.srvc_main_id, q.oper_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_error_nocredit", rs.getInt(3));
                }

                sql = "    SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.piority != " + TxQueue.TX_TYPE.WARNING.getId()
                        + "   AND q.chrg_flg = 'MT'"
                        + "   AND q.msisdn IS NOT NULL"
                        + "   AND q.oper_id = " + CARRIER.AIS_LEGACY.getId()
                        + "   AND q.status_desc = 'ERR|INSUFFICIENT_BALANCE'"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_error_nocredit", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeMTChargeErrorNoDR(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql =
                        "  SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + "  LEFT JOIN trns_dlvr_rept dr"
                        + "    ON q.txid = dr.txid"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.piority != " + TxQueue.TX_TYPE.WARNING.getId()
                        + "   AND q.chrg_flg = 'MT'"
                        + "   AND q.msisdn IS NOT NULL"
                        + "   AND q.oper_id IN ("
                        + CARRIER.TRUE.getId() + ", " + CARRIER.TRUEH.getId()
                        + ", " + CARRIER.DTAC.getId() + ", " + CARRIER.DTAC_SDP.getId()
                        + ")"
                        + "   AND ( dr.txid IS NULL )"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    replaceSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_chrg_error_nodr", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void summarizeMTNonChargeTotal(java.util.Date date)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String date_fmt = "yyyy-MM-dd";
                String d = new SimpleDateFormat(date_fmt).format(date);
                String sql = "SELECT q.srvc_main_id, q.oper_id, COUNT( tx_queue_id ) AS c"
                        + "  FROM trns_tx_queue q"
                        + " WHERE q.deliver_dt >= '" + d + " 00:00:00' AND q.deliver_dt <= '" + d + " 23:59:59'"
                        + "   AND q.piority != " + TxQueue.TX_TYPE.RECURRING.getId()
                        + "   AND q.piority != " + TxQueue.TX_TYPE.WARNING.getId()
                        + "   AND ( (q.chrg_flg = 'MO' AND msisdn IS NOT NULL AND oper_id=" + CARRIER.TRUE.getId() + ")"
                        + "       OR (q.chrg_flg = 'MO' AND msisdn IS NOT NULL AND oper_id=" + CARRIER.TRUEH.getId() + ")"
                        + "       OR (q.chrg_flg = 'MO' AND msisdn IS NOT NULL AND oper_id=" + CARRIER.AIS_LEGACY.getId() + ")"
                        + "       OR (q.chrg_flg = 'MO' AND msisdn IS NULL AND oper_id=" + CARRIER.AIS.getId() + ")"
                        + "       OR (q.chrg_flg = 'MO' AND msisdn IS NULL AND oper_id=" + CARRIER.DTAC.getId() + ")"
                        + "       OR (q.chrg_flg = 'MO' AND msisdn IS NULL AND oper_id=" + CARRIER.DTAC_SDP.getId() + ")"
                        + "       )"
                        + " GROUP BY q.oper_id, q.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    updateSummary(rs.getInt(1), CARRIER.fromId(rs.getInt(2)), date, "mt_non_chrg_total", rs.getInt(3));
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static enum COMMAND {

        SUB_TOTAL(0, "sub_total"),
        SUB_FT(1, "sub_ft"),
        SUB_BALANCE(2, "sub_balance"),
        SUB_ERROR_NOCREDIT(3, "sub_error_nocredit"),
        SUB_ERROR_NODR(4, "sub_error_nodr", true),
        UNSUB_TOTAL(5, "unsub_total"),
        UNSUB_RCHG_ERROR(6, "unsub_rchg_error"),
        UNSUB_REQUEST(7, "unsub_req"),
        RCUR_TOTAL(8, "rcur_total"),
        RCUR_BALANCE(9, "rcur_balance"),
        RCUR_ERROR_NOCREDIT(10, "rcur_error_nocredit"),
        RCUR_ERROR_NODR(11, "rcur_error_nodr"),
        WARN_TOTAL(12, "warn_total"),
        MT_CHRG_TOTAL(13, "mt_chrg_total"),
        MT_CHRG_BALANCE(14, "mt_chrg_balance"),
        MT_CHRG_ERROR_NOCREDIT(15, "mt_chrg_error_nocredit"),
        MT_CHRG_ERROR_NODR(16, "mt_chrg_error_nodr"),
        MT_NON_CHRG_TOTAL(17, "mt_non_chrg_total"),
        ALL(18, "");
        private final int id;
        private final String fieldname;
        private final int disable;

        private COMMAND(int id, String fieldname, boolean disable) {
            this.id = id;
            this.fieldname = fieldname;
            this.disable = (disable ? 1 : 0);
        }

        private COMMAND(int id, String fieldname) {
            this(id, fieldname, false);
        }

        public int getId() {
            return this.id;
        }

        public String getFieldName() {
            return this.fieldname;
        }

        public boolean isDisabled() {
            return this.disable > 0;
        }

        public static COMMAND fromId(int id) {
            for (COMMAND e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}