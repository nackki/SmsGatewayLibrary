package hippoping.smsgw.api.comparator.subscriptionactivereport;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.report.SummaryReport;
import java.util.Comparator;

public class SubscriptionActiveReportSortByActiveAis
  implements Comparator<SummaryReport>
{
  public int compare(SummaryReport s1, SummaryReport s2)
  {
    if ((s1 == null) || (s2 == null)) {
      return 0;
    }
    return s1.getMt_chrg_total(OperConfig.CARRIER.AIS) + s1.getMt_chrg_total(OperConfig.CARRIER.AIS_LEGACY) - (s2.getMt_chrg_total(OperConfig.CARRIER.AIS) + s2.getMt_chrg_total(OperConfig.CARRIER.AIS_LEGACY));
  }
}