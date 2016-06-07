package hippoping.smsgw.api.operator.truemove;

import hippoping.smsgw.api.operator.true_css.RsrReplyFactory;
import javax.ejb.Remote;

@Remote
public abstract interface MoEventRemote {

    public abstract RsrReplyFactory processKeyword(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt);

    public abstract String Hello(String paramString);
}