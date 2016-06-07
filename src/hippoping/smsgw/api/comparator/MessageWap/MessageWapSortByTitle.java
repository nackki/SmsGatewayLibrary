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
public class MessageWapSortByTitle implements Comparator<MessageWap> {
    public int compare(MessageWap s1, MessageWap s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        if (s1.title == null || s2.title == null) {
            return 0;
        }
        return s1.title.compareTo(s2.title);
    }
}
