/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.MessageWap;

import hippoping.smsgw.api.db.MessageWap;
import java.util.Comparator;

/**
 *
 * @author ITZONE
 */
public class MessageWapSortByUrl implements Comparator<MessageWap> {
    public int compare(MessageWap s1, MessageWap s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        if (s1.url == null || s2.url == null) {
            return 0;
        }
        return s1.url.compareTo(s2.url);
    }
}
