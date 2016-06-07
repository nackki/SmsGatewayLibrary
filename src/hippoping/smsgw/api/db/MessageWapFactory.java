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

public class MessageWapFactory {

    protected List<MessageWap> messageWapList;
    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75F;
    static Hashtable MessageWapFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static MessageWap getMessage(int id) throws Exception {
        MessageWap object = null;
        if ((!MessageWapFactory.containsKey(Integer.valueOf(id))) || ((object = (MessageWap) MessageWapFactory.get(Integer.valueOf(id))) == null)) {
            System.out.println("caching, wap message " + id);
            object = new MessageWap(id);

            if (MessageWapFactory.size() > MAX_POOL_SIZE * FACTOR) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (MessageWapFactory.containsKey(Integer.valueOf(id))) {
                MessageWapFactory.remove(Integer.valueOf(id));
            }
            MessageWapFactory.put(Integer.valueOf(id), object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        Vector v = new Vector(MessageWapFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            MessageWapFactory.remove(e);
            deleted++;
            if (deleted >= count) {
                break;
            }
        }
    }

    public static void remove() {
        Vector v = new Vector(MessageWapFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            MessageWapFactory.remove(e);
        }
    }

    public static void remove(int id) {
        if (MessageWapFactory.containsKey(Integer.valueOf(id))) {
            MessageWapFactory.remove(Integer.valueOf(id));
        }
    }

    public List<MessageWap> getMessageWapList() {
        return this.messageWapList;
    }

    public MessageWapFactory(String keyword, int offset, int len) {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String wherekeyword = "";
                if ((keyword != null) && (!keyword.isEmpty())) {
                    wherekeyword = " AND url_jar REGEXP '" + keyword + "' OR title REGEXP '" + keyword + "'";
                }

                String limit = "";
                if (len > 0) {
                    limit = " LIMIT " + offset + ", " + len;
                }

                String sql = "SELECT wap_push_id  FROM ctnt_sms_wap WHERE 1   AND disposable = 0" + wherekeyword + limit;

                ResultSet rs = cp.execQuery(sql);
                this.messageWapList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.messageWapList.add(getMessage(rs.getInt(1)));
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