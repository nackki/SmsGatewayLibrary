package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceCharge.SRVC_CHRG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.StringUtil;

public class ServiceElement
        implements Serializable {

    private static final Logger log = Logger.getLogger(ServiceElement.class.getName());
    public int srvc_main_id;
    public int oper_id;
    public String srvc_id;
    public int srvc_type;
    public String ivr_register;
    public String ivr_unregister;
    public String sms_register;
    public String sms_unregister;
    public String thrd_prty_register;
    public String thrd_prty_unregister;
    public String sender;
    public OperConfig oper_config;
    public OperConfig oper_config_non_chrg;
    public OperConfig oper_config_test;
    public short free_trial;
    public short ctnt_ctr;
    public short rmdr_ctr;
    public short rchg_ctr;
    public String msg_sub_ft;
    public String msg_usub_ft;
    public String msg_warn_ft;
    public String msg_sub_nm;
    public String msg_usub_nm;
    public String msg_warn_nm;
    public String msg_err_no_srvc;
    public String msg_err_dup;
    public int status;
    public int price;
    public int srvc_chrg_type_id;
    public int srvc_chrg_amnt;
    public String srvc_id_non_chrg;
    public String srvc_id_mo;
    public String srvc_id_mo_test;
    public String srvc_id_mt;
    public String srvc_id_mt_chrg;
    public String bcast_srvc_id;
    public String chrg_flg;
    public String srvc_name;
    protected AisLegacyCommand aisLegacyCommand;
    protected User owner;
    public int priority;

    public User getOwner() {
        return this.owner;
    }

    public AisLegacyCommand getAisLegacyCommand() {
        return this.aisLegacyCommand;
    }

    public ServiceElement() {
    }

    @Override
    public int hashCode() {
        return this.oper_id * 1000 + this.srvc_main_id;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof ServiceElement)) {
            ServiceElement se = (ServiceElement) obj;

            return hashCode() == se.hashCode();
        }
        return false;
    }

    public ServiceElement(int srvc_main_id, int oper_id, int type, int status) throws Exception {
        this(srvc_main_id, oper_id, type, status, null);
    }

    public ServiceElement(int srvc_main_id, int oper_id, int type, int status, User user) throws Exception {
        this.srvc_main_id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");

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
            
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + " WHERE 1"
                        + "   AND ss.srvc_main_id=" + srvc_main_id
                        + (oper_id > 0 ? "   AND ss.oper_id=" + oper_id : "")
                        + wheretype
                        + wherestatus
                        + whereuid
                        + " ORDER BY FIELD( ss.oper_id"
                        + " ," + CARRIER.AIS.getId()
                        + " ," + CARRIER.TRUEH.getId()
                        + " ," + CARRIER.TRUE.getId()
                        + " ," + CARRIER.DTAC_SDP.getId()
                        + " ," + CARRIER.DTAC.getId()
                        + " )"
                        + " LIMIT 1";

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    this.srvc_main_id = rs.getInt("srvc_main_id");
                    this.srvc_id = rs.getString("srvc_id");
                    this.oper_id = rs.getInt("oper_id");
                    this.srvc_type = rs.getInt("srvc_type");
                    this.ivr_register = rs.getString("ivr_register");
                    this.ivr_unregister = rs.getString("ivr_unregister");
                    this.sms_register = rs.getString("sms_register");
                    this.sms_unregister = rs.getString("sms_unregister");
                    this.thrd_prty_register = rs.getString("thrd_prty_register");
                    this.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    this.sender = rs.getString("sender");
                    this.free_trial = rs.getShort("free_trial");
                    this.ctnt_ctr = rs.getShort("ctnt_ctr");
                    this.rmdr_ctr = rs.getShort("rmdr_ctr");
                    this.rchg_ctr = rs.getShort("rchg_ctr");
                    this.msg_sub_ft = rs.getString("msg_sub_ft");
                    this.msg_usub_ft = rs.getString("msg_usub_ft");
                    this.msg_warn_ft = rs.getString("msg_warn_ft");
                    this.msg_sub_nm = rs.getString("msg_sub_nm");
                    this.msg_usub_nm = rs.getString("msg_usub_nm");
                    this.msg_warn_nm = rs.getString("msg_warn_nm");
                    this.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    this.msg_err_dup = rs.getString("msg_err_dup");
                    this.status = rs.getInt("status");
                    this.price = rs.getInt("price");
                    this.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    this.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    this.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    this.srvc_id_mo = rs.getString("srvc_id_mo");
                    this.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    this.srvc_id_mt = rs.getString("srvc_id_mt");
                    this.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    this.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    this.chrg_flg = rs.getString("chrg_flg");
                    this.oper_config = new OperConfig(rs.getInt("conf_id"));
                    this.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    this.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    this.srvc_name = rs.getString("name");
                    this.priority = rs.getInt("priority");

                    this.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (oper_id == CARRIER.AIS_LEGACY.getId()) {
                            this.aisLegacyCommand = new AisLegacyCommand(this.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }

                    fillMessageVariables();
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public ServiceElement(String srvc_id, CARRIER oper, int type, int status) throws Exception {
        this(srvc_id, oper, type, status, null);
    }

    public ServiceElement(String srvc_id, CARRIER oper, int type, int status, User user) throws Exception {
        this.srvc_main_id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");

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
            
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + " WHERE 1"
                        + "   AND ("
                        + "          (ss.srvc_id='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.ON.getDbId() + ")"
                        + "         OR (ss.srvc_id_mo_test='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.TEST.getDbId() + ")"
                        + (((oper == CARRIER.AIS) && ((type & SERVICE_TYPE.SMSDOWNLOAD.getDbId()) > 0))
                        || (type == SERVICE_TYPE.ALL.getId())
                        ? "         OR (ss.srvc_type & " + SERVICE_TYPE.SMSDOWNLOAD.getDbId()
                        + "            AND ( (LEFT(ss.srvc_id, 7)='" + srvc_id.substring(0, 7) + "'"
                        + "                       AND ss.status=" + SERVICE_STATUS.ON.getDbId() + ")"
                        + "                  OR"
                        + "                  (LEFT(ss.srvc_id_mo_test, 7)='" + srvc_id.substring(0, 7) + "'"
                        + "                       AND ss.status=" + SERVICE_STATUS.TEST.getDbId() + ")"
                        + "                )"
                        + "            )"
                        : "")
                        + "       )"
                        + (oper != null ? "   AND ss.oper_id=" + oper.getId() : "")
                        + wheretype
                        + wherestatus
                        + whereuid
                        + " ORDER BY ss.oper_id DESC"
                        + " LIMIT 1";

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) {
                    this.srvc_main_id = rs.getInt("srvc_main_id");
                    this.srvc_id = rs.getString("srvc_id");
                    this.oper_id = rs.getInt("oper_id");
                    this.srvc_id = rs.getString("srvc_id");
                    this.srvc_type = rs.getInt("srvc_type");
                    this.ivr_register = rs.getString("ivr_register");
                    this.ivr_unregister = rs.getString("ivr_unregister");
                    this.sms_register = rs.getString("sms_register");
                    this.sms_unregister = rs.getString("sms_unregister");
                    this.thrd_prty_register = rs.getString("thrd_prty_register");
                    this.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    this.sender = rs.getString("sender");
                    this.free_trial = rs.getShort("free_trial");
                    this.ctnt_ctr = rs.getShort("ctnt_ctr");
                    this.rmdr_ctr = rs.getShort("rmdr_ctr");
                    this.rchg_ctr = rs.getShort("rchg_ctr");
                    this.msg_sub_ft = rs.getString("msg_sub_ft");
                    this.msg_usub_ft = rs.getString("msg_usub_ft");
                    this.msg_warn_ft = rs.getString("msg_warn_ft");
                    this.msg_sub_nm = rs.getString("msg_sub_nm");
                    this.msg_usub_nm = rs.getString("msg_usub_nm");
                    this.msg_warn_nm = rs.getString("msg_warn_nm");
                    this.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    this.msg_err_dup = rs.getString("msg_err_dup");
                    this.status = rs.getInt("status");
                    this.price = rs.getInt("price");
                    this.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    this.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    this.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    this.srvc_id_mo = rs.getString("srvc_id_mo");
                    this.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    this.srvc_id_mt = rs.getString("srvc_id_mt");
                    this.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    this.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    this.oper_config = new OperConfig(rs.getInt("conf_id"));
                    this.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    this.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    this.chrg_flg = rs.getString("chrg_flg");
                    this.srvc_name = rs.getString("name");
                    this.priority = rs.getInt("priority");

                    this.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (this.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            this.aisLegacyCommand = new AisLegacyCommand(this.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    fillMessageVariables();
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static List<ServiceElement> getAllService(CARRIER oper, int type, int status) {
        List selist = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wherestatus = SERVICE_STATUS.where(status, "status");
            String wheretype = SERVICE_TYPE.where(type, "srvc_type");
            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND oper_id=" + oper.getId();
            }
            try {
                String sql =
                        "  SELECT srvc_main_id"
                        + "     , oper_id"
                        + "  FROM srvc_sub"
                        + " WHERE 1"
                        + wherestatus
                        + whereoper
                        + wheretype;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement(rs.getInt(1), rs.getInt(2), type, status);
                    if (se.srvc_main_id > 0) {
                        selist.add(se);
                    }
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return selist;
    }

    public static List<ServiceElement> getAllServiceMain(int oper_id, int srvc_main_id) {
        return getAllServiceMain(oper_id, srvc_main_id, null);
    }

    public static List<ServiceElement> getAllServiceMain(int oper_id, int srvc_main_id, String search) {
        List selist = new ArrayList();
        String whereoper = "";
        String wheresrvc = "";
        String wheresrvcid = "";

        if (oper_id > 0) {
            whereoper = " AND oper_id=" + oper_id;
        }
        if (srvc_main_id > 0) {
            wheresrvc = " AND sm.srvc_main_id=" + srvc_main_id;
        }
        if ((search != null) && (!search.isEmpty())) {
            wheresrvcid = " AND (ss.srvc_id LIKE '%" + search + "%' OR sm.name LIKE '%" + search + "%')";
        }
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "  SELECT DISTINCT sm.srvc_main_id, name, price, srvc_chrg_amnt, srvc_chrg_type_id"
                        + "  FROM srvc_main sm"
                        + "  LEFT JOIN srvc_sub ss"
                        + "    ON sm.srvc_main_id = ss.srvc_main_id"
                        + whereoper
                        + " WHERE 1"
                        + wheresrvcid
                        + wheresrvc;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.srvc_name = rs.getString("name");
                    se.price = rs.getInt("price");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    selist.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return selist;
    }

    public static List<ServiceElement> getRelatedServiceElementList(int srvc_main_id, CARRIER oper, int type, int status) {
        List srvcList = new ArrayList();
        try {
            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND oper_id=" + oper.getId();
            }

            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "  SELECT srvc_main_id, oper_id"
                        + "  FROM  ctnt_mngr_map"
                        + " WHERE db_code"
                        + "       IN ("
                        + "         SELECT db_code"
                        + "           FROM ctnt_mngr_map"
                        + "          WHERE srvc_main_id =" + srvc_main_id
                        + "       )"
                        + whereoper;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement(rs.getInt(1), rs.getInt(2), type, status);

                    if ((se != null) && (se.srvc_main_id > 0)) {
                        srvcList.add(se);
                    }
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return srvcList;
    }

    public static ServiceElement[] getServiceElementList(String srvc_id, CARRIER oper, int type, int status) {
        List srvcList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheresrvcid = "";
            if (srvc_id != null) {
                wheresrvcid = "   AND ( (ss.srvc_id='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.ON.getDbId() + ")"
                        + "          OR (ss.srvc_id_mo_test='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.TEST.getDbId() + ") )";
            }

            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND ss.oper_id=" + oper.getId();
            }

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + " WHERE 1"
                        + wherestatus
                        + whereoper
                        + wheresrvcid
                        + wheretype
                        + " ORDER BY ss.srvc_type&1 DESC";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementListByBcastId(String bcast_srvc_id, CARRIER oper, int type, int status) {
        List srvcList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheresrvcid = "";
            if (bcast_srvc_id != null) {
                wheresrvcid = " AND ss.bcast_srvc_id=" + bcast_srvc_id;
            }

            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND ss.oper_id=" + oper.getId();
            }

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + " WHERE 1"
                        + wherestatus
                        + whereoper
                        + wheresrvcid
                        + wheretype;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementList(String srvc_id, CARRIER oper, int type, int status, User user) {
        List srvcList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

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

            String wheresrvcid = "";
            if (srvc_id != null) {
                wheresrvcid = "   AND ( (ss.srvc_id='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.ON.getDbId() + ")"
                        + "          OR (ss.srvc_id_mo_test='" + srvc_id + "' AND ss.status=" + SERVICE_STATUS.TEST.getDbId() + ") )";
            }

            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND ss.oper_id=" + oper.getId();
            }

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + " WHERE 1"
                        + wherestatus
                        + whereoper
                        + wheresrvcid
                        + wheretype
                        + whereuid
                        + " ORDER BY ABS( oper_id -4 ) ASC, oper_id =3 DESC, ss.srvc_id, sm.name ";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementList(CARRIER oper, String srvc_id_mo, int type, int status) {
        List srvcList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheresrvcid = "";
            if (srvc_id_mo != null) {
                wheresrvcid = " AND ss.srvc_id_mo='" + srvc_id_mo + "'";
            }

            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND ss.oper_id=" + oper.getId();
            }

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + wherestatus
                        + whereoper
                        + wheresrvcid
                        + wheretype;
                
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementListByHybridServiceID(String srvc_id_hybrid, int type, int status) {
        List srvcList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String wheresrvcid = "";
            if (srvc_id_hybrid != null) {
                wheresrvcid = " AND (ss.thrd_prty_register='" + srvc_id_hybrid + "'"
                        + " OR ss.thrd_prty_unregister='" + srvc_id_hybrid + "')";
            }

            String whereoper = " AND ss.oper_id=" + CARRIER.AIS.getId();

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub AS ss"
                        + " INNER JOIN srvc_main AS sm"
                        + "    ON ss.srvc_main_id = sm.srvc_main_id"
                        + wherestatus
                        + whereoper
                        + wheresrvcid
                        + wheretype;
                
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementListByServiceIdMo(String srvc_id_mo, String msisdn, CARRIER oper, int type, int status) {
        List srvcList = new ArrayList();
        try {
            String oper_name = oper.toString().toLowerCase();
            DBPoolManager cp = new DBPoolManager();

            if ((srvc_id_mo == null) || (srvc_id_mo.isEmpty())) {
                return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
            }
            String wheresrvcid = " AND ss.srvc_id_mo='" + srvc_id_mo + "'";

            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND ss.oper_id=" + oper.getId();
            }

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub ss"
                        + "  LEFT JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = ss.srvc_main_id"
                        + ((msisdn != null) && (!msisdn.isEmpty())
                        ? " INNER JOIN mmbr_" + oper_name + " AS mbr"
                        + "    ON ss.srvc_main_id = mbr.srvc_main_id"
                        + "   AND mbr.msisdn=" + msisdn
                        + "   AND ("
                        + "         mbr.state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "         OR (mbr.state=1 AND CURDATE() <= DATE_ADD(expired_date, INTERVAL ss.rchg_ctr DAY))"
                        + "       )"
                        : "")
                        + " WHERE 1"
                        + whereoper
                        + wheresrvcid
                        + wheretype
                        + wherestatus;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementListByServiceIdMt(String srvc_id_mt, String msisdn, CARRIER oper, int type, int status) {
        List srvcList = new ArrayList();
        try {
            String oper_name = oper.toString().toLowerCase();
            DBPoolManager cp = new DBPoolManager();

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub ss"
                        + "  LEFT JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = ss.srvc_main_id"
                        + ((msisdn != null) && (!msisdn.isEmpty())
                        ? " INNER JOIN mmbr_" + oper_name + " AS mbr"
                        + "    ON ss.srvc_main_id = mbr.srvc_main_id"
                        + "   AND mbr.msisdn=" + msisdn
                        + "   AND ("
                        + "         mbr.state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "         OR (mbr.state=1 AND CURDATE() <= DATE_ADD(expired_date, INTERVAL ss.rchg_ctr DAY))"
                        + "       )"
                        : "")
                        + " WHERE ss.srvc_id_mt=?"
                        + "   AND ss.oper_id=?"
                        + wheretype
                        + wherestatus;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, srvc_id_mt);
                cp.getPreparedStatement().setInt(2, oper.getId());

                ResultSet rs = cp.execQueryPrepareStatement();
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public static ServiceElement[] getServiceElementList(String srvc_id, CARRIER oper, String msisdn, int type, int status) {
        List srvcList = new ArrayList();
        try {
            String oper_name = oper.toString().toLowerCase();
            DBPoolManager cp = new DBPoolManager();

            String wheresrvcid = "";
            if (srvc_id != null) {
                wheresrvcid = "   AND ( (ss.srvc_id='" + srvc_id + "'"
                        + " AND ss.status=" + SERVICE_STATUS.ON.getDbId() + ")"
                        + "          OR (ss.srvc_id_mo_test='" + srvc_id + "'"
                        + " AND ss.status=" + SERVICE_STATUS.TEST.getDbId() + ") )";
            }

            String whereoper = "";
            if ((oper != null) && (oper != CARRIER.ALL)) {
                whereoper = " AND ss.oper_id=" + oper.getId();
            }

            String wheretype = SERVICE_TYPE.where(type, "ss.srvc_type");
            String wherestatus = SERVICE_STATUS.where(status, "ss.status");
            try {
                String sql = "SELECT *"
                        + "  FROM srvc_sub ss"
                        + "  LEFT JOIN srvc_main sm"
                        + "    ON sm.srvc_main_id = ss.srvc_main_id"
                        + ((msisdn != null) && (!msisdn.isEmpty())
                        ? " INNER JOIN mmbr_" + oper_name + " AS mbr"
                        + "    ON ss.srvc_main_id = mbr.srvc_main_id"
                        + "   AND mbr.msisdn=" + msisdn
                        + "   AND ("
                        + "         mbr.state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "         OR (mbr.state=1 AND CURDATE() <= DATE_ADD(expired_date, INTERVAL ss.rchg_ctr DAY))"
                        + "       )"
                        : "")
                        + " WHERE 1"
                        + whereoper
                        + wheresrvcid
                        + wheretype
                        + wherestatus;

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceElement se = new ServiceElement();
                    se.srvc_main_id = rs.getInt("srvc_main_id");
                    se.oper_id = rs.getInt("oper_id");
                    se.srvc_id = rs.getString("srvc_id");
                    se.srvc_type = rs.getInt("srvc_type");
                    se.ivr_register = rs.getString("ivr_register");
                    se.ivr_unregister = rs.getString("ivr_unregister");
                    se.sms_register = rs.getString("sms_register");
                    se.sms_unregister = rs.getString("sms_unregister");
                    se.thrd_prty_register = rs.getString("thrd_prty_register");
                    se.thrd_prty_unregister = rs.getString("thrd_prty_unregister");
                    se.sender = rs.getString("sender");
                    se.oper_config = new OperConfig(rs.getInt("conf_id"));
                    se.oper_config_non_chrg = new OperConfig(rs.getInt("conf_id_non_chrg"));
                    se.oper_config_test = new OperConfig(rs.getInt("conf_id_test"));
                    se.free_trial = rs.getShort("free_trial");
                    se.ctnt_ctr = rs.getShort("ctnt_ctr");
                    se.rmdr_ctr = rs.getShort("rmdr_ctr");
                    se.rchg_ctr = rs.getShort("rchg_ctr");
                    se.msg_sub_ft = rs.getString("msg_sub_ft");
                    se.msg_usub_ft = rs.getString("msg_usub_ft");
                    se.msg_warn_ft = rs.getString("msg_warn_ft");
                    se.msg_sub_nm = rs.getString("msg_sub_nm");
                    se.msg_usub_nm = rs.getString("msg_usub_nm");
                    se.msg_warn_nm = rs.getString("msg_warn_nm");
                    se.msg_err_no_srvc = rs.getString("msg_err_no_srvc");
                    se.msg_err_dup = rs.getString("msg_err_dup");
                    se.status = rs.getInt("status");
                    se.srvc_chrg_type_id = rs.getInt("srvc_chrg_type_id");
                    se.srvc_chrg_amnt = rs.getInt("srvc_chrg_amnt");
                    se.srvc_id_non_chrg = rs.getString("srvc_id_non_chrg");
                    se.srvc_id_mo = rs.getString("srvc_id_mo");
                    se.srvc_id_mo_test = rs.getString("srvc_id_mo_test");
                    se.srvc_id_mt = rs.getString("srvc_id_mt");
                    se.srvc_id_mt_chrg = rs.getString("srvc_id_mt_chrg");
                    se.bcast_srvc_id = rs.getString("bcast_srvc_id");
                    se.chrg_flg = rs.getString("chrg_flg");
                    se.srvc_name = rs.getString("name");
                    se.priority = rs.getInt("priority");

                    se.owner = UserFactory.getUser(rs.getInt("uid"));
                    try {
                        if (se.oper_id == CARRIER.AIS_LEGACY.getId()) {
                            se.aisLegacyCommand = new AisLegacyCommand(se.srvc_main_id);
                        }
                    } catch (Exception e) {
                    }
                    se.fillMessageVariables();

                    srvcList.add(se);
                }

                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
            return null;
        }

        return (ServiceElement[]) srvcList.toArray(new ServiceElement[0]);
    }

    public int remove() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM srvc_sub"
                        + " WHERE srvc_main_id=" + this.srvc_main_id
                        + "   AND oper_id=" + this.oper_id;

                rows = cp.execUpdate(sql);
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public Hashtable getRawData() throws Exception {
        Hashtable raw = new Hashtable();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM srvc_sub"
                        + " WHERE srvc_main_id=" + this.srvc_main_id
                        + "   AND oper_id=" + this.oper_id;

                ResultSet rs = cp.execQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
                if (rs.next()) {
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        raw.put(rsmd.getColumnLabel(i), rs.getString(i) == null ? "" : rs.getString(i));
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return raw;
    }

    public static int add(Hashtable<String, String> items) throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String[] int_field = {"srvc_main_id", "oper_id", "srvc_type", "free_trial",
                "ctnt_ctr", "rmdr_ctr", "rchg_ctr", "status",
                "conf_id", "conf_id_test", "conf_id_non_chrg", "uid", "priority"};

            List int_field_list = Arrays.asList(int_field);

            if (items.isEmpty()) {
                return 0;
            }

            try {
                String FIELDS = "";
                String VALUES = "";

                Iterator iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();

                    FIELDS = FIELDS + key + (iter.hasNext() ? "," : "");
                    VALUES = VALUES + "?" + (iter.hasNext() ? "," : "");
                }

                String sql = "INSERT INTO srvc_sub  ( " + FIELDS + " )" + " VALUES " + " ( " + VALUES + " )";

                cp.prepareStatement(sql, 1);

                int n = 0;
                iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    n++;
                    String key = (String) iter.next();
                    String value = (String) items.get(key);

                    if (int_field_list.contains(key)) {
                        cp.getPreparedStatement().setInt(n, Integer.parseInt(value.isEmpty() ? "0" : value));
                    } else {
                        cp.getPreparedStatement().setString(n, value);
                    }

                }

                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public static int sync(Hashtable<String, String> items)
            throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String[] int_field = {"srvc_main_id", "oper_id", "srvc_type", "free_trial",
                "ctnt_ctr", "rmdr_ctr", "rchg_ctr", "status",
                "conf_id", "conf_id_test", "conf_id_non_chrg", "uid", "priority"};

            List int_field_list = Arrays.asList(int_field);

            if (items.isEmpty()) {
                return 0;
            }

            try {
                String VALUES = "";

                Iterator iter = items.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();

                    VALUES = VALUES + key + "= ?" + (iter.hasNext() ? "," : "");
                }

                String sql = "UPDATE srvc_sub"
                        + "  SET " + VALUES
                        + " WHERE srvc_main_id=" + (String) items.get("srvc_main_id")
                        + "   AND oper_id=" + (String) items.get("oper_id");

                cp.prepareStatement(sql);

                iter = items.keySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = (String) items.get(key);
                    if (int_field_list.contains(key)) {
                        cp.getPreparedStatement().setInt(++i, Integer.parseInt(value.isEmpty() ? "0" : value));
                    } else {
                        cp.getPreparedStatement().setString(++i, value);
                    }
                }

                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public boolean isAble2ManageSub() {
        boolean allow = false;

        if (((this.oper_id == CARRIER.TRUE.getId())
                || (this.oper_id == CARRIER.TRUEH.getId())
                || (this.oper_id == CARRIER.AIS_LEGACY.getId())
                || ((this.oper_id == CARRIER.AIS.getId()) && (this.oper_config.hybrid.name != null))
                || ((this.oper_id == CARRIER.AIS.getId()) && (SSS_TYPE.fromId(this.srvc_type & SSS_TYPE.ALL.getId()) == SSS_TYPE.TYPE_L))
                || ((this.oper_id == CARRIER.AIS.getId()) && (SSS_TYPE.fromId(this.srvc_type & SSS_TYPE.ALL.getId()) == SSS_TYPE.TYPE_L_PLUS))
                || ((this.oper_id == CARRIER.DTAC.getId()) && ((this.srvc_type & SERVICE_TYPE.CONTROLCMD.getId()) > 0))
                || ((this.oper_id == CARRIER.DTAC_SDP.getId()) && ((this.srvc_type & SERVICE_TYPE.CONTROLCMD.getId()) > 0)))
                && ((this.srvc_type & SERVICE_TYPE.SUBSCRIPTION.getId()) > 0)) {
            allow = true;
        }
        return allow;
    }

    public boolean isNormalSubscription() {
        return ((this.srvc_type & SERVICE_TYPE.SUBSCRIPTION.getId()) > 0) && (this.srvc_chrg_type_id != ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId());
    }

    public boolean isSubscriptionMTCharge() {
        return ((this.srvc_type & SERVICE_TYPE.SUBSCRIPTION.getId()) > 0) && (this.srvc_chrg_type_id == ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId());
    }

    public int getFirstExpireDate() {
        int day = 0;

        switch (SRVC_CHRG.fromId(this.srvc_chrg_type_id)) {
            case PER_DAY:
                day = 1;
                break;
            case PER_WEEK:
                day = 7;
                break;
            case PER_MONTH:
                day = 30;
        }

        return day * this.srvc_chrg_amnt;
    }

    public static int convertAliasId(String alias) throws Exception {
        return convertAliasId(alias, CARRIER.ALL);
    }

    public static int convertAliasId(String alias, CARRIER oper) throws Exception {
        int id = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "  SELECT srvc_main_id"
                        + "  FROM srvc_alias_map"
                        + " WHERE 1"
                        + "   AND ( (srvc_number='" + alias + "') OR ('" + alias + "' REGEXP srvc_number) )"
                        + "   AND (oper_id =" + CARRIER.ALL.getId()
                        + (oper != CARRIER.ALL ? " OR oper_id=" + oper.getId() : "")
                        + "       )"
                        + " ORDER BY "
                        + "       (srvc_number='" + alias + "') DESC" // string equal is highest priority
                        + "     , oper_id DESC";

                ResultSet rs = cp.execQuery(sql);
                if (rs.next()) { // get the first one
                    id = rs.getInt(1);
                } else {
                    throw new Exception("Service main ID not found(alias id: " + alias + ")");
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return id;
    }

    public Map<String, String> getMessageKeys() {
        Map map = new HashMap();
        try {
            map.put("CallCenter", this.owner.getTel());
            map.put("ServiceName", this.srvc_name);
            map.put("Price", Integer.toString(this.price));
            map.put("ChargePeriod", ServiceCharge.SRVC_CHRG.fromId(this.srvc_chrg_type_id).toString());
            map.put("ChargeIntervalDay", Integer.toString(ServiceCharge.getChargeInterval(this.srvc_main_id)));
            map.put("FreeTrial", Integer.toString(this.free_trial));
            map.put("IvrUnsub", this.ivr_unregister);
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }

        return map;
    }

    public final void fillMessageVariables() {
        try {
            Map map = getMessageKeys();

            this.msg_sub_ft = StringUtil.fillMessageVariables(map, this.msg_sub_ft);
            this.msg_usub_ft = StringUtil.fillMessageVariables(map, this.msg_usub_ft);
            this.msg_warn_ft = StringUtil.fillMessageVariables(map, this.msg_warn_ft);
            this.msg_sub_nm = StringUtil.fillMessageVariables(map, this.msg_sub_nm);
            this.msg_usub_nm = StringUtil.fillMessageVariables(map, this.msg_usub_nm);
            this.msg_warn_nm = StringUtil.fillMessageVariables(map, this.msg_warn_nm);
            this.msg_err_no_srvc = StringUtil.fillMessageVariables(map, this.msg_err_no_srvc);
            this.msg_err_dup = StringUtil.fillMessageVariables(map, this.msg_err_dup);
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
    }

    public static boolean isOwner(int srvc_main_id, CARRIER oper, User user) {
        boolean access = false;

        if (user != null) {
            String[] children = user.getChildUid();
            for (String child : children) {
                if ((child != null) && (!child.isEmpty())) {
                    try {
                        User uchild = UserFactory.getUser(Integer.parseInt(child));

                        ServiceElement[] seList = getServiceElementList(null, oper, SERVICE_TYPE.SUBSCRIPTION.getId(), SERVICE_STATUS.ON.getId(), uchild);

                        for (ServiceElement se : seList) {
                            if (se.srvc_id != null) {
                                if (se.srvc_main_id == srvc_main_id) {
                                    access = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "exception caught!!", e);
                    }
                }
                if (access) {
                    break;
                }
            }
        }
        return access;
    }

    public SSS_TYPE getSSSType() {
        int sss_type = SSS_TYPE.UNKNOWN.getId();
        if (CARRIER.fromId(this.oper_id) == CARRIER.AIS) {
            sss_type = this.srvc_type & SSS_TYPE.ALL.getId();
            if (sss_type == 0) {
                if ((this.srvc_type & SERVICE_TYPE.SMSDOWNLOAD.getId()) > 0) {
                    sss_type = SSS_TYPE.TYPE_SMS_DOWNLOAD.getId();
                } else if (((this.srvc_type & SERVICE_TYPE.SUBSCRIPTION.getId()) > 0) && (this.srvc_chrg_type_id != ServiceCharge.SRVC_CHRG.PER_MESSAGE.getId())) {
                    sss_type = SSS_TYPE.TYPE_B.getId();
                } else {
                    sss_type = SSS_TYPE.TYPE_A.getId();
                }
            }
        }

        return SSS_TYPE.fromId(sss_type);
    }

    public static enum SERVICE_STATUS {

        ALL(0),
        OFF(1),
        ON(2),
        TEST(4);
        private final int id;

        private SERVICE_STATUS(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public int getDbId() {
            return (int) (Math.log(this.id) / Math.log(2.0D));
        }

        public static SERVICE_STATUS fromId(int id) {
            for (SERVICE_STATUS e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        public static String where(int id, String field) {
            String wherestatus = "";
            if (id > ALL.getId()) {
                wherestatus = " AND ( 0";
                if (id >= TEST.getId()) {
                    wherestatus = wherestatus + " OR " + field + "=" + TEST.getDbId();
                    id -= TEST.getId();
                }
                if (id >= ON.getId()) {
                    wherestatus = wherestatus + " OR " + field + "=" + ON.getDbId();
                    id -= ON.getId();
                }
                if (id >= OFF.getId()) {
                    wherestatus = wherestatus + " OR " + field + "=" + OFF.getDbId();
                    id -= OFF.getId();
                }
                wherestatus = wherestatus + ")";
            }
            return wherestatus;
        }
    }

    public static enum SERVICE_TYPE {

        ALL(0),
        SUBSCRIPTION(1),
        SMSDOWNLOAD(2),
        RESERVED(4),
        SMS(8),
        WAP(16),
        MMS(32),
        DDS(64),
        CONTROLCMD(128),
        NOCSS(256),
        SFTP(512),
        BULK(1024),
        BCSERVICEID(2048),
        MTCHARGE(4096),
        CPVALIDATE(8192),
        CHAINQUIZ(16384),
        LIVEBC(32768),
        USSDDIRECTREPLY(65536);
        private final int id;

        private SERVICE_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        /**
         * @deprecated
         */
        public int getDbId() {
            return this.id;
        }

        public static SERVICE_TYPE fromId(int id) {
            for (SERVICE_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        public static String where(int type, String field) {
            String wheretype = "";
            if (type > ALL.getId()) {
                SERVICE_TYPE[] ACTION = {RESERVED, SMSDOWNLOAD, SUBSCRIPTION};
                wheretype = " AND ( 1";
                for (SERVICE_TYPE a : ACTION) {
                    if ((a.getId() & type) > 0) {
                        wheretype = wheretype + " AND (" + field + "&" + a.getId() + ")";
                    }
                }
                wheretype = wheretype + ")";

                SERVICE_TYPE[] CONTENT = {SMS, WAP, MMS};
                if ((type & 0x38) != 0) {
                    wheretype = wheretype + " AND ( 0";
                    for (SERVICE_TYPE c : CONTENT) {
                        if ((c.getId() & type) > 0) {
                            wheretype = wheretype + " OR (" + field + "&" + c.getId() + ")";
                        }
                    }
                    wheretype = wheretype + ")";
                }
            }
            return wheretype;
        }
    }

    public static enum SSS_TYPE {

        UNKNOWN(0),
        TYPE_A(64),
        TYPE_B(128),
        TYPE_L(256),
        TYPE_SMS_DOWNLOAD(512),
        TYPE_L_PLUS(1024),
        ALL(TYPE_A.getId() | TYPE_B.getId() | TYPE_L.getId() | TYPE_SMS_DOWNLOAD.getId() | TYPE_L_PLUS.getId());
        private final int id;

        private SSS_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static SSS_TYPE fromId(int id) {
            for (SSS_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}