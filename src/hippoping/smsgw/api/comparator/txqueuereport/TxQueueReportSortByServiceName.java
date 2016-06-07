/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.txqueuereport;

import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_STATUS;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_TYPE;
import java.util.Comparator;
import hippoping.smsgw.api.db.report.TxQueueReport;

/**
 *
 * @author nack
 */
public class TxQueueReportSortByServiceName implements Comparator<TxQueueReport> {
    public int compare(TxQueueReport s1, TxQueueReport s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        ServiceElement se1 = null;
        ServiceElement se2 = null;
        try {
        se1 = new ServiceElement(s1.getTxQueue().srvc_main_id, s1.getTxQueue().oper_id,
                SERVICE_TYPE.ALL.getId(), SERVICE_STATUS.ALL.getId());
        se2 = new ServiceElement(s2.getTxQueue().srvc_main_id, s2.getTxQueue().oper_id,
                SERVICE_TYPE.ALL.getId(), SERVICE_STATUS.ALL.getId());
        } catch (Exception e) {}
        if (se1==null || se2==null) {
            return 0;
        }
        return se1.srvc_name.compareTo(se2.srvc_name);
    }
}
