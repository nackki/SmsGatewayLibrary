/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.subscriptionsummaryreport;

import hippoping.smsgw.api.db.report.SummaryMonthlyReport;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class SubscriptionSummaryReportSortByMonth implements Comparator<SummaryMonthlyReport> {
    public int compare(SummaryMonthlyReport s1, SummaryMonthlyReport s2) {
        return ( (s1.getYear()<<16) + s1.getMonth() ) - ( (s2.getYear()<<16) + s2.getMonth() );
    }
}
