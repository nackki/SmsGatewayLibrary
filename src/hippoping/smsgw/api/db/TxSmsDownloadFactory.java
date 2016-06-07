package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import lib.common.DBPoolManager;

public class TxSmsDownloadFactory {

    protected Date from;
    protected Date to;
    protected List<TxSmsDownload> txSmsDownloadList;
    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75F;
    static Hashtable txSmsDownloadFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static TxSmsDownload getMessage(int id) throws Exception {
        TxSmsDownload object = null;
        if ((!txSmsDownloadFactory.containsKey(Integer.valueOf(id))) || ((object = (TxSmsDownload) txSmsDownloadFactory.get(Integer.valueOf(id))) == null)) {
            System.out.println("caching, tx sms download " + id);
            object = new TxSmsDownload(id);

            if (txSmsDownloadFactory.size() > MAX_POOL_SIZE * FACTOR) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (txSmsDownloadFactory.containsKey(Integer.valueOf(id))) {
                txSmsDownloadFactory.remove(Integer.valueOf(id));
            }
            txSmsDownloadFactory.put(Integer.valueOf(id), object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        Vector v = new Vector(txSmsDownloadFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            txSmsDownloadFactory.remove(e);
            deleted++;
            if (deleted >= count) {
                break;
            }
        }
    }

    public static void remove() {
        Vector v = new Vector(txSmsDownloadFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            txSmsDownloadFactory.remove(e);
        }
    }

    public Date getFrom() {
        return this.from;
    }

    public Date getTo() {
        return this.to;
    }

    public List<TxSmsDownload> getTxSmsDownloadList() {
        return this.txSmsDownloadList;
    }

    public TxSmsDownloadFactory(Date from, Date to, OperConfig.CARRIER oper, int srvc_main_id, String msisdn, User user, String keyword) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereuid = "";
                if (user != null) {
                    whereuid = " AND ( 0";
                    for (int i = 0; i < user.getChildUid().length; i++) {
                        if (!user.getChildUid()[i].isEmpty()) {
                            whereuid = whereuid + " OR ss.uid=" + user.getChildUid()[i];
                        }
                    }
                    whereuid = whereuid + " )";
                }

                String wheresrvc = "";
                if (srvc_main_id > 0) {
                    wheresrvc = " AND t.srvc_main_id=" + srvc_main_id;
                }

                String wheremsisdn = "";
                if ((msisdn != null) && (!msisdn.isEmpty())) {
                    wheremsisdn = " AND t.msisdn LIKE '%" + msisdn + "%'";
                }

                String whereoper = "";
                if ((oper != null) && (oper != OperConfig.CARRIER.ALL)) {
                    whereoper = " AND t.oper_id=" + oper.getId();
                }

                String wherekey = "";
                if ((keyword != null) && (!keyword.isEmpty())) {
                    wherekey = " AND t.keyword='" + keyword + "'";
                }

                String wheredate = "";
                String date_fmt = "yyyy-MM-dd HH:mm:ss";
                if ((from != null) && (to != null)) {
                    wheredate = " AND t.datetime BETWEEN '" + new SimpleDateFormat(date_fmt).format(from) + "' AND '" + new SimpleDateFormat(date_fmt).format(to) + "'";
                } else if (from != null) {
                    wheredate = " AND t.datetime >= '" + new SimpleDateFormat(date_fmt).format(from) + "'";
                } else if (to != null) {
                    wheredate = " AND t.datetime <= '" + new SimpleDateFormat(date_fmt).format(to) + "'";
                }

                String sql = "SELECT t.id  FROM trns_sms_download t INNER JOIN srvc_sub ss    ON ss.srvc_main_id = t.srvc_main_id   AND ss.oper_id = t.oper_id   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId() + whereuid + " WHERE 1" + wheredate + wheresrvc + wheremsisdn + whereoper + wherekey;

                ResultSet rs = cp.execQuery(sql);
                this.txSmsDownloadList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.txSmsDownloadList.add(getMessage(rs.getInt("id")));
                    } catch (Exception e) {
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public TxSmsDownloadFactory(Date from, Date to, int srvc_main_id, User user, long fromid, int count) throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereuid = "";
                if (user != null) {
                    whereuid = " AND ( 0";
                    for (int i = 0; i < user.getChildUid().length; i++) {
                        if (!user.getChildUid()[i].isEmpty()) {
                            whereuid = whereuid + " OR ss.uid=" + user.getChildUid()[i];
                        }
                    }
                    whereuid = whereuid + " )";
                }

                String wheresrvc = "";
                if (srvc_main_id > 0) {
                    wheresrvc = " AND t.srvc_main_id=" + srvc_main_id;
                }

                String wherefromid = "";
                if (fromid > 0) {
                    wherefromid = " AND t.id>" + fromid;
                }

                String wheredate = "";
                String date_fmt = "yyyy-MM-dd HH:mm:ss";
                if ((from != null) && (to != null)) {
                    wheredate = " AND t.datetime BETWEEN '" + new SimpleDateFormat(date_fmt).format(from)
                            + "' AND '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(to) + "'";
                } else if (from != null) {
                    wheredate = " AND t.datetime >= '" + new SimpleDateFormat(date_fmt).format(from) + "'";
                } else if (to != null) {
                    wheredate = " AND t.datetime <= '" + new SimpleDateFormat(date_fmt).format(to) + "'";
                }

                String sql = "SELECT *"
                        + " FROM "
                        + " (SELECT t.id"
                        + "  FROM trns_sms_download t"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = t.srvc_main_id"
                        + "   AND ss.oper_id = t.oper_id"
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId() 
                        + whereuid 
                        + " WHERE 1" 
                        + wheredate 
                        + wheresrvc 
                        + wherefromid 
                        + " ORDER BY t.datetime DESC" 
                        + " LIMIT " + count + ") as tbl"
                        + " order by tbl.id";

                ResultSet rs = cp.execQuery(sql);
                this.txSmsDownloadList = new ArrayList();
                while (rs.next()) {
                    try {
                        this.txSmsDownloadList.add(getMessage(rs.getInt("id")));
                    } catch (Exception e) {
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static List<String> getKeywordList(List<TxSmsDownload> list) throws Exception {
        List keywords = new ArrayList();
        try {
            for (TxSmsDownload txSmsDownload : list) {
                if (!keywords.contains(txSmsDownload.getKeyword().toUpperCase()))
                    keywords.add(txSmsDownload.getKeyword().toUpperCase());
            }
        } catch (Exception e) {
            throw e;
        }

        return keywords;
    }
}