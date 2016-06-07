package hippoping.smsgw.api.operator.trueh_css;

import hippoping.smsgw.api.db.RxQueue.RX_TYPE;
import javax.ejb.Remote;

@Remote
public abstract interface MoEventBeanRemote {

    public abstract void processKeyword(String paramString1, String paramString2, String paramString3, RX_TYPE paramString4, String paramString5, long paramInt);
}