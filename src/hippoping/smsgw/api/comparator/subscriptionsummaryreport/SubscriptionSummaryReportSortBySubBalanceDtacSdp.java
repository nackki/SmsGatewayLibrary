/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.subscriptionsummaryreport;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.report.SummaryReport;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class SubscriptionSummaryReportSortBySubBalanceDtacSdp implements Comparator<SummaryReport> {
    public int compare(SummaryReport s1, SummaryReport s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getSub_balance(CARRIER.DTAC_SDP) - s2.getSub_balance(CARRIER.DTAC_SDP);
    }
}
