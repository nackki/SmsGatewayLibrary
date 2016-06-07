/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.MessageSms;

import hippoping.smsgw.api.db.MessageSms;
import java.util.Comparator;

/**
 *
 * @author ITZONE
 */
public class MessageSmsSortByContent implements Comparator<MessageSms> {
    public int compare(MessageSms s1, MessageSms s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        if (s1.getContent() == null || s2.getContent() == null) {
            return 0;
        }
        if (s1.getContent().length == 0 || s2.getContent().length == 0) {
            return 0;
        }
        return s1.getContent()[0].compareTo(s2.getContent()[0]);
    }
}
