/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.link;

import hippoping.smsgw.api.db.OperConfig;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class LinkConfigureSortByName implements Comparator<OperConfig> {
    public int compare(OperConfig s1, OperConfig s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.conf_name.compareTo(s2.conf_name);
    }
}
