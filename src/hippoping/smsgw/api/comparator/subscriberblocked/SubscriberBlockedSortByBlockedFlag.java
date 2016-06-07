/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.subscriberblocked;

import hippoping.smsgw.api.db.SubscriberBlocked;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class SubscriberBlockedSortByBlockedFlag implements Comparator<SubscriberBlocked> {
    public int compare(SubscriberBlocked s1, SubscriberBlocked s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        int s1_b = (s1.isBlocked())?1:0;
        int s2_b = (s2.isBlocked())?1:0;
        return s1_b - s2_b;
    }
}
