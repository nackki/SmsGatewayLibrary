/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.comparator.subscriberblocked;

import hippoping.smsgw.api.db.SubscriberBlocked;
import java.util.Comparator;
import lib.common.DatetimeUtil;

/**
 *
 * @author developer
 */
public class SubscriberBlockedSortByCreateDate implements Comparator<SubscriberBlocked> {

    public int compare(SubscriberBlocked s1, SubscriberBlocked s2) {
        if (s1 == null || s2 == null) {
            return 0;
        }
        if (s1.getCreate_dt() == null && s2.getCreate_dt() == null) {
            return 0;
        } else if (s2.getCreate_dt() == null) {
            return 1;
        } else if (s1.getCreate_dt() == null) {
            return -1;
        }
        String dd1 = DatetimeUtil.print("yyyyMMdd", DatetimeUtil.toDate(s1.getCreate_dt()));
        String dd2 = DatetimeUtil.print("yyyyMMdd", DatetimeUtil.toDate(s2.getCreate_dt()));
        return dd1.compareTo(dd2);
    }
}
