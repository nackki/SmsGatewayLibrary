package hippoping.smsgw.api.comparator.MessageMms;

import hippoping.smsgw.api.db.MessageMms;
import java.util.Comparator;

public class MessageMmsSortBySubject
        implements Comparator<MessageMms> {

    public int compare(MessageMms s1, MessageMms s2) {
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }
        if ((s1.getSubject() == null) || (s2.getSubject() == null)) {
            return 0;
        }
        return s1.getSubject().compareTo(s2.getSubject());
    }
}