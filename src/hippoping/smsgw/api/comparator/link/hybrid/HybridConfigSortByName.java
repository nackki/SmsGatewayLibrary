package hippoping.smsgw.api.comparator.link.hybrid;

import hippoping.smsgw.api.db.HybridConfig;
import java.util.Comparator;

public class HybridConfigSortByName
        implements Comparator<HybridConfig> {

    public int compare(HybridConfig s1, HybridConfig s2) {
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }
        return s1.getName().compareTo(s2.getName());
    }
}