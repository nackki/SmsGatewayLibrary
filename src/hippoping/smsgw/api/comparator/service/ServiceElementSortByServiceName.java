/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hippoping.smsgw.api.comparator.service;

import hippoping.smsgw.api.db.ServiceElement;
import java.util.Comparator;

/**
 *
 * @author developer
 */
public class ServiceElementSortByServiceName implements Comparator<ServiceElement> {
    public int compare(ServiceElement s1, ServiceElement s2) {
        if (s1==null || s2==null) {
            return 0;
        }
        return s1.srvc_name.compareTo(s2.srvc_name);
    }
}
