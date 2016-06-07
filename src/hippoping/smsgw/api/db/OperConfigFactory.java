/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author nack_ki
 */
public class OperConfigFactory {

    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75f;
    static Hashtable operConfigFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static OperConfig getOperConfig(int conf_id) throws Exception {
        OperConfig object = null;
        if (!operConfigFactory.containsKey(conf_id)
                || (object = (OperConfig) operConfigFactory.get(conf_id)) == null) {
            System.out.println("caching, link config " + conf_id + "");
            object = new OperConfig(conf_id);

            // Add to hashtable
            if (operConfigFactory.size() > (MAX_POOL_SIZE * FACTOR)) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (operConfigFactory.containsKey(conf_id)) {
                operConfigFactory.remove(conf_id);
            }
            operConfigFactory.put(conf_id, object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        // remove an oldest object
        Vector v = new Vector(operConfigFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            operConfigFactory.remove(e);
            if ((++deleted) >= count) {
                break;
            }
        }
    }

    public static void remove() {
        // remove an oldest object
        Vector v = new Vector(operConfigFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            operConfigFactory.remove(e);
        }
    }
}
