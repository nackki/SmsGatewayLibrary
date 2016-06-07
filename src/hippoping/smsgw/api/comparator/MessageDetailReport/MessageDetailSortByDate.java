package hippoping.smsgw.api.comparator.MessageDetailReport;

import hippoping.smsgw.api.content.manage.MessageDetail;
import java.util.Comparator;

public class MessageDetailSortByDate
        implements Comparator<MessageDetail> {

    public int compare(MessageDetail s1, MessageDetail s2) {
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }
        if ((s1.getTimestamp() == null) || (s2.getTimestamp() == null)) {
            return 0;
        }
        return s1.getTimestamp().compareTo(s2.getTimestamp());
    }
}