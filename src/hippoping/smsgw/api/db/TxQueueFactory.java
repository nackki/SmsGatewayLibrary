package hippoping.smsgw.api.db;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public class TxQueueFactory {
    
    private static final Logger log = Logger.getLogger(TxQueueFactory.class.getName());

    private static final int MAX_POOL_SIZE = 2000;
    private static final float RATIO = 0.3F;
    private static Set queue = new LinkedHashSet();

    public static boolean isEmpty() {
        return queue.isEmpty();
    }

    public static boolean isAvailable() {
        return queue.size() < MAX_POOL_SIZE;
    }

    public static boolean isBelow() {
        return queue.size() < (MAX_POOL_SIZE * RATIO);
    }

    public static boolean isBelow(float ratio) {
        return queue.size() < MAX_POOL_SIZE * ratio;
    }

    /**
     * @deprecated
     */
    public static boolean isContained(Object obj) {
        return queue.contains(obj);
    }

    public static int enqueue(Object obj) {
        int size = -1;
        if (queue.add(obj)) {
            size = queue.size();
        }

        return size;
    }

    public static int enqueue(Object obj, Boolean check_dup) {
        int size = -1;
        if (queue.add(obj)) {
            size = queue.size();
        }

        return size;
    }

    public static Object dequeue() {
        synchronized (TxQueueFactory.class) {
            Object obj = null;
            Iterator itr = queue.iterator();
            if (itr.hasNext()) {
                obj = itr.next();
                if (!queue.remove(obj)) {
                    log.warning("remove txq[" + ((TxQueue)obj).getTx_queue_id() + "] from TxQueue failed, return null!!");
                    obj = null;
                }
            }
            return obj;
        }
    }

    public static void clear() {
        queue.clear();
    }
}