/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.thirdpartyconfig;

import hippoping.smsgw.api.db.ThirdPartyConfig;
import java.util.Comparator;

/**
 *
 * @author ITZONE
 */
public class ThirdPartyConfigSortByUrl implements Comparator<ThirdPartyConfig> {
    public int compare(ThirdPartyConfig s1, ThirdPartyConfig s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getUrl().compareTo(s2.getUrl());
    }
}
