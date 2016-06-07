/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_TYPE;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.StringConvert;

/**
 *
 * @author nack_ki
 */
public class UssdReplyMessageFactory {

    private static final Logger log = Logger.getLogger(UssdReplyMessageFactory.class.getName());
    public static final String DEF_MSG = "Thank you and wait sms confirm";
    static int MAX_POOL_SIZE = 100;
    static float FACTOR = 0.75f;
    static Hashtable ussdReplyMessageFactory = new Hashtable(MAX_POOL_SIZE, FACTOR);

    public static String getReplyMessage(com.truemove.css.ussd.request.MessageElement request, CARRIER oper) throws Exception {
        String object = null;
        String ussd = request.getSms().getUd().getContent();
        int hashcode = StringConvert.computeHash(ussd) + oper.getId();
        if (!ussdReplyMessageFactory.containsKey(hashcode)
                || (object = (String) ussdReplyMessageFactory.get(hashcode)) == null) {
            log.info("caching, USSD mo reply message srvc_id_mo|" + ussd + "");

            String srvc_id = request.getSms().getDestination().getAddress().getNumber().getValue();
            String srvc_id_mo = request.getSms().getServiceId();

            ServiceElement[] se = ServiceElement.getServiceElementList(
                    oper, srvc_id_mo,
                    ServiceElement.SERVICE_TYPE.ALL.getId(),
                    ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

            if (se == null || se.length == 0) {
                // no service found
                log.log(Level.INFO,
                        "Invalid service id from the request, srvc_id_mo:{0}, shortcode:{1}",
                        new Object[]{srvc_id_mo, srvc_id});
                throw new Exception("Service not found!!");
            }

            if ((se[0].srvc_type & SERVICE_TYPE.USSDDIRECTREPLY.getId()) == 0) {
                log.info("this service isn't USSD direct reply, use default message.");
                object = DEF_MSG;
            } else {
                if (se[0].msg_sub_nm == null || se[0].msg_sub_nm.isEmpty()) {
                    log.info("msg_sub_nm hasn't been set, use default message.");
                    object = DEF_MSG;
                } else {
                    object = se[0].msg_sub_nm;
                }
            }

            // Add to hashtable
            if (ussdReplyMessageFactory.size() > (MAX_POOL_SIZE * FACTOR)) {
                removeOlder(MAX_POOL_SIZE / 2);
            }
            if (ussdReplyMessageFactory.containsKey(hashcode)) {
                ussdReplyMessageFactory.remove(hashcode);
            }
            ussdReplyMessageFactory.put(hashcode, object);
        }
        return object;
    }

    public static void removeOlder(int count) {
        // remove an oldest object
        Vector v = new Vector(ussdReplyMessageFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        int deleted = 0;
        while (e.hasMoreElements()) {
            ussdReplyMessageFactory.remove(e);
            if ((++deleted) >= count) {
                break;
            }
        }
    }

    public static void remove() {
        // remove an oldest object
        Vector v = new Vector(ussdReplyMessageFactory.keySet());
        Collections.sort(v);
        Enumeration e = v.elements();
        if (e.hasMoreElements()) {
            ussdReplyMessageFactory.remove(e);
        }
    }
}
