package hippoping.smsgw.api.operator.ais_legacy;

import java.io.Serializable;
import java.util.Map;
import lib.common.UrlParse;

public class DlvrMsgRequest
        implements Serializable {

    protected String transid;
    protected String cmd;
    protected String fet;
    protected String ntype;
    protected String from;
    protected String to;
    protected String code;
    protected String ctype;
    protected String content;

    public DlvrMsgRequest(String transid, String cmd, String fet, String ntype, String from, String to, String code, String ctype, String content) {
        this.transid = transid;
        this.cmd = cmd;
        this.fet = fet;
        this.ntype = ntype;
        this.from = from;
        this.to = to;
        this.code = code;
        this.ctype = ctype;
        this.content = content;
    }

    public DlvrMsgRequest(String query) {
        Map params = UrlParse.getQueryMap(query);
        this.transid = ((String) params.get("TRANSID"));
        this.cmd = ((String) params.get("CMD"));
        this.fet = ((String) params.get("FET"));
        this.ntype = ((String) params.get("NTYPE"));
        this.from = ((String) params.get("FROM"));
        this.to = ((String) params.get("TO"));
        this.code = ((String) params.get("CODE"));
        this.ctype = ((String) params.get("CTYPE"));
        this.content = ((String) params.get("CONTENT"));
    }

    public String getCmd() {
        return this.cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCtype() {
        return this.ctype;
    }

    public void setCtype(String ctype) {
        this.ctype = ctype;
    }

    public String getFet() {
        return this.fet;
    }

    public void setFet(String fet) {
        this.fet = fet;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getNtype() {
        return this.ntype;
    }

    public void setNtype(String ntype) {
        this.ntype = ntype;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTransid() {
        return this.transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }
}