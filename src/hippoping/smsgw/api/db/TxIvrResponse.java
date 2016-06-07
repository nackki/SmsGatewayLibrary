package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class TxIvrResponse {

    protected int ivr_resp_id;
    protected String srvc_id;
    protected String txid;
    protected String msisdn;
    protected String msg;
    protected String msg_type;
    protected String msg_encd;
    protected String from;
    protected String to;
    protected String scts;
    protected String rcpt_dt;
    protected int req_code;
    protected String req_desc;
    protected String req_dt;

    public TxIvrResponse() {
    }

    public TxIvrResponse(int ivr_resp_id)
            throws Exception {
        this.ivr_resp_id = -1;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "SELECT ivr_resp_id     , srvc_id     , txid     , msisdn     , msg     , msg_type     , msg_encd     , `from`     , `to`     , scts     , DATE_FORMAT(rcpt_dt, '%Y%m%dT%h:%m:%s+07:00') AS rcpt_dt     , req_code     , req_desc     , DATE_FORMAT(req_dt, '%Y%m%dT%h:%m:%s+07:00') AS req_dt  FROM trns_ivr_resp  WHERE ivr_resp_id=?;";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, ivr_resp_id);

            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "inquiry ivr_resp[" + ivr_resp_id + "] ok");
                this.ivr_resp_id = rs.getInt("ivr_resp_id");
                this.srvc_id = rs.getString("srvc_id");
                this.txid = rs.getString("txid");
                this.msisdn = rs.getString("msisdn");
                this.msg = rs.getString("msg");
                this.msg_type = rs.getString("msg_type");
                this.msg_encd = rs.getString("msg_encd");
                this.from = rs.getString("from");
                this.to = rs.getString("to");
                this.scts = rs.getString("scts");
                this.rcpt_dt = rs.getString("rcpt_dt");
                this.req_code = rs.getInt("req_code");
                this.req_desc = rs.getString("req_desc");
                this.req_dt = rs.getString("req_dt");
            } else {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "ivr_resp[" + ivr_resp_id + "] not found!!");
            }

            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public TxIvrResponse(String srvc_id, String txid, String msisdn, String msg, String msg_type, String msg_encd, String from, String to, String scts)
            throws Exception {
        this.ivr_resp_id = -1;
        this.srvc_id = srvc_id;
        this.txid = txid;
        this.msisdn = msisdn;
        this.msg = msg;
        this.msg_type = msg_type;
        this.msg_encd = msg_encd;
        this.from = from;
        this.to = to;
        this.scts = scts;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "INSERT INTO trns_ivr_resp (srvc_id, txid, msisdn, msg, msg_type, msg_encd, `from`, `to`, scts) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            cp.prepareStatement(sql, 1);
            cp.getPreparedStatement().setString(1, srvc_id);
            cp.getPreparedStatement().setString(2, txid);
            cp.getPreparedStatement().setString(3, msisdn);
            cp.getPreparedStatement().setString(4, msg);
            cp.getPreparedStatement().setString(5, msg_type);
            cp.getPreparedStatement().setString(6, msg_encd);
            cp.getPreparedStatement().setString(7, from);
            cp.getPreparedStatement().setString(8, to);
            cp.getPreparedStatement().setString(9, scts);

            int row = cp.execUpdatePrepareStatement();
            if (row == 1) {
                ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                if (rs.next()) {
                    this.ivr_resp_id = rs.getInt(1);
                }

                rs.close();
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
    }

    public int updateStatus(int code, String desc) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "UPDATE trns_ivr_resp   SET req_code=?     , req_desc=?     , req_dt=NOW() WHERE ivr_resp_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, code);
                cp.getPreparedStatement().setString(2, desc);
                cp.getPreparedStatement().setInt(3, this.ivr_resp_id);

                row = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return row;
    }

    public static TxIvrResponse getInstance(String srvc_id, String txid, String msisdn, String msg, String msg_type, String msg_encd, String from, String to, String scts) {
        TxIvrResponse ivr = null;
        try {
            ivr = new TxIvrResponse(srvc_id, txid, msisdn, msg, msg_type, msg_encd, from, to, scts);
        } catch (Exception e) {
            ivr = null;
        }

        if (ivr.getIvr_resp_id() == -1) {
            ivr = null;
        }

        return ivr;
    }

    public String getReq_dt() {
        return this.req_dt;
    }

    public void setReq_dt(String send_dt) {
        this.req_dt = send_dt;
    }

    public String getReq_desc() {
        return this.req_desc;
    }

    public void setReq_desc(String status_desc) {
        this.req_desc = (this.req_desc.toLowerCase().equals("success") ? this.req_desc : "Fail");
    }

    public int getReq_code() {
        return this.req_code;
    }

    public void setReq_code(int status_code) {
        this.req_code = (this.req_code == 0 ? this.req_code : 1);
    }

    public String getRcpt_dt() {
        return this.rcpt_dt;
    }

    public void setRcpt_dt(String rcpt_dt) {
        this.rcpt_dt = rcpt_dt;
    }

    public String getScts() {
        return this.scts;
    }

    public void setScts(String scts) {
        this.scts = scts;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg_encd() {
        return this.msg_encd;
    }

    public void setMsg_encd(String msg_encd) {
        this.msg_encd = msg_encd;
    }

    public String getMsg_type() {
        return this.msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getTxid() {
        return this.txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getSrvc_id() {
        return this.srvc_id;
    }

    public void setSrvc_id(String srvc_id) {
        this.srvc_id = srvc_id;
    }

    public int getIvr_resp_id() {
        return this.ivr_resp_id;
    }

    public void setIvr_resp_id(int ivr_resp_id) {
        this.ivr_resp_id = ivr_resp_id;
    }
}