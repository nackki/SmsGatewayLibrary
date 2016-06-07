/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.portal;

import com.ais.legacy.dlvrmsg.response.XML;
import com.dtac.cpa.response.CpaResponse;
import com.hippoping.SubscriptionPortal.request.MsisdnElement;
import com.hippoping.SubscriptionPortal.response.SubscriberElement;
import com.truemove.css.mo.response.RsrDetailElement;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_STATUS;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_TYPE;
import hippoping.smsgw.api.db.Subscriber;
import hippoping.smsgw.api.db.SubscriberGroup.SUB_STATUS;
import hippoping.smsgw.api.subscription.AisLegacySubscriptionService;
import hippoping.smsgw.api.subscription.DtacSubscriptionService;
import hippoping.smsgw.api.subscription.TruemoveSubscriptionService;

/**
 *
 * @author ITZONE
 */
public class SubscriptionPortal {

    public SubscriberElement doSub(MsisdnElement me, int srvc_main_id) throws Exception {
        SubscriberElement reply = new SubscriberElement();

        try {
            // update response
            reply.setMsisdn(new com.hippoping.SubscriptionPortal.response.MsisdnElement());
            reply.getMsisdn().setValue(me.getValue());
            reply.getMsisdn().setOper(me.getOper());

            CARRIER oper = CARRIER.valueOf(me.getOper());
            if (oper == CARRIER.AIS) {
                oper = CARRIER.AIS_LEGACY;
            }

            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(),
                    SERVICE_TYPE.SUBSCRIPTION.getId(), SERVICE_STATUS.ON.getId()); // allow only production subscription service

            if (se.srvc_id == null) {
                throw new Exception("service not found");
            }

            // verify can control sub
            if (!se.isAble2ManageSub()) {
                throw new Exception("service doesn't allow for control sub");
            }

            switch (oper) {
                case AIS_LEGACY:
                    XML xml = new AisLegacySubscriptionService().sendRegister(se, me.getValue());
                    reply.setStatus(xml.getSTATUS());
                    reply.setDetail(xml.getDETAIL());
                    break;
                case DTAC:
                    CpaResponse cpaResponse = new DtacSubscriptionService().sendRegister(se, me.getValue());
                    reply.setStatus(cpaResponse.getStatus().intValue() == 200 ? "OK" : "ERROR");
                    reply.setDetail(cpaResponse.getStatusDescription());
                    break;
                case TRUE:
                    RsrDetailElement rsr = new TruemoveSubscriptionService().sendRegister(se, me.getValue());
                    reply.setStatus(rsr.getCode().intValue() == 0 ? "OK" : "ERROR");
                    reply.setDetail(rsr.getDescription());
                    break;
                default:
                    throw new Exception("operator not supported");
            }

            // get subscriber information
            Subscriber subscriber = new Subscriber(me.getValue(), se.srvc_main_id, oper);
            reply.setSubDate(subscriber.getRegister_date("yyyyMMdd"));
            reply.setExpiredDate(subscriber.getExpired_date("yyyyMMdd"));
            reply.setBalancedDate(subscriber.getBalanced_date("yyyyMMdd"));
            reply.setUnsubDate(subscriber.getUnregister_date("yyyyMMdd"));

        } catch (Exception e) {
            reply.setStatus("ERROR");
            reply.setDetail(e.getMessage());
        }

        return reply;
    }

    public SubscriberElement doUnsub(MsisdnElement me, int srvc_main_id) throws Exception {
        SubscriberElement reply = new SubscriberElement();

        try {
            // update response
            reply.setMsisdn(new com.hippoping.SubscriptionPortal.response.MsisdnElement());
            reply.getMsisdn().setValue(me.getValue());
            reply.getMsisdn().setOper(me.getOper());

            // verify can control sub
            CARRIER oper = CARRIER.valueOf(me.getOper());
            if (oper == CARRIER.AIS) {
                oper = CARRIER.AIS_LEGACY;
            }

            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(),
                    SERVICE_TYPE.SUBSCRIPTION.getId(), SERVICE_STATUS.ON.getId()); // allow only production subscription service

            if (se.srvc_id == null) {
                throw new Exception("service not found");
            }

            if (!se.isAble2ManageSub()) {
                throw new Exception("service doesn't allowed for control sub");
            }

            switch (oper) {
                case AIS_LEGACY:
                    XML xml = new AisLegacySubscriptionService().sendUnregister(se, me.getValue());
                    reply.setStatus(xml.getSTATUS());
                    reply.setDetail(xml.getDETAIL());
                    break;
                case DTAC:
                    CpaResponse cpaResponse = new DtacSubscriptionService().sendUnregister(se, me.getValue());
                    reply.setStatus(cpaResponse.getStatus().intValue() == 200 ? "OK" : "ERROR");
                    reply.setDetail(cpaResponse.getStatusDescription());
                    break;
                case TRUE:
                    RsrDetailElement rsr = new TruemoveSubscriptionService().sendUnregister(se, me.getValue());
                    reply.setStatus(rsr.getCode().intValue() == 0 ? "OK" : "ERROR");
                    reply.setDetail(rsr.getDescription());
                    break;
                default:
                    reply.setStatus("ERROR");
                    reply.setDetail("operator not supported");
            }

            // get subscriber information
            Subscriber subscriber = new Subscriber(me.getValue(), se.srvc_main_id, oper);
            reply.setSubDate(subscriber.getRegister_date("yyyyMMdd"));
            reply.setExpiredDate(subscriber.getExpired_date("yyyyMMdd"));
            reply.setBalancedDate(subscriber.getBalanced_date("yyyyMMdd"));
            reply.setUnsubDate(subscriber.getUnregister_date("yyyyMMdd"));

        } catch (Exception e) {
            reply.setStatus("ERROR");
            reply.setDetail(e.getMessage());
        }

        return reply;
    }

    public SubscriberElement doQuery(MsisdnElement me, int srvc_main_id) throws Exception {
        SubscriberElement reply = new SubscriberElement();

        try {
            // update response
            reply.setMsisdn(new com.hippoping.SubscriptionPortal.response.MsisdnElement());
            reply.getMsisdn().setValue(me.getValue());
            reply.getMsisdn().setOper(me.getOper());

            // verify can control sub
            CARRIER oper = CARRIER.valueOf(me.getOper());
            oper = CARRIER.AIS_LEGACY;

            ServiceElement se = new ServiceElement(srvc_main_id, oper.getId(),
                    SERVICE_TYPE.SUBSCRIPTION.getId(), SERVICE_STATUS.ON.getId()); // allow only production subscription service

            if (se.srvc_id == null) {
                throw new Exception("service not found");
            }

            // get subscriber information
            Subscriber subscriber = new Subscriber(me.getValue(), se.srvc_main_id, oper);

            reply.setStatus(subscriber.getState()==SUB_STATUS.REGISTER.getId()?"ACTIVE":"EXPIRED");
            reply.setDetail("");
            reply.setSubDate(subscriber.getRegister_date("yyyyMMdd"));
            reply.setExpiredDate(subscriber.getExpired_date("yyyyMMdd"));
            reply.setBalancedDate(subscriber.getBalanced_date("yyyyMMdd"));
            reply.setUnsubDate(subscriber.getUnregister_date("yyyyMMdd"));

        } catch (Exception e) {
            e.printStackTrace();
            reply.setStatus("ERROR");
            reply.setDetail(e.getMessage());
        }

        return reply;
    }
}
