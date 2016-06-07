package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class DroConfigure {

    protected int dro_id;
    protected ServiceElement service;
    protected int sub_ft = 0;
    protected int sub_nm = 0;
    protected int sub_renew_ft = 0;
    protected int sub_renew_nm = 0;
    protected int sub_dup = 0;
    protected int unregister = 0;
    protected int warning = 0;
    protected int recurring = 0;
    protected int broadcast = 0;
    protected int pullsms = 0;
    protected int error = 0;
    protected int forward = 0;

    public DroConfigure() {
    }

    public DroConfigure(ServiceElement se)
            throws Exception {
        this.service = se;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM conf_dro  WHERE srvc_main_id=?   AND oper_id=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, se.srvc_main_id);
                cp.getPreparedStatement().setInt(2, se.oper_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.dro_id = rs.getInt("dro_id");
                    this.sub_ft = rs.getInt("sub_ft");
                    this.sub_nm = rs.getInt("sub_nm");
                    this.sub_renew_ft = rs.getInt("sub_renew_ft");
                    this.sub_renew_nm = rs.getInt("sub_renew_nm");
                    this.sub_dup = rs.getInt("sub_dup");
                    this.unregister = rs.getInt("unregister");
                    this.warning = rs.getInt("warning");
                    this.recurring = rs.getInt("recurring");
                    this.broadcast = rs.getInt("broadcast");
                    this.pullsms = rs.getInt("pullsms");
                    this.error = rs.getInt("error");
                    this.forward = rs.getInt("forward");
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public DroConfigure(int srvc_main_id, int oper_id) throws Exception {
        this.service = new ServiceElement(srvc_main_id, oper_id, 0, 0);
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM conf_dro  WHERE srvc_main_id=?   AND oper_id=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                cp.getPreparedStatement().setInt(2, oper_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.dro_id = rs.getInt("dro_id");
                    this.sub_ft = rs.getInt("sub_ft");
                    this.sub_nm = rs.getInt("sub_nm");
                    this.sub_renew_ft = rs.getInt("sub_renew_ft");
                    this.sub_renew_nm = rs.getInt("sub_renew_nm");
                    this.sub_dup = rs.getInt("sub_dup");
                    this.unregister = rs.getInt("unregister");
                    this.warning = rs.getInt("warning");
                    this.recurring = rs.getInt("recurring");
                    this.broadcast = rs.getInt("broadcast");
                    this.pullsms = rs.getInt("pullsms");
                    this.error = rs.getInt("error");
                    this.forward = rs.getInt("forward");
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public DroConfigure(int dro_id) throws Exception {
        this.dro_id = dro_id;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM conf_dro  WHERE  srvc_main_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, dro_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    ServiceElement se = null;
                    try {
                        se = new ServiceElement(rs.getInt("srvc_main_id"), rs.getInt("oper_id"), ServiceElement.SERVICE_TYPE.ALL.getId(), ServiceElement.SERVICE_STATUS.ALL.getId());
                        if (se.srvc_main_id <= 0) {
                            se = null;
                        }
                    } catch (Exception e) {
                    }
                    this.service = se;
                    this.sub_ft = rs.getInt("sub_ft");
                    this.sub_nm = rs.getInt("sub_nm");
                    this.sub_renew_ft = rs.getInt("sub_renew_ft");
                    this.sub_renew_nm = rs.getInt("sub_renew_nm");
                    this.sub_dup = rs.getInt("sub_dup");
                    this.unregister = rs.getInt("unregister");
                    this.warning = rs.getInt("warning");
                    this.recurring = rs.getInt("recurring");
                    this.broadcast = rs.getInt("broadcast");
                    this.pullsms = rs.getInt("pullsms");
                    this.error = rs.getInt("error");
                    this.forward = rs.getInt("forward");
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static int add(DroConfigure dc) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   INSERT INTO conf_dro (`srvc_main_id`, `oper_id`, `sub_ft`, `sub_nm`, `sub_renew_ft`, `sub_renew_nm`, `sub_dup`, `unregister`, `warning`, `recurring`, `broadcast`, `pullsms`, `error`, `forward`)  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                cp.prepareStatement(sql, 1);
                cp.getPreparedStatement().setInt(1, dc.service.srvc_main_id);
                cp.getPreparedStatement().setInt(2, dc.service.oper_id);
                cp.getPreparedStatement().setInt(3, dc.sub_ft);
                cp.getPreparedStatement().setInt(4, dc.sub_nm);
                cp.getPreparedStatement().setInt(5, dc.sub_renew_ft);
                cp.getPreparedStatement().setInt(6, dc.sub_renew_nm);
                cp.getPreparedStatement().setInt(7, dc.sub_dup);
                cp.getPreparedStatement().setInt(8, dc.unregister);
                cp.getPreparedStatement().setInt(9, dc.warning);
                cp.getPreparedStatement().setInt(10, dc.recurring);
                cp.getPreparedStatement().setInt(11, dc.broadcast);
                cp.getPreparedStatement().setInt(12, dc.pullsms);
                cp.getPreparedStatement().setInt(13, dc.error);
                cp.getPreparedStatement().setInt(14, dc.forward);
                row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        dc.dro_id = rs.getInt(1);
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

        return row;
    }

    public static List<DroConfigure> getAll() {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "  SELECT dro_id  FROM conf_dro";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    list.add(new DroConfigure(rs.getInt(1)));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            return null;
        }

        return list;
    }

    public int remove() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM conf_dro WHERE dro_id=" + this.dro_id;

                rows = cp.execUpdate(sql);

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "dro configure " + rows + " row(s) removed.");
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

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE conf_dro    SET srvc_main_id=?      , oper_id=?      , sub_ft=?      , sub_nm=?      , sub_renew_ft=?      , sub_renew_nm=?      , sub_dup=?      , unregister=?      , warning=?      , recurring=?      , broadcast=?      , pullsms=?      , error=?      , forward=?  WHERE dro_id=" + this.dro_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, this.service.srvc_main_id);
                cp.getPreparedStatement().setInt(2, this.service.oper_id);
                cp.getPreparedStatement().setInt(3, this.sub_ft);
                cp.getPreparedStatement().setInt(4, this.sub_nm);
                cp.getPreparedStatement().setInt(5, this.sub_renew_ft);
                cp.getPreparedStatement().setInt(6, this.sub_renew_nm);
                cp.getPreparedStatement().setInt(7, this.sub_dup);
                cp.getPreparedStatement().setInt(8, this.unregister);
                cp.getPreparedStatement().setInt(9, this.warning);
                cp.getPreparedStatement().setInt(10, this.recurring);
                cp.getPreparedStatement().setInt(11, this.broadcast);
                cp.getPreparedStatement().setInt(12, this.pullsms);
                cp.getPreparedStatement().setInt(13, this.error);
                cp.getPreparedStatement().setInt(14, this.forward);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public void setDroFlag(int dro_flag) {
        this.sub_ft = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.SUB_FT.getId())) >>> DRO_EVENT_TYPE.SUB_FT.getId());
        this.sub_nm = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.SUB_NM.getId())) >>> DRO_EVENT_TYPE.SUB_NM.getId());
        this.sub_renew_ft = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.SUB_RENEW_FT.getId())) >>> DRO_EVENT_TYPE.SUB_RENEW_FT.getId());
        this.sub_renew_nm = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.SUB_RENEW_NM.getId())) >>> DRO_EVENT_TYPE.SUB_RENEW_NM.getId());
        this.sub_dup = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.SUB_DUP.getId())) >>> DRO_EVENT_TYPE.SUB_DUP.getId());
        this.unregister = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.UNREGISTER.getId())) >>> DRO_EVENT_TYPE.UNREGISTER.getId());
        this.warning = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.WARNING.getId())) >>> DRO_EVENT_TYPE.WARNING.getId());
        this.recurring = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.RECURRING.getId())) >>> DRO_EVENT_TYPE.RECURRING.getId());
        this.broadcast = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.BROADCAST.getId())) >>> DRO_EVENT_TYPE.BROADCAST.getId());
        this.pullsms = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.SMSDOWNLOAD.getId())) >>> DRO_EVENT_TYPE.SMSDOWNLOAD.getId());
        this.error = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.ERROR.getId())) >>> DRO_EVENT_TYPE.ERROR.getId());
        this.forward = ((dro_flag & (int) Math.pow(2.0D, DRO_EVENT_TYPE.FORWARD.getId())) >>> DRO_EVENT_TYPE.FORWARD.getId());
    }

    public int getDroFlag() {
        int flag = 0;

        flag |= this.sub_ft << DRO_EVENT_TYPE.SUB_FT.getId();
        flag |= this.sub_nm << DRO_EVENT_TYPE.SUB_NM.getId();
        flag |= this.sub_renew_ft << DRO_EVENT_TYPE.SUB_RENEW_FT.getId();
        flag |= this.sub_renew_nm << DRO_EVENT_TYPE.SUB_RENEW_NM.getId();
        flag |= this.sub_dup << DRO_EVENT_TYPE.SUB_DUP.getId();
        flag |= this.unregister << DRO_EVENT_TYPE.UNREGISTER.getId();
        flag |= this.warning << DRO_EVENT_TYPE.WARNING.getId();
        flag |= this.recurring << DRO_EVENT_TYPE.RECURRING.getId();
        flag |= this.broadcast << DRO_EVENT_TYPE.BROADCAST.getId();
        flag |= this.pullsms << DRO_EVENT_TYPE.SMSDOWNLOAD.getId();
        flag |= this.error << DRO_EVENT_TYPE.ERROR.getId();
        flag |= this.forward << DRO_EVENT_TYPE.FORWARD.getId();

        return flag;
    }

    public void setBroadcast(int broadcast) {
        this.broadcast = broadcast;
    }

    public void setError(int error) {
        this.error = error;
    }

    public void setForward(int forward) {
        this.forward = forward;
    }

    public void setPullsms(int pullsms) {
        this.pullsms = pullsms;
    }

    public void setRecurring(int recurring) {
        this.recurring = recurring;
    }

    public void setSub_dup(int sub_dup) {
        this.sub_dup = sub_dup;
    }

    public void setSub_ft(int sub_ft) {
        this.sub_ft = sub_ft;
    }

    public void setSub_nm(int sub_nm) {
        this.sub_nm = sub_nm;
    }

    public void setSub_renew_ft(int sub_renew_ft) {
        this.sub_renew_ft = sub_renew_ft;
    }

    public void setSub_renew_nm(int sub_renew_nm) {
        this.sub_renew_nm = sub_renew_nm;
    }

    public void setUnregister(int unregister) {
        this.unregister = unregister;
    }

    public void setWarning(int warning) {
        this.warning = warning;
    }

    public int getForward() {
        return this.forward;
    }

    public int getSub_ft() {
        return this.sub_ft;
    }

    public int getError() {
        return this.error;
    }

    public int getUnregister() {
        return this.unregister;
    }

    public int getSub_dup() {
        return this.sub_dup;
    }

    public int getSub_renew_nm() {
        return this.sub_renew_nm;
    }

    public int getSub_renew_ft() {
        return this.sub_renew_ft;
    }

    public int getSub_nm() {
        return this.sub_nm;
    }

    public int getPullsms() {
        return this.pullsms;
    }

    public int getBroadcast() {
        return this.broadcast;
    }

    public int getRecurring() {
        return this.recurring;
    }

    public int getWarning() {
        return this.warning;
    }

    public ServiceElement getService() {
        return this.service;
    }

    public void setService(ServiceElement se) {
        this.service = se;
    }

    public static enum DRO_EVENT_TYPE {

        SUB_FT(0),
        SUB_NM(1),
        SUB_RENEW_FT(2),
        SUB_RENEW_NM(3),
        SUB_DUP(4),
        UNREGISTER(5),
        WARNING(6),
        RECURRING(7),
        BROADCAST(8),
        SMSDOWNLOAD(9),
        ERROR(10),
        FORWARD(11);
        private final int id;

        private DRO_EVENT_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public int getDbId() {
            return (int) Math.pow(2.0D, this.id);
        }

        public static DRO_EVENT_TYPE fromId(int id) {
            for (DRO_EVENT_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}