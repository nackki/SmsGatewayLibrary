package hippoping.smsgw.api.comparator.LogEvent;

import hippoping.smsgw.api.db.LogEvent;
import java.util.Comparator;

public class LogEventSortByTimestamp
        implements Comparator<LogEvent> {

    public int compare(LogEvent s1, LogEvent s2) {
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }
        if ((s1.timestamp == null) || (s2.timestamp == null)) {
            return 0;
        }
        return s1.timestamp.compareTo(s2.timestamp);
    }
}