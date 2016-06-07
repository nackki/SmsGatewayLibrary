package hippoping.smsgw.api.operator.true_css;

import com.truemove.css.thirdparty.response.Response;
import hippoping.smsgw.api.db.OperConfig;
import javax.ejb.Remote;

@Remote
public abstract interface CssDispatcherBeanRemote {

    public abstract Response sendPacket(int paramInt, String paramString1, String paramString2, OperConfig.CARRIER paramCARRIER);

}