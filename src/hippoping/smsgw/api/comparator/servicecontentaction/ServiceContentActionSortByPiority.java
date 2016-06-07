/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.servicecontentaction;

import hippoping.smsgw.api.db.ServiceContentAction;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class ServiceContentActionSortByPiority implements Comparator<ServiceContentAction> {
    public int compare(ServiceContentAction s1, ServiceContentAction s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.getPiority() - s2.getPiority();
    }
}
