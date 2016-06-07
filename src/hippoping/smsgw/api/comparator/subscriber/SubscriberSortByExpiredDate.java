/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.subscriber;

import hippoping.smsgw.api.db.Subscriber;
import java.util.Comparator;

/**
 *
 * @author ITZONE
 */
public class SubscriberSortByExpiredDate implements Comparator<Subscriber> {

    public int compare(Subscriber s1, Subscriber s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        if (s1.getExpired_date() == null && s2.getExpired_date() == null) {
            return 0;
        } else if (s2.getExpired_date() == null) {
            return 1;
        } else if (s1.getExpired_date() == null) {
            return -1;
        }

        String dd1 = s1.getExpired_date("yyyyMMdd");
        String dd2 = s2.getExpired_date("yyyyMMdd");
        return dd1.compareTo(dd2);
    }
}
