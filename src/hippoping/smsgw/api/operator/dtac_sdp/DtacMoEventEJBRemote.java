/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.operator.dtac_sdp;

import hippoping.smsgw.api.db.RxQueue.RX_TYPE;
import hippoping.smsgw.api.db.ServiceElement;
import javax.ejb.Remote;

/**
 *
 * @author nack
 */
@Remote
public interface DtacMoEventEJBRemote {

    CpaResponseFactory processKeyword(
            final String srvc_id, final String msisdn, final String ivr, final String keyword,
            final String ivr_resp_id, final String ussd, final long rx_id, final RX_TYPE type);
    
    // Support direct sub/unsub to specific service
    CpaResponseFactory processKeyword(
            final ServiceElement se, final String msisdn, final String ivr, final String keyword,
            final String ivr_resp_id, final String ussd, final long rx_id, final RX_TYPE type, String action);
}
