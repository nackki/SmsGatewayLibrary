package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class SubscriberFactory {

    private static final Logger logger = Logger.getLogger(SubscriberFactory.class.getName());

    public Subscriber[] getSubscriberList(String[] msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, User user) {
        return getSubscriberList(msisdn, srvc_main_id, oper_id, sort, from, records, user, -1);
    }

    public Subscriber[] getSubscriberList(String[] msisdn, int srvc_main_id, int oper_id, String sort, int from, int records, User user, int status) {
        List subList = new ArrayList();
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

            String whereservice = "";
            whereservice = srvc_main_id > 0 ? " AND m.srvc_main_id = " + srvc_main_id : "";

            String wheremsisdn = "";
            if ((msisdn != null)
                    && (msisdn.length > 0)) {
                for (String m : msisdn) {
                    if (m.matches("^66[689][0-9]{8}$")) {
                        wheremsisdn = wheremsisdn.concat("msisdn='" + m + "' OR ");
                    }
                }
                if (wheremsisdn.indexOf(" OR") > 0) {
                    wheremsisdn = wheremsisdn.substring(0, wheremsisdn.lastIndexOf(" OR"));
                    wheremsisdn = " AND ( " + wheremsisdn + " )";
                }

            }

            String wherestatus = "";
            if (status > -1) {
                wherestatus = " AND m.state = " + status;
            }

            String order = "";
            String sort_field = "";
            if ((sort != null)
                    && (!sort.equals(""))) {
                order = " ORDER BY " + sort;
                sort_field = " , " + (sort.indexOf(" ") >= 0 ? sort.split(" ")[0] : sort);
            }

            String limit = "";
            if (from >= 0) {
                limit = " LIMIT " + from + (records > 0 ? " ," + records : "");
            }

            String sql = "";

            if (oper_id == 0) {
                sql = "SELECT msisdn, m.srvc_main_id, ss.oper_id" + sort_field
                        + "  FROM mmbr_" + CARRIER.DTAC.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND (ss.oper_id = " + CARRIER.DTAC.getId()
                        + "      OR ss.oper_id = " + CARRIER.DTAC_SDP.getId() + " )"
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + whereuid
                        + " WHERE 1"
                        + whereservice
                        + wheremsisdn
                        + wherestatus
                        + " UNION "
                        + "SELECT msisdn, m.srvc_main_id, ss.oper_id" + sort_field
                        + "  FROM mmbr_" + CARRIER.TRUE.name().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + CARRIER.TRUE.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + whereuid
                        + " WHERE 1"
                        + whereservice
                        + wheremsisdn
                        + wherestatus
                        + " UNION "
                        + "SELECT msisdn, m.srvc_main_id, ss.oper_id" + sort_field
                        + "  FROM mmbr_" + CARRIER.TRUEH.name().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + CARRIER.TRUEH.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + whereuid
                        + " WHERE 1"
                        + whereservice
                        + wheremsisdn
                        + wherestatus
                        + " UNION "
                        + "SELECT msisdn, m.srvc_main_id, ss.oper_id" + sort_field
                        + "  FROM mmbr_" + CARRIER.AIS.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ( ss.oper_id = " + CARRIER.AIS.getId()
                        + " OR ss.oper_id = " + CARRIER.AIS_LEGACY.getId() + " )"
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + whereuid
                        + " WHERE 1"
                        + whereservice
                        + wheremsisdn
                        + wherestatus
                        + order
                        + limit;
            } else {
                sql = "SELECT msisdn, m.srvc_main_id, ss.oper_id" + sort_field
                        + "  FROM mmbr_" + CARRIER.fromId(oper_id).toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + oper_id
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + whereuid
                        + " WHERE 1"
                        + whereservice
                        + wheremsisdn
                        + wherestatus
                        + order
                        + limit;
            }

            //logger.info(sql);
            DBPoolManager cp = new DBPoolManager();
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    try {
                        Subscriber sub = new Subscriber(rs.getString(1), rs.getInt(2), CARRIER.fromId(rs.getInt(3)));
                        if (sub != null) {
                            subList.add(sub);
                        }
                    } catch (Exception e) {
                    }
                }
                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return null;
        }

        return (Subscriber[]) subList.toArray(new Subscriber[0]);
    }

    public List<String> getActiveMsisdnList(int srvc_main_id, CARRIER oper) {
        List msisdnList = new ArrayList();

        String sql = "SELECT msisdn  FROM mmbr_" + oper.toString().toLowerCase() + " m"
                + " INNER JOIN srvc_sub s"
                + "    ON s.srvc_main_id = m.srvc_main_id"
                + "   AND s.oper_id = " + oper.getId()
                + "   AND s.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                + " WHERE m.srvc_main_id=" + srvc_main_id
                + "   AND m.state IN ( "
                + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                + ", " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + ")";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    msisdnList.add(rs.getString(1));
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

        return msisdnList;
    }

    public List<Subscriber> getMalformStateSubList(CARRIER oper) {
        List subList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "  SELECT mb.srvc_main_id, mb.msisdn"
                    + "  FROM mmbr_" + oper.toString().toLowerCase() + " mb"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON mb.srvc_main_id = ss.srvc_main_id"
                    + "   AND ss.oper_id = " + oper.getId()
                    + "   AND ss.status = " + ServiceElement.SERVICE_STATUS.ON.getDbId()
                    + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                    + " WHERE 1"
                    + "   AND mb.state NOT IN ("
                    + ((oper == CARRIER.TRUE) || (oper == CARRIER.TRUEH) || (oper == CARRIER.AIS_LEGACY)
                    ? SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + ","
                    : "")
                    + SubscriberGroup.SUB_STATUS.REGISTER.getId() + "," + SubscriberGroup.SUB_STATUS.UNREGISTER.getId() + ")"
                    + " ORDER BY mb.srvc_main_id ASC";

            if (oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP) {
                sql = "SELECT mb.srvc_main_id, mb.msisdn"
                        + "  FROM mmbr_" + oper.toString().toLowerCase() + " mb"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON mb.srvc_main_id = ss.srvc_main_id"
                        + "   AND ss.oper_id = " + oper.getId()
                        + "   AND ss.status = " + ServiceElement.SERVICE_STATUS.ON.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " WHERE (( (ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId() + ") > 0 )"
                        + "   AND mb.state NOT IN (" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + "," + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "," + SubscriberGroup.SUB_STATUS.UNREGISTER.getId()
                        + ") )"
                        + "       OR (( (ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId() + ") = 0 )"
                        + " AND mb.state NOT IN ("
                        + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "," + SubscriberGroup.SUB_STATUS.UNREGISTER.getId()
                        + ") )"
                        + " ORDER BY mb.srvc_main_id ASC";
            }

            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    subList.add(new Subscriber(rs.getString("msisdn"), rs.getInt("srvc_main_id"), oper));
                    System.out.print("malform state sub list size: " + subList.size());
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return subList;
    }

    public List<Subscriber> getWarningSubList(CARRIER oper) {
        List subList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT mb.srvc_main_id, mb.msisdn"
                    + "  FROM mmbr_" + oper.toString().toLowerCase() + " mb"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON mb.srvc_main_id = ss.srvc_main_id"
                    + "   AND ss.oper_id = " + oper.getId()
                    + "   AND ss.status = " + ServiceElement.SERVICE_STATUS.ON.getDbId()
                    + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                    + (oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP
                    ? " AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId()
                    : "")
                    + (oper == CARRIER.AIS
                    ? "   AND ( ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L.getId()
                    + "       OR ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId()
                    + " ) "
                    : "")
                    + " INNER JOIN srvc_main sm"
                    + "    ON ss.srvc_main_id = sm.srvc_main_id"
                    + "   AND sm.price > 0"
                    + "   AND sm.srvc_chrg_type_id != " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                    + " WHERE CURDATE() = DATE_SUB(mb.expired_date, INTERVAL mb.rmdr_ctr DAY)"
                    + "   AND mb.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                    + "   AND mb.rmdr_ctr > 0"
                    + " ORDER BY mb.srvc_main_id ASC";
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    subList.add(new Subscriber(rs.getString("msisdn"), rs.getInt("srvc_main_id"), oper));
                }

                logger.info("warning sub list size: " + subList.size());

                rs.close();

                sql = "  UPDATE mmbr_" + oper.toString().toLowerCase() + " mb"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON mb.srvc_main_id = ss.srvc_main_id"
                        + "   AND ss.oper_id = " + oper.getId()
                        + "   AND ss.status = " + ServiceElement.SERVICE_STATUS.ON.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + (oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP
                        ? "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId()
                        : "")
                        + (oper == CARRIER.AIS
                        ? "   AND ( ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L.getId()
                        + "       OR ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId() + " ) "
                        : "")
                        + " INNER JOIN srvc_main sm"
                        + "   AND sm.price > 0"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id != " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + "   SET mb.rmdr_ctr = mb.rmdr_ctr-1"
                        + " WHERE CURDATE() = DATE_SUB(mb.expired_date, INTERVAL mb.rmdr_ctr DAY)"
                        + "   AND mb.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "   AND mb.rmdr_ctr > 0";

                cp.execUpdate(sql);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return subList;
    }

    public List<Subscriber> getRecurringSubList(CARRIER oper) {
        List subList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT mb.srvc_main_id, msisdn"
                    + "  FROM mmbr_" + oper.toString().toLowerCase() + " mb"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON mb.srvc_main_id = ss.srvc_main_id"
                    + "   AND ss.oper_id = " + oper.getId()
                    + "   AND ss.status = " + ServiceElement.SERVICE_STATUS.ON.getDbId()
                    + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                    + (oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP
                    ? " AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId()
                    : "")
                    + (oper == CARRIER.AIS
                    ? "   AND ( ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L.getId()
                    + "       OR ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId()
                    + " ) "
                    : "")
                    + " INNER JOIN srvc_main sm"
                    + "    ON ss.srvc_main_id = sm.srvc_main_id"
                    + "   AND sm.price > 0"
                    + "   AND sm.srvc_chrg_type_id != " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                    + " WHERE CURDATE() >= mb.expired_date"
                    + "   AND (mb.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                    + " or mb.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + ")"
                    + "   AND mb.extd_ctr > 0" + "   AND CURDATE() < DATE_ADD(mb.expired_date, INTERVAL ss.rchg_ctr DAY)"
                    + " ORDER BY mb.srvc_main_id ASC";
            try {
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    subList.add(new Subscriber(rs.getString("msisdn"), rs.getInt("srvc_main_id"), oper));
                }

                rs.close();

                sql = "    UPDATE mmbr_" + oper.toString().toLowerCase() + " mb"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON mb.srvc_main_id = ss.srvc_main_id"
                        + "   AND ss.oper_id = " + oper.getId()
                        + "   AND ss.status = " + ServiceElement.SERVICE_STATUS.ON.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + (oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP
                        ? " AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId()
                        : "")
                        + (oper == CARRIER.AIS
                        ? "   AND ( ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L.getId()
                        + "       OR ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId() + " ) "
                        : "") + " INNER JOIN srvc_main sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + "   AND sm.price > 0"
                        + "   AND sm.srvc_chrg_type_id != " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + "   SET mb.extd_ctr = mb.extd_ctr-1"
                        + "     , mb.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + " WHERE CURDATE() >= mb.expired_date"
                        + "   AND (mb.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR mb.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + "       )"
                        + "   AND mb.extd_ctr > 0";

                cp.execUpdate(sql);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            return null;
        }
        return subList;
    }

    public int renew(int srvc_main_id, String msisdn, CARRIER oper, SUB_REUSE reuse_flag) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(),
                    ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(),
                    ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

            String sql = "UPDATE mmbr_" + oper.toString().toLowerCase()
                    + "   SET ctnt_ctr=?"
                    + "     , rmdr_ctr=?"
                    + "     , extd_ctr=?"
                    + "     , srvc_chrg_type_id=?"
                    + "     , srvc_chrg_amnt=?"
                    + (se.isSubscriptionMTCharge()
                    ? "     , register_date=curdate()"
                    + "     , state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                    : reuse_flag != SUB_REUSE.OFFLINE
                    ? "    , state="
                    + ((reuse_flag == SUB_REUSE.NOEXP_PROMO) || (se.isSubscriptionMTCharge())
                    ? SubscriberGroup.SUB_STATUS.REGISTER.getId()
                    : SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId())
                    : "")
                    + ((reuse_flag == SUB_REUSE.NOEXP_PROMO) || (se.isSubscriptionMTCharge())
                    ? " , unregister_date=NULL"
                    : " , free_trial=0")
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?"
                    + "   AND (state = " + SubscriberGroup.SUB_STATUS.UNREGISTER.getId()
                    + "        OR state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                    + "        OR ( state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                    + "             AND CURDATE() >= expired_date )"
                    + "       )";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, se.ctnt_ctr);
                cp.getPreparedStatement().setInt(2, se.rmdr_ctr);
                cp.getPreparedStatement().setInt(3, se.rchg_ctr);
                cp.getPreparedStatement().setInt(4, se.srvc_chrg_type_id);
                cp.getPreparedStatement().setInt(5, se.srvc_chrg_amnt);
                cp.getPreparedStatement().setInt(6, srvc_main_id);
                cp.getPreparedStatement().setString(7, msisdn);

                row = cp.execUpdatePrepareStatement();
                logger.log(Level.INFO, "msisdn[{0}] {1} record(s) renewal.", new Object[]{msisdn, row});
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public int recurringSuccess(String msisdn, int srvc_main_id, CARRIER oper) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(), ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());
            String expired_date = "DATE_ADD(CURDATE(), INTERVAL " + ServiceCharge.getChargeInterval(srvc_main_id) + " DAY)";
            String sql = "UPDATE mmbr_" + oper.toString().toLowerCase()
                    + "   SET ctnt_ctr=?"
                    + "     , rmdr_ctr=?"
                    + "     , extd_ctr=?"
                    + "     , unregister_date=NULL"
                    + "     , balanced_date=CURDATE()"
                    + "     , expired_date=" + expired_date
                    + "     , srvc_chrg_type_id=?"
                    + "     , srvc_chrg_amnt=?"
                    + "     , state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                    + "     , free_trial=0"
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, se.ctnt_ctr);
                cp.getPreparedStatement().setInt(2, se.rmdr_ctr);
                cp.getPreparedStatement().setInt(3, se.rchg_ctr);
                cp.getPreparedStatement().setInt(4, se.srvc_chrg_type_id);
                cp.getPreparedStatement().setInt(5, se.srvc_chrg_amnt);
                cp.getPreparedStatement().setInt(6, srvc_main_id);
                cp.getPreparedStatement().setString(7, msisdn);

                row = cp.execUpdatePrepareStatement();
                logger.log(Level.INFO, "msisdn[{0}] {1} record(s) recurring success.", new Object[]{msisdn, row});
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public boolean isMemberValid(int srvc_main_id, String msisdn, CARRIER oper) {
        boolean exist = false;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT COUNT(msisdn)"
                    + "  FROM mmbr_" + oper.toString().toLowerCase() + " mb"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON mb.srvc_main_id = ss.srvc_main_id"
                    + "   AND ss.oper_id = " + oper.getId()
                    + "   AND ss.status != 0"
                    + " WHERE mb.srvc_main_id=" + srvc_main_id
                    + "   AND msisdn='" + msisdn + "'"
                    + "   AND state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId();
            try {
                ResultSet rs = cp.execQuery(sql);
                if ((rs.next()) && (rs.getInt(1) > 0)) {
                    exist = true;
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return exist;
    }

    public SubscriberGroup.SUB_STATUS getSubStatus(int srvc_main_id, String msisdn, CARRIER oper) {
        SubscriberGroup.SUB_STATUS status = null;
        try {
            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(), ServiceElement.SERVICE_TYPE.ALL.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT state"
                    + "  FROM mmbr_" + oper.toString().toLowerCase() + " mb"
                    + " INNER JOIN srvc_sub ss"
                    + "    ON mb.srvc_main_id = ss.srvc_main_id"
                    + "   AND ss.oper_id = " + oper.getId()
                    + "   AND ss.status != 0"
                    + " WHERE mb.srvc_main_id=" + srvc_main_id
                    + "   AND msisdn='" + msisdn + "'";
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    status = SubscriberGroup.SUB_STATUS.fromId(rs.getInt(1));
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return status;
    }

    public SUB_REUSE isMemberReuse(int srvc_main_id, String msisdn, String oper_name) {
        SUB_REUSE status = SUB_REUSE.NOREUSE;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "  SELECT CURDATE() < expired_date"
                    + "     , CURDATE() <= DATE_ADD(register_date, INTERVAL free_trial DAY) AND free_trial != 0"
                    + "     , state  FROM mmbr_" + oper_name.toLowerCase()
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?"
                    + "   AND ( state > 2 OR state = 1 OR (state=2 AND CURDATE() >= expired_date)) ";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, msisdn);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    boolean noexpired = rs.getInt(1) == 1;
                    boolean promo = rs.getInt(2) == 1;
                    if ((noexpired == Boolean.FALSE.booleanValue()) && (rs.getInt(3) == 1)) {
                        status = SUB_REUSE.RETRY_CHARGE;
                    } else if (noexpired == Boolean.FALSE.booleanValue()) {
                        status = SUB_REUSE.EXPIRED;
                    } else if ((noexpired == Boolean.TRUE.booleanValue()) && (promo == Boolean.FALSE.booleanValue())) {
                        status = SUB_REUSE.NOEXP_NOPROMO;
                    } else if ((noexpired == Boolean.TRUE.booleanValue()) && (promo == Boolean.TRUE.booleanValue())) {
                        status = SUB_REUSE.NOEXP_PROMO;
                    } else {
                        status = SUB_REUSE.OFFLINE;
                    }
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return status;
    }

    public boolean isPrepared2Register(int srvc_main_id, String msisdn, String oper_name) {
        boolean status = false;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT *"
                    + "  FROM mmbr_" + oper_name.toLowerCase()
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?"
                    + "   AND state=1";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, msisdn);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    status = true;
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return status;
    }

    public boolean isNew(int srvc_main_id, String msisdn, String oper_name) {
        boolean status = false;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT *"
                    + "  FROM mmbr_" + oper_name.toLowerCase()
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?"
                    + "   AND state=0";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, msisdn);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    status = true;
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return status;
    }

    public int removeSub(int srvc_main_id, String msisdn, String oper_name) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "DELETE"
                    + "  FROM mmbr_" + oper_name.toLowerCase()
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, msisdn);
                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public int chageSubState(int srvc_main_id, String msisdn, String oper_name, SubscriberGroup.SUB_STATUS state) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql
                    = "UPDATE mmbr_" + oper_name.toLowerCase()
                    + " SET state=?"
                    + " WHERE srvc_main_id=?"
                    + "   AND msisdn=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, state.getId());
                cp.getPreparedStatement().setInt(2, srvc_main_id);
                cp.getPreparedStatement().setString(3, msisdn);
                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public void doCleansing(CARRIER oper) {
        offlineChargeFailSub(oper);
        removeExpiredSub(oper);
    }

    /**
     * Offline an expired sub
     *
     * @param oper
     * @return Hash map of subscribers that need the external process send to CSS
     * (TMV/TMH)
     */
    public HashMap<Subscriber, String> offlineChargeFailSub(CARRIER oper) {
        HashMap<Subscriber, String> subList = null;
        try {
            DBPoolManager cp = new DBPoolManager();

            try {

                if ((oper == CARRIER.TRUE) || (oper == CARRIER.TRUEH)) {
                    subList = new HashMap();
                    String sql = "SELECT m.msisdn, m.srvc_main_id, ss.Thrd_prty_unregister"
                            + "  FROM mmbr_" + oper.toString().toLowerCase() + " m"
                            + " INNER JOIN srvc_sub ss"
                            + "    ON ss.srvc_main_id = m.srvc_main_id"
                            + "   AND ss.oper_id = " + oper.getId()
                            + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                            + "   AND NOT ss.srvc_type & " + ServiceElement.SERVICE_TYPE.NOCSS.getId()
                            + " INNER JOIN srvc_main sm"
                            + "    ON ss.srvc_main_id = sm.srvc_main_id"
                            + "   AND sm.srvc_chrg_type_id != " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                            + " WHERE 1"
                            + "   AND ( ( CURDATE() >= m.expired_date"
                            + "         AND m.extd_ctr = 0"
                            + "         AND (m.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                            + " or m.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + ")"
                            + "         )"
                            + "       OR( (m.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                            + " or m.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + ")"
                            + "         AND CURDATE() >= DATE_ADD(m.expired_date, INTERVAL ss.rchg_ctr DAY)"
                            + "         )"
                            + "       )";

                    ResultSet rs = cp.execQuery(sql);
                    try {
                        while (rs.next()) {
                            subList.put(new Subscriber(rs.getString("msisdn"), rs.getInt("srvc_main_id"), oper), rs.getString("Thrd_prty_unregister"));
                        }
                    } finally {
                        rs.close();
                    }
                }

                String upd_sql = "UPDATE mmbr_" + oper.toString().toLowerCase() + " m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + oper.getId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + (oper == CARRIER.DTAC || oper == CARRIER.DTAC_SDP
                        ? " AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.CONTROLCMD.getId()
                        : "")
                        + (oper == CARRIER.AIS
                        ? "   AND ( ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L.getId()
                        + "       OR ss.srvc_type & " + ServiceElement.SSS_TYPE.TYPE_L_PLUS.getId() + " ) "
                        : "")
                        + " INNER JOIN srvc_main sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + "   AND sm.srvc_chrg_type_id != " + ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId()
                        + "   SET m.state = " + SubscriberGroup.SUB_STATUS.UNREGISTER.getId()
                        + "     , m.unregister_date = CURDATE()"
                        + "     , m.ctnt_ctr=0"
                        + "     , m.free_trial=0"
                        + "     , m.rmdr_ctr=0"
                        + "     , m.extd_ctr=0"
                        + " WHERE 1"
                        + "   AND ( ( CURDATE() >= m.expired_date"
                        + "         AND m.extd_ctr = 0"
                        + "         AND (m.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "             OR m.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + "             )"
                        + "         )"
                        + "         OR ( (m.state = " + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "               OR m.state = " + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + "              )"
                        + "         AND CURDATE() >= DATE_ADD(m.expired_date, INTERVAL ss.rchg_ctr DAY)"
                        + "         )"
                        + "       )";
                try {
                    logger.log(Level.INFO, "offline expired {0} sub {1} row(s).", new Object[]{oper.name(), cp.execUpdate(upd_sql)});
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "cleansing process fail!!", e);
                }
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "DB connection error!!", e);
        }

        return subList;
    }

    /**
     * @deprecated
     */
    public int removeExpiredSub(CARRIER oper) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = " REPLACE INTO mmbr_" + oper.toString().toLowerCase() + "_bkup "
                        + " SELECT *"
                        + "   FROM mmbr_" + oper.toString().toLowerCase()
                        + "  WHERE state!=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "    AND unregister_date IS NOT NULL"
                        + "    AND CURDATE() > DATE_ADD(unregister_date, INTERVAL 6 MONTH)";

                cp.execUpdate(sql);
                sql = "DELETE FROM mmbr_" + oper.toString().toLowerCase()
                        + "  WHERE state!=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "   AND CURDATE() > DATE_ADD(unregister_date, INTERVAL 6 MONTH)";

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public int removeInvalidSub(CARRIER oper) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = " INSERT INTO mmbr_" + oper.toString().toLowerCase() + "_bkup "
                        + " SELECT *"
                        + "   FROM mmbr_" + oper.toString().toLowerCase()
                        + "  WHERE state NOT IN (" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "," + SubscriberGroup.SUB_STATUS.UNREGISTER.getId() + ")"
                        + "    AND CURDATE() > DATE_ADD(last_mod_dt, INTERVAL 1 MONTH)";

                cp.execUpdate(sql);
                sql = "DELETE FROM mmbr_" + oper.toString().toLowerCase()
                        + "  WHERE state NOT IN (" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "," + SubscriberGroup.SUB_STATUS.UNREGISTER.getId() + ")"
                        + "    AND CURDATE() > DATE_ADD(last_mod_dt, INTERVAL 1 MONTH)";

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public int backupSub(String msisdn, int srvc_main_id, CARRIER oper) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = " INSERT INTO mmbr_" + oper.toString().toLowerCase() + "_bkup " + " SELECT *" + "   FROM mmbr_" + oper.toString().toLowerCase() + "  WHERE msisdn='" + msisdn + "'" + "    AND srvc_main_id=" + srvc_main_id;

                cp.execUpdate(sql);
                sql = "DELETE FROM mmbr_" + oper.toString().toLowerCase() + "  WHERE msisdn='" + msisdn + "'" + "    AND srvc_main_id=" + srvc_main_id;

                row = cp.execUpdate(sql);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public static String getExpiredDate(int srvc_main_id, String msisdn, String oper_name) {
        String dt = "";
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT DATE_FORMAT(expired_date, '%d/%m/%y') FROM mmbr_" + oper_name.toLowerCase() + " WHERE srvc_main_id=?" + " AND msisdn=?";
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setString(2, msisdn);
                ResultSet rs = cp.execQueryPrepareStatement();
                if ((rs.next()) && (rs.getInt(1) > 0)) {
                    dt = rs.getString(1);
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return dt;
    }

    public static Date getNextExpiredDate(int srvc_main_id) {
        Date dt = null;
        try {
            DBPoolManager cp = new DBPoolManager();

            String sql = "SELECT DATE_ADD(CURDATE(), INTERVAL " + ServiceCharge.getChargeInterval(srvc_main_id) + " DAY)";
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    dt = rs.getDate(1);
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return dt;
    }

    public static String getNextExpiredDate(int srvc_main_id, String format) {
        Date dt = getNextExpiredDate(srvc_main_id);

        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(dt);
    }

    public static int getAmountOfServicesSubscribe(int srvc_main_id, String msisdn, CARRIER oper) {
        int row = 0;

        int service_type = ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                | ServiceElement.SERVICE_TYPE.SMS.getId()
                | ServiceElement.SERVICE_TYPE.WAP.getId()
                | ServiceElement.SERVICE_TYPE.MMS.getId()
                | ServiceElement.SERVICE_TYPE.CONTROLCMD.getId();
        String wheretype = ServiceElement.SERVICE_TYPE.where(service_type, "ss.srvc_type");

        int service_status = ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId();
        String wherestatus = ServiceElement.SERVICE_STATUS.where(service_status, "ss.status");

        String sql
                = "  SELECT COUNT(*)"
                + "  FROM `mmbr_" + oper.toString().toLowerCase() + "` m"
                + " INNER JOIN ("
                + "       SELECT srvc_main_id, srvc_id_mo, oper_id, thrd_prty_register"
                + "         FROM srvc_sub"
                + "        WHERE oper_id=" + oper.getId()
                + "          AND srvc_main_id=" + srvc_main_id
                + "       ) ssm"
                + "    ON m.srvc_main_id = ssm.srvc_main_id"
                + " INNER JOIN srvc_sub ss"
                + "    ON ss.oper_id = ssm.oper_id"
                + "   AND ss.srvc_id_mo = ssm.srvc_id_mo"
                + "   AND ss.thrd_prty_register = ssm.thrd_prty_register"
                + wheretype
                + wherestatus
                + "      WHERE msisdn = '" + msisdn + "'"
                + "        AND ( m.state = 2 "
                + "         OR (m.state=1 AND CURDATE() <= DATE_ADD(expired_date, INTERVAL ss.rchg_ctr DAY)))";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    row = rs.getInt(1);
                }

                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return row;
    }

    public static enum SUB_REUSE {

        NOREUSE,
        OFFLINE,
        RETRY_CHARGE,
        EXPIRED,
        NOEXP_NOPROMO,
        NOEXP_PROMO;
    }
}
