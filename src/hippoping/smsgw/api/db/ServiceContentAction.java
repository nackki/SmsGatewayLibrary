package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.StringUtil;

public class ServiceContentAction {

    private static final Logger log = Logger.getLogger(ServiceContentAction.class.getName());
    protected int ctnt_map_id;
    protected int srvc_main_id;
    protected OperConfig.CARRIER oper;
    protected String ivr_content_id;
    protected String keyword;
    protected String ussd_content_id;
    public ACTION_TYPE action_type;
    public int contentId;
    protected String chrg_flg;
    protected int piority;
    protected Message message;
    
    // add new property to support check fixed message
    private Boolean fixed_message = null;
    
    /**
     * Supported ServiceContentMap enhancement class.
     * @return NULL if it isn't SMS TEXT type, TRUE is yes otherwise return FALSE
     */
    public Boolean isFixedMessage() {
        return fixed_message;
    }

    public int getId() {
        return this.ctnt_map_id;
    }

    public Message getMessage() {
        return this.message;
    }

    public int getPiority() {
        return this.piority;
    }

    public void setPiority(int piority) {
        this.piority = piority;

        if (this.ctnt_map_id > 0) {
            try {
                DBPoolManager cp = new DBPoolManager();
                try {
                    String sql = "   UPDATE srvc_ctnt_map    SET piority=?  WHERE ctnt_map_id=?";

                    cp.prepareStatement(sql);
                    cp.getPreparedStatement().setInt(1, this.piority);
                    cp.getPreparedStatement().setInt(2, this.ctnt_map_id);
                    cp.execUpdatePrepareStatement();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQL error!!", e);
                } finally {
                    cp.release();
                }
            } catch (Exception e) {
                log.severe(e.getMessage());
            }
        }
    }

    public String getChrg_flg() {
        return this.chrg_flg;
    }

    public void setChrg_flg(String chrg_flg) {
        this.chrg_flg = chrg_flg;
    }

    public void setCtnt_map_id(int ctnt_map_id) {
        this.ctnt_map_id = ctnt_map_id;
    }

    public void setIvr_content_id(String ivr_content_id) {
        this.ivr_content_id = ivr_content_id;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setUssd_content_id(String ussd_content_id) {
        this.ussd_content_id = ussd_content_id;
    }

    public void setAction_type(ACTION_TYPE type) {
        this.action_type = type;
    }

    public void setContentId(int ctnt_id) {
        this.contentId = ctnt_id;
    }

    public void setSrvc_main_id(int id) {
        this.srvc_main_id = id;
    }

    public void setOper(OperConfig.CARRIER oper) {
        this.oper = oper;
    }

    public String getUssd_content_id() {
        return this.ussd_content_id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public String getIvr_content_id() {
        return this.ivr_content_id;
    }

    public OperConfig.CARRIER getOper() {
        return this.oper;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }

    public ServiceContentAction() {
    }

    public ServiceContentAction(int ctnt_map_id) throws Exception {
        this.ctnt_map_id = 0;

        String sql = "  SELECT *  FROM srvc_ctnt_map WHERE ctnt_map_id=?";

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, ctnt_map_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.ctnt_map_id = rs.getInt("ctnt_map_id");
                this.srvc_main_id = rs.getInt("srvc_main_id");
                this.oper = OperConfig.CARRIER.fromId(rs.getInt("oper_id"));
                this.ivr_content_id = rs.getString("ivr_ctnt_id");
                this.keyword = rs.getString("keyword");
                this.ussd_content_id = rs.getString("ussd_ctnt_id");
                this.action_type = ACTION_TYPE.fromId(rs.getInt("ctnt_type"));
                this.contentId = rs.getInt("ctnt_id");
                this.piority = rs.getInt("piority");
                this.chrg_flg = rs.getString("chrg_flg");

                switch (this.action_type) {
                    case SMS:
                        this.message = new MessageSms(this.contentId);
                        break;
                    case WAP:
                        this.message = new MessageWap(this.contentId);
                        break;
                    case MMS:
                        this.message = new MessageMms(this.contentId);
                        break;
                    default:
                        this.message = null;
                }
            }
            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public ServiceContentAction(int srvc_main_id, String ivr, String sms, String ussd, int oper_id) throws Exception {
        this.srvc_main_id = srvc_main_id;

        String whereivr = "";
        String wheresms = "";
        String whereussd = "";

        if ((ivr != null) && (!ivr.trim().isEmpty())) {
            whereivr = "   OR regex_replace('[\\\\^\\\\$\\\\)\\\\(\\\\|\\\\.\\\\*]','','" + ivr + "') REGEXP ivr_ctnt_id" + " OR '" + ivr + "' = ivr_ctnt_id";
        }

        if ((sms != null) && (!sms.trim().isEmpty())) {
            wheresms = "   OR regex_replace('[\\\\^\\\\$\\\\)\\\\(\\\\|\\\\.\\\\*]','','" + sms + "') REGEXP keyword" + " OR '" + sms + "' = keyword";
        }

        if ((ussd != null) && (!ussd.trim().isEmpty())) {
            whereussd = " OR '" + ussd + "' REGEXP ussd_ctnt_id";
        }

        String sql = 
                "  SELECT ctnt_map_id"
                + "     , IF(ctnt_id IS NULL, 0, ctnt_id) AS ctnt_id"
                + "     , ctnt_type"
                + "     , chrg_flg"
                + "  FROM srvc_ctnt_map"
                + " WHERE ( oper_id = ? OR oper_id = 0 )"
                + "   AND srvc_main_id=?"
                + "   AND ( 0" 
                + whereivr 
                + wheresms 
                + whereussd 
                + "     )" + " ORDER BY piority DESC";

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper_id);
            cp.getPreparedStatement().setInt(2, srvc_main_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                this.ctnt_map_id = rs.getInt("ctnt_map_id");
                this.action_type = ACTION_TYPE.fromId(rs.getInt("ctnt_type"));
                this.contentId = rs.getInt("ctnt_id");
                this.chrg_flg = rs.getString("chrg_flg");

                switch (this.action_type) {
                    case SMS:
                        this.message = new MessageSms(this.contentId);

                        switch (this.message.getSmsType()) {
                            case TEXT:
                                String tmp = this.message.getContents();
                                if (tmp.matches("(?i).*\\$SQL\\{.*")) {
                                    Map param = new HashMap();
                                    param.put("ivr", ivr == null ? "" : ivr);
                                    param.put("sms", sms == null ? "" : sms);
                                    param.put("ussd", ussd == null ? "" : ussd);
                                    tmp = StringUtil.fillMessageVariables(param, tmp);
                                    tmp = StringUtil.fillMessageSqlAnnotate(tmp);

                                    this.contentId = new MessageSms().add(tmp, 1);
                                    
                                    fixed_message = false;
                                } else {
                                    fixed_message = true;
                                }

                                break;
                        }

                        break;
                    case WAP:
                        this.message = new MessageWap(this.contentId);
                        break;
                    case MMS:
                        this.message = new MessageMms(this.contentId);
                        break;
                    default:
                        this.message = null;
                }
                log.log(Level.INFO, "reply content: {0}({1})", new Object[]{Integer.valueOf(this.contentId), this.action_type});
            } else {
                throw new Exception("content map not found!!");
            }
            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public static int getNextPriority(int srvc_main_id, OperConfig.CARRIER oper) throws Exception {
        int priority = 0;

        String sql = "  SELECT piority  FROM srvc_ctnt_map WHERE ( oper_id = ? )   AND srvc_main_id=? ORDER BY piority DESC LIMIT 1";

        DBPoolManager cp = new DBPoolManager();
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, oper.getId());
            cp.getPreparedStatement().setInt(2, srvc_main_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                priority = rs.getInt(1) + 1;
            }
            rs.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }

        return priority;
    }

    public static void reorderPriority(List<ServiceContentAction> scas, boolean ascending) {
        if (scas.isEmpty()) {
            return;
        }

        for (int i = 0; i < scas.size(); i++) {
            ServiceContentAction sca = (ServiceContentAction) scas.get(i);
            sca.setPiority(sca.getPiority() + 1000);
        }

        int i = 0;
        if (!ascending) {
            i = scas.size() - 1;
        }

        Iterator iter = scas.iterator();
        while (iter.hasNext()) {
            ServiceContentAction sca = (ServiceContentAction) iter.next();
            sca.setPiority(i);
            log.info("Set priority " + i + " to SCA[" + sca.getId() + "] '" + sca.getKeyword() + "'");
            if (ascending) {
                i++;
            } else {
                i--;
            }
        }
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE srvc_ctnt_map    SET srvc_main_id=?      , oper_id=?      , ivr_ctnt_id=?      , keyword=?      , ussd_ctnt_id=?      , ctnt_id=?      , ctnt_type=?      , piority=?      , chrg_flg=?  WHERE ctnt_map_id=" + this.ctnt_map_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.srvc_main_id);
                cp.getPreparedStatement().setInt(2, this.oper.getId());
                cp.getPreparedStatement().setString(3, this.ivr_content_id);
                cp.getPreparedStatement().setString(4, this.keyword);
                cp.getPreparedStatement().setString(5, this.ussd_content_id);
                cp.getPreparedStatement().setInt(6, this.contentId);
                cp.getPreparedStatement().setInt(7, this.action_type.getId());
                cp.getPreparedStatement().setInt(8, this.piority);
                cp.getPreparedStatement().setString(9, this.chrg_flg);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public static int add(ServiceContentAction sca) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "INSERT INTO srvc_ctnt_map      (srvc_main_id, oper_id, ivr_ctnt_id, keyword, ussd_ctnt_id, ctnt_id, ctnt_type, piority, chrg_flg) VALUES (?,?,?,?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, sca.srvc_main_id);
                cp.getPreparedStatement().setInt(2, sca.oper.getId());
                cp.getPreparedStatement().setString(3, sca.ivr_content_id);
                cp.getPreparedStatement().setString(4, sca.keyword);
                cp.getPreparedStatement().setString(5, sca.ussd_content_id);
                cp.getPreparedStatement().setInt(6, sca.contentId);
                cp.getPreparedStatement().setInt(7, sca.action_type.getId());
                cp.getPreparedStatement().setInt(8, sca.piority);
                cp.getPreparedStatement().setString(9, sca.chrg_flg);
                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        sca.ctnt_map_id = rs.getInt(1);
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

        return sca.ctnt_map_id;
    }

    public static Map<Integer, List<ServiceContentAction>> getAll() {
        Map list = new HashMap();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String wheretype = ServiceElement.SERVICE_TYPE.where(ServiceElement.SERVICE_TYPE.SMSDOWNLOAD.getId(), "s.srvc_type");
                String wherestatus = ServiceElement.SERVICE_STATUS.where(ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId(), "s.status");

                String sql = "  SELECT DISTINCT m.ctnt_map_id, m.srvc_main_id  FROM srvc_ctnt_map m  LEFT JOIN srvc_sub s    ON s.srvc_main_id = m.srvc_main_id" + wheretype + wherestatus + " ORDER BY m.srvc_main_id ASC, m.oper_id DESC, m.piority DESC";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    ServiceContentAction sca = null;
                    try {
                        sca = new ServiceContentAction(rs.getInt("ctnt_map_id"));
                    } catch (Exception e) {
                        log.log(Level.WARNING, "content not found, skip sca_id:{0}[{1}]", new Object[]{Integer.valueOf(rs.getInt("ctnt_map_id")), e.getMessage()});
                    }

                    List tmpList = null;

                    if ((tmpList = (List) list.get(Integer.valueOf(rs.getInt("srvc_main_id")))) == null) {
                        tmpList = new ArrayList();
                        tmpList.add(sca);
                        list.put(Integer.valueOf(rs.getInt("srvc_main_id")), tmpList);
                    } else {
                        ((List) list.get(Integer.valueOf(rs.getInt("srvc_main_id")))).add(sca);
                    }
                }

                rs.close();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception error!!", e);
            return null;
        }

        return list;
    }

    public int remove() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM srvc_ctnt_map WHERE ctnt_map_id=" + this.ctnt_map_id;

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

    public static enum ACTION_TYPE {

        SMS(0),
        WAP(1),
        MMS(2),
        FORWARD(3),
        IVR(4),
        USSD(5),
        UNDEFINED(6);
        private final int id;

        private ACTION_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static ACTION_TYPE fromId(int id) {
            for (ACTION_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}