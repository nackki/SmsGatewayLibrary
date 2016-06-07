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
public class MessageSmsSortByTitle implements Comparator<MessageSms> {

    public int compare(MessageSms s1, MessageSms s2) {
        if (s1 == null || s2 == null) {
            return 0;
        }
        if (s1.getMessageInfo().getTitle() == null || s2.getMessageInfo().getTitle() == null) {
            return 0;
        }
        if (s1.getMessageInfo().getTitle().isEmpty() || s2.getMessageInfo().getTitle().isEmpty()) {
            return 0;
        }
        return s1.getMessageInfo().getTitle().compareTo(s2.getMessageInfo().getTitle());
    }
}
