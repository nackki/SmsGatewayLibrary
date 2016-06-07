package hippoping.smsgw.api.operator.dtac;

import com.dtac.cpa.subscription.status.query.CpaSubscriptionStatusQuery;
import com.dtac.cpa.subscription.status.reply.CpaSubscriptionStatusReply;
import com.dtac.cpa.subscription.status.reply.SubscriptionStatus;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.Subscriber;
import hippoping.smsgw.api.db.SubscriberGroup;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;
import lib.common.HttpClientConnection;
import lib.common.HttpClientConnectionPool;
import lib.common.HttpClientIndexing;
import lib.common.HttpPost;

public class SubscriberSynchronize {

    private static final Logger log = Logger.getLogger(SubscriberSynchronize.class.getName());
    private static final int _MAX_SYNC_ENTRY = 50;

    public static XMLSerializer getXMLSerializer(OutputStream os) {
        OutputFormat of = new OutputFormat();

        String[] cdata = {"^not use cdata"};
        of.setCDataElements(cdata);
        of.setNonEscapingElements(cdata);

        of.setPreserveSpace(true);
        of.setIndenting(true);

        XMLSerializer serializer = new XMLSerializer(of);
        serializer.setOutputByteStream(os);

        return serializer;
    }

    public void sendSyncStatus(ServiceElement ns, Subscriber[] msisdns, String register, String expired, String canceled)
            throws Exception {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.dtac.cpa.subscription.status.query");
            Unmarshaller u = jc.createUnmarshaller();
            Marshaller m = jc.createMarshaller();
            StringBuilder xmlStr = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><cpa-subscription-status-query>        <authentication>                <user>myuser</user>                <password>mypassword</password>        </authentication>        <destination>                <serviceid>90114450001</serviceid>        </destination>" + (register != null ? "        <start-date>20100216</start-date>" : "") + (canceled != null ? "        <canceled-date>20100216</canceled-date>" : "") + (expired != null ? "        <expired-date>20100216</expired-date>" : "") + "</cpa-subscription-status-query>");

            CpaSubscriptionStatusQuery query = (CpaSubscriptionStatusQuery) u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));

            query.getAuthentication().setUser(ns.oper_config.user);
            query.getAuthentication().setPassword(ns.oper_config.password);
            query.getDestination().setServiceid(ns.bcast_srvc_id);

            if (register != null) {
                query.setStartDate(register);
            }
            if (canceled != null) {
                query.setCanceledDate(canceled);
            }
            if (expired != null) {
                query.setExpiredDate(expired);
            }

            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            if ((msisdns != null) && (msisdns.length > 0)) {
                int used = 0;
                List subList = new ArrayList(Arrays.asList(msisdns));
                List syncedList = new ArrayList();

                HttpClientIndexing key = new HttpClientIndexing();
                key.setUrl(ns.oper_config.sub_stat_url);
                key.setUserpswd("", "");
                key.setTimeout(60000);
                key.setConnection("keep-live");

                HttpClientConnectionPool pool = new HttpClientConnectionPool();

                while (!subList.isEmpty()) {
                    query.getDestination().getMsisdn().clear();
                    used = 0;
                    syncedList.clear();

                    for (int i = 0; (i < 50) && (!subList.isEmpty()); i++) {
                        Subscriber subscriber = (Subscriber) subList.remove(0);
                        syncedList.add(subscriber);

                        query.getDestination().getMsisdn().add(subscriber.getMsisdn());
                        used++;
                    }

                    if (used > 0) {
                        String resp = "";
                        HttpClientConnection conn = pool.open(key);
                        try {
                            XMLSerializer serializer = getXMLSerializer(conn.getWriter());

                            m.marshal(query, serializer.asContentHandler());

                            StringWriter sw = new StringWriter();
                            m.marshal(query, sw);
                            log.log(Level.INFO, sw.getBuffer().toString());
                            sw.close();

                            resp = conn.getResponse();
                            List subscriptionStatusList = readReply(resp, ns);
                            updateSyncProfile(syncedList, subscriptionStatusList, ns.srvc_main_id);
                        } catch (SocketTimeoutException se) {
                            log.log(Level.SEVERE, "Socket timeout!!", se);
                        } catch (UnmarshalException ue) {
                            log.log(Level.SEVERE, "Invalid reply!!", ue);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Connection error!!", e);
                        } finally {
                            conn.close();
                        }
                    }
                }
            } else {
                HttpPost httpPost = new HttpPost();
                String resp = "";
                try {
                    httpPost.setUrl(ns.oper_config.sub_stat_url);
                    httpPost.setUserpswd("", "");
                    httpPost.setTimeout(60000);

                    XMLSerializer serializer = getXMLSerializer(httpPost.getWriter());

                    m.marshal(query, serializer.asContentHandler());

                    StringWriter sw = new StringWriter();
                    m.marshal(query, sw);
                    log.log(Level.INFO, sw.getBuffer().toString());
                    sw.close();

                    resp = httpPost.getResponse();
                    List subscriptionStatusList = readReply(resp, ns);
                    updateSyncProfile(subscriptionStatusList, ns.srvc_main_id);
                } catch (SocketTimeoutException e) {
                    log.log(Level.SEVERE, "Socket timeout!!");
                    throw e;
                } catch (Exception e1) {
                    log.log(Level.SEVERE, "error on connecting!!");
                    throw e1;
                } finally {
                    httpPost.disconnect();
                }
            }
        } catch (UnsupportedEncodingException uex) {
            log.log(Level.SEVERE, "exception caught", uex);
        } catch (Exception e) {
            throw e;
        }
    }

    public int updateProfile(SubscriptionStatus subStatus, int srvc_main_id, SubscriberGroup.SUB_STATUS state) throws Exception {
        int rows = 0;

        String register_date = subStatus.getStartDate();
        String expired_date = subStatus.getExpiredDate();

        if (register_date != null) {
            String fmt = "yyyyMMddHHmmss";
            if (register_date.length() == "20100705170715".length()) {
                register_date = DatetimeUtil.changeDateFormat(register_date, fmt, "yyyy-MM-dd");
            } else if (register_date.length() == "20100705".length()) {
                fmt = "yyyyMMdd";
                register_date = DatetimeUtil.changeDateFormat(register_date, fmt, "yyyy-MM-dd");
            }
        }

        if (expired_date != null) {
            String fmt = "yyyyMMddHHmmss";
            if (expired_date.length() == "20100705170715".length()) {
                expired_date = DatetimeUtil.changeDateFormat(expired_date, fmt, "yyyy-MM-dd");
            } else if (expired_date.length() == "20100705".length()) {
                fmt = "yyyyMMdd";
                expired_date = DatetimeUtil.changeDateFormat(expired_date, fmt, "yyyy-MM-dd");
            }

        }

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "UPDATE mmbr_dtac SET state=" + state.getId() + "   , unregister_date=" + (state == SubscriberGroup.SUB_STATUS.UNREGISTER ? "CURDATE()" : "NULL") + (register_date != null ? "   , register_date='" + register_date + "'" : "") + (expired_date != null ? "   , expired_date='" + expired_date + "'" : "") + " WHERE msisdn=?" + "   AND srvc_main_id=?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setString(1, subStatus.getMsisdn());
            cp.getPreparedStatement().setInt(2, srvc_main_id);
            rows = cp.execUpdatePrepareStatement();
            log.log(Level.INFO, "updated subscription profile {0} record(s).", Integer.valueOf(rows));
        } catch (SQLException e) {
            log.log(Level.SEVERE, "sql error!!", e);
        } finally {
            cp.release();
        }
        return rows;
    }

    public int updateSyncProfile(List<SubscriptionStatus> subStatusList, int srvc_main_id) throws Exception {
        int rows = 0;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "UPDATE mmbr_dtac   SET state=?     , register_date=?     , expired_date=?     , balanced_date=?     , unregister_date=?     , free_trial=? WHERE msisdn=?   AND srvc_main_id=?";

            cp.prepareStatement(sql);

            for (int i = 0; i < subStatusList.size(); i++) {
                SubscriptionStatus subStatus = (SubscriptionStatus) subStatusList.get(i);
                try {
                    cp.getPreparedStatement().setInt(1, (subStatus.getStatus().equals("A")) || (subStatus.getStatus().equals("P")) ? SubscriberGroup.SUB_STATUS.REGISTER.getId() : SubscriberGroup.SUB_STATUS.UNREGISTER.getId());

                    cp.getPreparedStatement().setString(2, subStatus.getStartDate());
                    cp.getPreparedStatement().setString(3, subStatus.getExpiredDate());
                    if (subStatus.getLatestBilledDate() != null) {
                        cp.getPreparedStatement().setString(4, subStatus.getLatestBilledDate());
                    } else {
                        cp.getPreparedStatement().setNull(4, 91);
                    }
                    if ((subStatus.getStatus().equals("A")) || (subStatus.getStatus().equals("P"))) {
                        cp.getPreparedStatement().setNull(5, 91);
                    } else {
                        cp.getPreparedStatement().setString(5, subStatus.getExpiredDate());
                    }
                    cp.getPreparedStatement().setInt(6, subStatus.getStatus().equals("P") ? (int) DatetimeUtil.dateDiff(subStatus.getStartDate(), subStatus.getExpiredDate(), "yyyyMMdd", DatetimeUtil.DF_DATE.DF_DAY) : 0);

                    cp.getPreparedStatement().setString(7, subStatus.getMsisdn());
                    cp.getPreparedStatement().setInt(8, srvc_main_id);
                    int r = cp.execUpdatePrepareStatement();

                    if (r == 0) {
                        r = createProfile(subStatus, srvc_main_id);
                    }

                    rows += r;
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "sql error!!", e);
                }
            }

            log.log(Level.INFO, "updated subscriber {0} record(s).", Integer.valueOf(rows));
        } finally {
            cp.release();
        }
        return rows;
    }

    public int updateSyncProfile(List<Subscriber> syncedList, List<SubscriptionStatus> subStatusList, int srvc_main_id) throws Exception {
        int rows = 0;

        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = "UPDATE mmbr_dtac   SET state=?     , register_date=?     , expired_date=?     , balanced_date=?     , unregister_date=?     , free_trial=? WHERE msisdn=?   AND srvc_main_id=?";

            cp.prepareStatement(sql);

            for (int i = 0; i < subStatusList.size(); i++) {
                SubscriptionStatus subStatus = (SubscriptionStatus) subStatusList.get(i);
                Subscriber sub;
                try {
                    sub = new Subscriber(subStatus.getMsisdn(), srvc_main_id, CARRIER.DTAC);

                    if ((sub != null)
                            && (syncedList.remove(sub))) {
                        if (((subStatus.getStatus().equals("A")) || (subStatus.getStatus().equals("P"))) && (sub.getState() == SubscriberGroup.SUB_STATUS.REGISTER.getId()) && (subStatus.getExpiredDate() != null) && (subStatus.getExpiredDate().matches("^" + DatetimeUtil.print("yyyyMMdd", sub.getExpired_date()) + ".*"))) {
                            continue;
                        }

                        if ((!subStatus.getStatus().equals("A")) && (!subStatus.getStatus().equals("P")) && (sub.getState() == SubscriberGroup.SUB_STATUS.UNREGISTER.getId())) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    cp.getPreparedStatement().setInt(1, (subStatus.getStatus().equals("A")) || (subStatus.getStatus().equals("P")) ? SubscriberGroup.SUB_STATUS.REGISTER.getId() : SubscriberGroup.SUB_STATUS.UNREGISTER.getId());

                    cp.getPreparedStatement().setString(2, subStatus.getStartDate());
                    cp.getPreparedStatement().setString(3, subStatus.getExpiredDate());
                    if (subStatus.getLatestBilledDate() != null) {
                        cp.getPreparedStatement().setString(4, subStatus.getLatestBilledDate());
                    } else {
                        cp.getPreparedStatement().setNull(4, 91);
                    }
                    if ((subStatus.getStatus().equals("A")) || (subStatus.getStatus().equals("P"))) {
                        cp.getPreparedStatement().setNull(5, 91);
                    } else {
                        cp.getPreparedStatement().setString(5, subStatus.getExpiredDate());
                    }
                    cp.getPreparedStatement().setInt(6, subStatus.getStatus().equals("P") ? (int) DatetimeUtil.dateDiff(subStatus.getStartDate(), subStatus.getExpiredDate(), "yyyyMMdd", DatetimeUtil.DF_DATE.DF_DAY) : 0);

                    cp.getPreparedStatement().setString(7, subStatus.getMsisdn());
                    cp.getPreparedStatement().setInt(8, srvc_main_id);
                    rows += cp.execUpdatePrepareStatement();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "sql error!!", e);
                }

            }

            for (int i = 0; i < syncedList.size(); i++) {
                if (((Subscriber) syncedList.get(i)).getState() != SubscriberGroup.SUB_STATUS.UNREGISTER.getId()) {
                    try {
                        log.log(Level.INFO, "unsub invalid result for {0} -> return: {1}", new Object[]{((Subscriber) syncedList.get(i)).getMsisdn(), ((Subscriber) syncedList.get(i)).doUnsub()});
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "exception caught", e);
                    }
                }
            }

            log.log(Level.INFO, "update unmatched {0} record(s)/ unsub no result {1} record(s).", new Object[]{Integer.valueOf(rows), Integer.valueOf(syncedList.size())});
        } finally {
            cp.release();
        }
        return rows;
    }

    public int createProfile(SubscriptionStatus subStatus, int srvc_main_id) throws Exception {
        int rows = 0;

        DBPoolManager cp = new DBPoolManager();
        String sql = "INSERT IGNORE INTO mmbr_" + CARRIER.DTAC.toString().toLowerCase() + " (msisdn, srvc_main_id, ctnt_ctr, free_trial" + " , rmdr_ctr, extd_ctr, non_expired, srvc_chrg_type_id, srvc_chrg_amnt" + " , state, register_date, expired_date, balanced_date, unregister_date)" + " SELECT ?, s.srvc_main_id, s.ctnt_ctr, ?" + " , s.rmdr_ctr, s.rchg_ctr, 0, m.srvc_chrg_type_id, m.srvc_chrg_amnt" + " , ?, ?, ?, " + (subStatus.getLatestBilledDate() != null ? "'" + subStatus.getLatestBilledDate() + "'" : "NULL") + " , " + (subStatus.getStatus().equals("E") ? "'" + subStatus.getExpiredDate() + "'" : "NULL") + " FROM srvc_sub s" + " INNER JOIN srvc_main m" + " ON s.srvc_main_id = m.srvc_main_id" + " WHERE s.srvc_main_id = ?" + " LIMIT 1";
        try {
            cp.prepareStatement(sql);

            cp.getPreparedStatement().setString(1, subStatus.getMsisdn());
            cp.getPreparedStatement().setInt(2, subStatus.getStatus().equals("P") ? (int) DatetimeUtil.dateDiff(subStatus.getStartDate(), subStatus.getExpiredDate(), "yyyyMMdd", DatetimeUtil.DF_DATE.DF_DAY) : 0);

            cp.getPreparedStatement().setInt(3, (subStatus.getStatus().equals("A")) || (subStatus.getStatus().equals("P")) ? SubscriberGroup.SUB_STATUS.REGISTER.getId() : SubscriberGroup.SUB_STATUS.UNREGISTER.getId());

            cp.getPreparedStatement().setString(4, subStatus.getStartDate());
            cp.getPreparedStatement().setString(5, subStatus.getExpiredDate());
            cp.getPreparedStatement().setInt(6, srvc_main_id);

            log.log(Level.INFO, "new subscriber {0} record(s) inserted.", Integer.valueOf(cp.execUpdatePrepareStatement()));
        } catch (SQLException e) {
            log.log(Level.SEVERE, "SQL error!!", e);
        } finally {
            cp.release();
        }
        return rows;
    }

    public List<SubscriptionStatus> readReply(String content, ServiceElement service) throws Exception {
        List subscriptionStatusList = new ArrayList();
        try {
            JAXBContext jc = JAXBContext.newInstance("com.dtac.cpa.subscription.status.reply");
            Unmarshaller um = jc.createUnmarshaller();

            CpaSubscriptionStatusReply cpaReply = (CpaSubscriptionStatusReply) um.unmarshal(new StreamSource(new StringReader(content)));

            List subStatusList = cpaReply.getSubscriptionStatus();
            Iterator itr = subStatusList.iterator();

            while (itr.hasNext()) {
                SubscriptionStatus subStatus = (SubscriptionStatus) itr.next();
                subscriptionStatusList.add(subStatus);
            }
        } catch (Exception e) {
            throw e;
        }

        return subscriptionStatusList;
    }
}