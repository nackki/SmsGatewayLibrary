/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.subscriptionsummaryreport;

import hippoping.smsgw.api.db.report.SummaryDailyReport;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class SubscriptionSummaryReportSortByDate implements Comparator<SummaryDailyReport> {
    public int compare(SummaryDailyReport s1, SummaryDailyReport s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getDate().compareTo(s2.getDate());
    }
}
