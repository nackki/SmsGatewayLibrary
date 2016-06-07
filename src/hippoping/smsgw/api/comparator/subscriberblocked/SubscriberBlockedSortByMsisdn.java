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
public class SubscriberBlockedSortByMsisdn implements Comparator<SubscriberBlocked> {
    public int compare(SubscriberBlocked s1, SubscriberBlocked s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getMsisdn().compareTo(s2.getMsisdn());
    }
}
