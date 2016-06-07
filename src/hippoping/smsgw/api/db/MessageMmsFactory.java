package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import lib.common.DBPoolManager;

public class MessageMmsFactory {

    protected List<MessageMms> messageMmsList;
    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75F;
    static Hashtable messageMmsFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static MessageMms getMessage(int id) throws Exception {
        MessageMms object = null;
        if ((!messageMmsFactory.containsKey(Integer.valueOf(id))) || ((object = (MessageMms) messageMmsFactory.get(Integer.valueOf(id))) == null)) {
            System.out.println("caching, mms message " + id);
            object = new MessageMms(id);

            if (messageMmsFactory.size() > MAX_POOL_SIZE * FACTOR) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (messageMmsFactory.containsKey(Integer.valueOf(id))) {
                messageMmsFactory.remove(Integer.valueOf(id));
            }
            messageMmsFactory.put(Integer.valueOf(id), object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        Vector v = new Vector(messageMmsFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            messageMmsFactory.remove(e);
            deleted++;
            if (deleted >= count) {
                break;
            }
        }
    }

    public static void remove() {
        Vector v = new Vector(messageMmsFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            messageMmsFactory.remove(e);
        }
    }

    public static void remove(int id) {
        if (messageMmsFactory.containsKey(Integer.valueOf(id))) {
            messageMmsFactory.remove(Integer.valueOf(id));
        }
    }

    public static void flush() {
        if (!messageMmsFactory.isEmpty()) {
            messageMmsFactory.clear();
        }
    }

    public List<MessageMms> getMessageMmsList() {
        return this.messageMmsList;
    }

    public MessageMmsFactory(String keyword, int offset, int len, int uid) {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String wherekeyword = "";
                if ((keyword != null) && (!keyword.isEmpty())) {
                    wherekeyword = " AND ( subject REGEXP '" + keyword + "' OR `from` REGEXP '" + keyword + "' ) ";
                }

                String whereuid = "";
                if (uid > 0) {
                    whereuid = " AND owner=" + uid;
                }

                String limit = "";
                if (len > 0) {
                    limit = " LIMIT " + offset + ", " + len;
                }

                String sql = "SELECT mms_mesg_id  FROM ctnt_mms_mesg WHERE 1   AND disposable = 0" + wherekeyword + whereuid + limit;

                ResultSet rs = cp.execQuery(sql);
                this.messageMmsList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.messageMmsList.add(getMessage(rs.getInt(1)));
                    } catch (Exception e) {
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}