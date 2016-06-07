package hippoping.smsgw.api.comparator.MessageMms;

import hippoping.smsgw.api.db.MessageMms;
import java.util.Comparator;

public class MessageMmsSortByFrom
        implements Comparator<MessageMms> {

    public int compare(MessageMms s1, MessageMms s2) {
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }
        if ((s1.getFrom() == null) || (s2.getFrom() == null)) {
            return 0;
        }
        return s1.getFrom().compareTo(s2.getFrom());
    }
}