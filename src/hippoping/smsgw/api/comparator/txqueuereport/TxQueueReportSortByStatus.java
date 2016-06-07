/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.txqueuereport;

import hippoping.smsgw.api.db.TxQueue.TX_STATUS;
import java.util.Comparator;
import hippoping.smsgw.api.db.report.TxQueueReport;

/**
 *
 * @author nack
 */
public class TxQueueReportSortByStatus implements Comparator<TxQueueReport> {
    public int compare(TxQueueReport s1, TxQueueReport s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getTxQueue().getStatus().compareTo(s2.getTxQueue().getStatus());
    }
}
