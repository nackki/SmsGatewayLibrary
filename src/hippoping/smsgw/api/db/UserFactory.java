package hippoping.smsgw.api.db;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class UserFactory {

    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75F;
    static Hashtable userFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static User getUser(int uid) throws Exception {
        User object = null;
        if ((!userFactory.containsKey(Integer.valueOf(uid))) || ((object = (User) userFactory.get(Integer.valueOf(uid))) == null)) {
            System.out.println("caching, user " + uid + "");
            object = new User(uid);

            if (userFactory.size() > MAX_POOL_SIZE * FACTOR) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (userFactory.containsKey(Integer.valueOf(uid))) {
                userFactory.remove(Integer.valueOf(uid));
            }
            userFactory.put(Integer.valueOf(uid), object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        Vector v = new Vector(userFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            userFactory.remove(e);
            deleted++;
            if (deleted >= count) {
                break;
            }
        }
    }

    public static void remove() {
        Vector v = new Vector(userFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            userFactory.remove(e);
        }
    }
}