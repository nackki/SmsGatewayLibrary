/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.droconfigure;

import hippoping.smsgw.api.db.DroConfigure;
import java.util.Comparator;

/**
 *
 * @author ITZONE
 */
public class DroConfigureSortByOper implements Comparator<DroConfigure> {
    public int compare(DroConfigure s1, DroConfigure s2) {
        if (s1==null || s2==null) {
            return 0;
        }

        return s1.getService().oper_id - s2.getService().oper_id;
    }
}
