package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class SubscriptionServices {

    private static final Logger log = Logger.getLogger(SubscriptionServices.class.getClass().getName());

    public int isMoRegisterBySrvcIdMo(String srvc_id_mo, String ivr, String sms, int oper_id) {
        int srvc_main_id = 0;

        if (ivr == null) {
            ivr = "";
        }
        if (sms == null) {
            sms = "";
        }

        String wherestatus = ServiceElement.SERVICE_STATUS.where(ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId(), "status");
        String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), "srvc_type");

        String sql =
                "  SELECT srvc_main_id"
                + "  FROM srvc_sub"
                + " WHERE srvc_id_mo=?"
                + "   AND ( (? REGEXP ivr_register AND ivr_register!='')"
                + "       OR (? REGEXP sms_register AND sms_register!='')"
                + "       OR (? = sms_register AND sms_register!='') )"
                + "   AND oper_id = ?"
                + wherestatus
                + wheretype
                + " ORDER BY '1_2sAdb87' REGEXP sms_register ASC, '1_2sAdb87' REGEXP ivr_register ASC";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, srvc_id_mo);
                cp.getPreparedStatement().setString(2, ivr.trim());
                cp.getPreparedStatement().setString(3, sms.trim());
                cp.getPreparedStatement().setString(4, sms.trim());
                cp.getPreparedStatement().setInt(5, oper_id);
                ResultSet rs = cp.execQueryPrepareStatement();

                if (rs.next()) {
                    srvc_main_id = rs.getInt("srvc_main_id");
                }
                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return srvc_main_id;
    }

    public int isMoRegister(String srvc_id, String ivr, String sms, int oper_id) {
        int srvc_main_id = -1;

        if (ivr == null) {
            ivr = "";
        }
        if (sms == null) {
            sms = "";
        }

        String wherestatus = ServiceElement.SERVICE_STATUS.where(ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId(), "status");
        String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), "srvc_type");

        if (oper_id == CARRIER.AIS.getId()) {
            srvc_id = srvc_id.substring(0, 7);
        }

        String sql =
                "   SELECT srvc_main_id"
                + "   FROM srvc_sub"
                + "  WHERE ? IN (srvc_id, srvc_id_mo, srvc_id_mo_test"
                + (oper_id == CARRIER.AIS.getId()
                ? "      , LEFT(srvc_id,7), LEFT(srvc_id_mo,7), LEFT(srvc_id_mo_test,7)"
                : "")
                + "       )"
                + "   AND ( (? REGEXP ivr_register AND ivr_register!='')"
                + "       OR (? REGEXP sms_register AND sms_register!='')"
                + "       OR (? = sms_register AND sms_register!='') )"
                + "   AND oper_id=?"
                + wherestatus
                + wheretype
                + " ORDER BY '1_2sAdb87' REGEXP sms_register ASC, '1_2sAdb87' REGEXP ivr_register ASC";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, srvc_id);
                cp.getPreparedStatement().setString(2, ivr.trim());
                cp.getPreparedStatement().setString(3, sms.trim());
                cp.getPreparedStatement().setString(4, sms.trim());
                cp.getPreparedStatement().setInt(5, oper_id);
                ResultSet rs = cp.execQueryPrepareStatement();

                if (rs.next()) {
                    srvc_main_id = rs.getInt("srvc_main_id");
                }
                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "exception caught", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return srvc_main_id;
    }

    public int isMoRegister(String srvc_id, String sms, int oper_id) {
        return isMoRegister(srvc_id, "", sms, oper_id);
    }

    public int isMoUnregisterBySrvcIdMo(String srvc_id_mo, String ivr, String sms, int oper_id) {
        int srvc_main_id = 0;

        if (ivr == null) {
            ivr = "";
        }
        if (sms == null) {
            sms = "";
        }

        String wherestatus = ServiceElement.SERVICE_STATUS.where(ServiceElement.SERVICE_STATUS.ON.getId() + ServiceElement.SERVICE_STATUS.TEST.getId(), "status");
        String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), "srvc_type");

        String sql = "SELECT srvc_main_id  FROM srvc_sub  WHERE srvc_id_mo=?   AND ( (? REGEXP ivr_unregister AND ivr_unregister!='')       OR (? REGEXP sms_unregister AND sms_unregister!='')       OR (? = sms_unregister AND sms_unregister!='') )   AND oper_id=?" + wherestatus + wheretype + " ORDER BY '1_2sAdb87' REGEXP sms_unregister ASC, '1_2sAdb87' REGEXP ivr_unregister ASC";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, srvc_id_mo);
                cp.getPreparedStatement().setString(2, ivr.trim());
                cp.getPreparedStatement().setString(3, sms.trim());
                cp.getPreparedStatement().setString(4, sms.trim());
                cp.getPreparedStatement().setInt(5, oper_id);
                ResultSet rs = cp.execQueryPrepareStatement();

                if (rs.next()) {
                    srvc_main_id = rs.getInt("srvc_main_id");
                }
                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return srvc_main_id;
    }

    public int isMoUnregister(String srvc_id, String ivr, String sms, int oper_id) {
        int srvc_main_id = -1;

        if (ivr == null) {
            ivr = "";
        }
        if (sms == null) {
            sms = "";
        }

        String wherestatus = ServiceElement.SERVICE_STATUS.where(ServiceElement.SERVICE_STATUS.ON.getId() + ServiceElement.SERVICE_STATUS.TEST.getId(), "status");
        String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), "srvc_type");

        if (oper_id == CARRIER.AIS.getId()) {
            srvc_id = srvc_id.substring(0, 7);
        }

        String sql =
                "  SELECT srvc_main_id"
                + "  FROM srvc_sub"
                + " WHERE ? IN (srvc_id, srvc_id_mo, srvc_id_mo_test"
                + (oper_id == CARRIER.AIS.getId()
                ? "    , LEFT(srvc_id,7), LEFT(srvc_id_mo,7), LEFT(srvc_id_mo_test,7)"
                : "")
                + "       )"
                + "   AND status!=0"
                + "   AND ( (? REGEXP ivr_unregister AND ivr_unregister!='')"
                + "       OR (? REGEXP sms_unregister AND sms_unregister!='')"
                + "       OR (? = sms_unregister AND sms_unregister!='') )"
                + "   AND oper_id=?"
                + wherestatus
                + wheretype
                + " ORDER BY '1_2sAdb87' REGEXP sms_unregister ASC, '1_2sAdb87' REGEXP ivr_unregister ASC";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, srvc_id);
                cp.getPreparedStatement().setString(2, ivr.trim());
                cp.getPreparedStatement().setString(3, sms.trim());
                cp.getPreparedStatement().setString(4, sms.trim());
                cp.getPreparedStatement().setInt(5, oper_id);
                ResultSet rs = cp.execQueryPrepareStatement();

                if ((rs.next()) && (rs.getInt(1) > 0)) {
                    srvc_main_id = rs.getInt("srvc_main_id");
                }
                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "exception caught", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return srvc_main_id;
    }

    public int isMoUnregister(String srvc_id, String sms, int oper_id) {
        return isMoUnregister(srvc_id, "", sms, oper_id);
    }

    public SUB_RESULT doSub(String msisdn, int srvc_main_id, CARRIER oper) throws Exception {
        ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(), ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());
        SUB_RESULT ret = SUB_RESULT.INVALID;
        SubscriberFactory.SUB_REUSE reuse_flg = SubscriberFactory.SUB_REUSE.NOREUSE;

        SubscriberFactory sf = new SubscriberFactory();
        if (sf.isMemberValid(srvc_main_id, msisdn, oper)) {
            ret = SUB_RESULT.DUPLICATED;
        } else if ((reuse_flg = sf.isMemberReuse(srvc_main_id, msisdn, oper.toString())) != SubscriberFactory.SUB_REUSE.NOREUSE) {
            ret = reuse_flg == SubscriberFactory.SUB_REUSE.NOEXP_PROMO ? SUB_RESULT.RENEW_TRIAL : SUB_RESULT.RENEW;
            if (sf.renew(srvc_main_id, msisdn, oper, reuse_flg) != 1) {
                log.log(Level.INFO, "renew process failed for msisdn[{0}], srvc_main_id[{1}]", new Object[]{msisdn, Integer.valueOf(srvc_main_id)});
                ret = SUB_RESULT.INVALID;
            }
        } else {
            DBPoolManager cp = new DBPoolManager();
            try {
                String expired_date = "0000-00-00";
                String sql = "";

                int state = 0;
                String balance = "NULL";

                if (se.free_trial > 0) {
                    ret = SUB_RESULT.NEW_TRIAL;
                    state = SubscriberGroup.SUB_STATUS.REGISTER.getId();
                    expired_date = "DATE_ADD(CURDATE(), INTERVAL " + se.free_trial + " DAY)";
                } else if ((oper == CARRIER.AIS) && ((se.srvc_type & ServiceElement.SSS_TYPE.TYPE_L.getId()) == 0) && ((se.srvc_type & ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId()) == 0)) {
                    ret = SUB_RESULT.NEW;
                    state = SubscriberGroup.SUB_STATUS.REGISTER.getId();
                    expired_date = "DATE_ADD(CURDATE(), INTERVAL " + ServiceCharge.getChargeInterval(srvc_main_id) + " DAY)";
                    balance = "CURDATE()";
                } else if ((oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP) && (!se.isAble2ManageSub())) {
                    ret = SUB_RESULT.NEW;
                    state = SubscriberGroup.SUB_STATUS.REGISTER.getId();
                    expired_date = "DATE_ADD(CURDATE(), INTERVAL " + ServiceCharge.getChargeInterval(srvc_main_id) + " DAY)";
                } else if (se.isSubscriptionMTCharge()) {
                    ret = SUB_RESULT.NEW;
                    state = SubscriberGroup.SUB_STATUS.REGISTER.getId();
                    expired_date = "DATE_ADD(CURDATE(), INTERVAL 1 YEAR)";
                } else {
                    ret = SUB_RESULT.NEW;
                    state = SubscriberGroup.SUB_STATUS.UNREGISTER.getId();
                    expired_date = "CURDATE()";
                }

                sql = "INSERT INTO mmbr_" + oper.toString().toLowerCase()
                        + " (msisdn, srvc_main_id, ctnt_ctr, free_trial, rmdr_ctr, extd_ctr"
                        + " , register_date, unregister_date, expired_date"
                        + " , non_expired, balanced_date, srvc_chrg_type_id, srvc_chrg_amnt, state, last_mod_dt)"
                        + " SELECT ?, ?, ss.ctnt_ctr, ss.free_trial, ss.rmdr_ctr, ss.rchg_ctr, CURDATE(), NULL, "
                        + expired_date
                        + "      , ss.srvc_type, "
                        + balance
                        + "      , s.srvc_chrg_type_id, s.srvc_chrg_amnt"
                        + "      , " + state + ", NULL"
                        + "   FROM srvc_main AS s, srvc_sub AS ss"
                        + "  WHERE s.srvc_main_id=?"
                        + "    AND s.srvc_main_id=ss.srvc_main_id"
                        + "    AND ss.oper_id = " + oper.getId();

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, msisdn);
                cp.getPreparedStatement().setInt(2, srvc_main_id);
                cp.getPreparedStatement().setInt(3, srvc_main_id);

                int row = cp.execUpdatePrepareStatement();
                log.log(Level.INFO, "subscriber {0} record(s) inserted", String.valueOf(row));
            } catch (SQLException e) {
                log.log(Level.INFO, e.getMessage());
                if (e.getMessage().indexOf("Duplicate") >= 0) {
                    ret = SUB_RESULT.DUPLICATED;
                } else {
                    ret = SUB_RESULT.INVALID;
                }
            } finally {
                cp.release();
            }
        }
        return ret;
    }

    public static Set<Integer> getRelatedActiveServices(String srvc_id, String msisdn, CARRIER oper) {
        Set tmp = new LinkedHashSet();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), "ss.srvc_type");
            String wherestatus = ServiceElement.SERVICE_STATUS.where(ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId(), "ss.status");
            try {
                String sql = "SELECT ss.srvc_main_id  FROM srvc_sub AS ss INNER JOIN mmbr_" + oper.toString().toLowerCase() + " m" + "    ON ss.srvc_main_id = m.srvc_main_id" + "   AND m.msisdn='" + msisdn + "'" + "   AND m.state IN (" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + "," + SubscriberGroup.SUB_STATUS.REGISTER.getId() + ")" + "   AND ( " + "         (ss.srvc_id='" + srvc_id + "' AND ss.status=" + ServiceElement.SERVICE_STATUS.ON.getDbId() + ")" + "         OR (ss.srvc_id_mo_test='" + srvc_id + "' AND ss.status=" + ServiceElement.SERVICE_STATUS.TEST.getDbId() + ")" + "       )" + (oper != null ? "   AND ss.oper_id=" + oper.getId() : "") + wheretype + wherestatus + " ORDER BY ss.srvc_main_id";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    tmp.add(Integer.valueOf(rs.getInt(1)));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return tmp;
    }

    public SUB_RESULT doUnsub(String msisdn, int srvc_main_id, CARRIER oper) throws Exception {
        SUB_RESULT ret = SUB_RESULT.INVALID;

        DBPoolManager cp = new DBPoolManager();
        try {
            int state = SubscriberGroup.SUB_STATUS.UNREGISTER.getId();

            String sql = "  UPDATE mmbr_" + oper.toString().toLowerCase() 
                    + "   SET state=" + state 
                    + "     , unregister_date=CURDATE()" 
                    + "     , ctnt_ctr=0" 
                    + "     , free_trial=IF(CURDATE() > DATE_ADD(register_date, INTERVAL free_trial DAY), 0, free_trial)" 
                    + "     , rmdr_ctr=0" 
                    + "     , extd_ctr=0" 
                    + " WHERE msisdn=? " 
                    + "   AND srvc_main_id=?"
                    + "   AND state!=" + state ;

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setString(1, msisdn);
            cp.getPreparedStatement().setInt(2, srvc_main_id);

            int row = cp.execUpdatePrepareStatement();
            if (row > 0) {
                ret = SUB_RESULT.SUCCESS;
            }
            log.log(Level.INFO, "msisdn[{0}] {1} row offline.", new Object[]{msisdn, Integer.valueOf(row)});
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }

        return ret;
    }

    public static enum SUB_RESULT {

        NEW,
        NEW_TRIAL,
        RENEW,
        RENEW_TRIAL,
        DUPLICATED,
        INVALID,
        SUCCESS;
    }
}