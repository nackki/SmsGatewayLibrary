/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package subs.gateway.dtac.xmlevent.async;

import com.dtac.sdp.unregister.CpaUnregister;
import hippoping.smsgw.api.db.RxQueue.RX_TYPE;
import java.util.concurrent.Future;
import javax.ejb.Remote;

/**
 *
 * @author nacks_mcair
 */
@Remote
public interface CpaUnregisterAsyncAdapterBeanRemote {
    public Future<String> handle(final CpaUnregister cpaUnregister, final RX_TYPE type, final String content);
}
