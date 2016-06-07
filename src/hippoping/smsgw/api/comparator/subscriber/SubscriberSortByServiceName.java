/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.subscriber;

import hippoping.smsgw.api.db.Subscriber;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class SubscriberSortByServiceName implements Comparator<Subscriber> {
    public int compare(Subscriber s1, Subscriber s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getSrvc_name().compareTo(s2.getSrvc_name());
    }
}
