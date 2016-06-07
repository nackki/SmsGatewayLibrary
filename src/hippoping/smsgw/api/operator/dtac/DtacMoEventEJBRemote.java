/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.operator.dtac;

import javax.ejb.Remote;

/**
 *
 * @author nack
 */
@Remote
public interface DtacMoEventEJBRemote {

    CpaResponseFactory processKeyword(
            final String srvc_id, final String msisdn, final String ivr, final String keyword,
            final String ivr_resp_id, final String ussd, final int rx_id);
    
    String Hello(String name);
}
