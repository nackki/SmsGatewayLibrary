package hippoping.smsgw.api.operator.ais_legacy;

import javax.ejb.Remote;

@Remote
public abstract interface DlvrMsgRequestEventRemote {

    public abstract DlvrMsgReplyFactory processKeyword(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt);

    public abstract DlvrMsgReplyFactory handle(String paramString, int paramInt);
}