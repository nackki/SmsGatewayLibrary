package hippoping.smsgw.api.operator.ais_legacy;

import javax.ejb.Remote;

@Remote
public abstract interface DlvrRepRequestEventRemote {

    public abstract DlvrMsgReplyFactory handle(String paramString, int paramInt);
}