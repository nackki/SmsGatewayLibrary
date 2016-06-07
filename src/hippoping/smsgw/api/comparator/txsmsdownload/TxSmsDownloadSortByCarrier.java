/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.txsmsdownload;

import hippoping.smsgw.api.db.TxSmsDownload;
import java.util.Comparator;

/**
 *
 * @author nack
 */
public class TxSmsDownloadSortByCarrier implements Comparator<TxSmsDownload> {
    public int compare(TxSmsDownload s1, TxSmsDownload s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getOper().toString().compareTo(s2.getOper().toString());
    }
}
