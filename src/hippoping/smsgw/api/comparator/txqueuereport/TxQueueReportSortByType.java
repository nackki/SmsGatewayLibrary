/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.txqueuereport;

import java.util.Comparator;
import hippoping.smsgw.api.db.report.TxQueueReport;

/**
 *
 * @author nack
 */
public class TxQueueReportSortByType implements Comparator<TxQueueReport> {
    public int compare(TxQueueReport s1, TxQueueReport s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getTxQueue().content_type.compareTo(s2.getTxQueue().content_type);
    }
}
