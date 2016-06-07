/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.operator.dtac_sdp;

import com.dtac.sdp.subscription.status.reply.SubscriptionStatus;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.Subscriber;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author nack_ki
 */
@Remote
public interface XMLSupplierRemote {

    public List<SubscriptionStatus> sendRegister(final ServiceElement ns, final Subscriber[] msisdns, final String txid, final String org_type) throws Exception;
    public List<SubscriptionStatus> sendUnregister(final ServiceElement ns, final String[] msisdns, final String txid, final String org_type) throws Exception;

    public void syncNewSub(String date, int srvc_main_id) throws Exception;
    public void syncUnSub(String date, int srvc_main_id) throws Exception;
    public void syncExpired(String date, int srvc_main_id) throws Exception;
    public void syncStatus(int srvc_main_id) throws Exception;
    void syncMalformStateSub();

    public SubscriptionStatus querySubStatus(String msisdn, int srvc_main_id, String txid);
    
}
