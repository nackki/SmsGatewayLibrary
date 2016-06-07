package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class ServiceContentMapFactory {

    private static final Logger log = Logger.getLogger(ServiceContentMapFactory.class.getName());
    static int MAX_POOL_SIZE = 1000;
    static float FACTOR = 0.75F;
    static Hashtable scmFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static List getServiceContentMap(int key) throws Exception {
        return (List) scmFactory.get(key);
    }

    /**
     * Add ServiceContentMap to SCM list
     *
     * @param scm
     * @return
     */
    public static int push(ServiceContentMap scm) {
        log.info("cache SCM: " + 
                scm.getScm_type().name() + "|" + 
                scm.getSrvc_id_mo() + "|" + 
                CARRIER.fromId(scm.getServiceElement().oper_id).name() + "|" + 
                scm.getKeyword() + "|" +
                scm.getKw_type().name());
        List list = (List) scmFactory.remove(scm.hashCode());
        if (list == null) {
            list = new LinkedList();
        }

        list.add(scm);

        if (scmFactory.size() > (MAX_POOL_SIZE * FACTOR)) {
            removeOlder(MAX_POOL_SIZE / 2);
        }

        // finally push object back to Factory
        scmFactory.put(scm.hashCode(), list);

        return list.size();
    }

    public static void removeOlder(int count) {
        Vector v = new Vector(scmFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            scmFactory.remove(e);
            deleted++;
            if (deleted >= count) {
                break;
            }
        }

        log.info("removing older buffer, ServiceContentMapFactory buffer size:" + scmFactory.size());
    }

    public static void remove() {
        Vector v = new Vector(scmFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            scmFactory.remove(e);
        }

        log.info("removing all buffer, ServiceContentMapFactory buffer size:" + scmFactory.size());
    }
}
