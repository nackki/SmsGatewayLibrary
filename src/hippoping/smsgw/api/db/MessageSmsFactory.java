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

public class MessageSmsFactory {

    protected List<MessageSms> messageSmsList;
    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75F;
    static Hashtable messageSmsFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static MessageSms getMessage(int id) throws Exception {
        MessageSms object = null;
        if ((!messageSmsFactory.containsKey(Integer.valueOf(id))) || ((object = (MessageSms) messageSmsFactory.get(Integer.valueOf(id))) == null)) {
            System.out.println("caching, sms message " + id);
            object = new MessageSms(id);

            if (messageSmsFactory.size() > MAX_POOL_SIZE * FACTOR) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (messageSmsFactory.containsKey(Integer.valueOf(id))) {
                messageSmsFactory.remove(Integer.valueOf(id));
            }
            messageSmsFactory.put(Integer.valueOf(id), object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        Vector v = new Vector(messageSmsFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            messageSmsFactory.remove(e);
            deleted++;
            if (deleted >= count) {
                break;
            }
        }
    }

    public static void remove() {
        Vector v = new Vector(messageSmsFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            messageSmsFactory.remove(e);
        }
    }

    public static void remove(int id) {
        if (messageSmsFactory.containsKey(Integer.valueOf(id))) {
            messageSmsFactory.remove(Integer.valueOf(id));
        }
    }

    public List<MessageSms> getMessageSmsList() {
        return this.messageSmsList;
    }

    public MessageSmsFactory(Message.SMS_TYPE type, String keyword, int offset, int len, int uid) {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String wherekeyword = "";
                if ((keyword != null) && (!keyword.isEmpty())) {
                    wherekeyword = " AND content REGEXP '" + keyword + "'";
                }

                String wheretype = "";
                if (type != null) {
                    wheretype = " AND type=" + type.getId();
                }

                String limit = "";
                if (len > 0) {
                    limit = " LIMIT " + offset + ", " + len;
                }

                String whereuid = "";
                if (uid > 0) {
                    whereuid = " AND owner=" + uid;
                }

                String sql = "SELECT sms_mesg_id  FROM ctnt_sms_mesg WHERE 1   AND disposable = 0" + wherekeyword + wheretype + whereuid + limit;

                ResultSet rs = cp.execQuery(sql);
                this.messageSmsList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.messageSmsList.add(getMessage(rs.getInt(1)));
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