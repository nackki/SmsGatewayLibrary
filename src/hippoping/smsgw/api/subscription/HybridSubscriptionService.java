package hippoping.smsgw.api.subscription;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.SubscriptionServices;
import hippoping.smsgw.api.hybrid.HybridwsdlStub;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HybridSubscriptionService {

    private static final Logger log = Logger.getLogger(HybridSubscriptionService.class.getClass().getName());
    private final StringBuffer buff = new StringBuffer();

    private void print() {
        log.log(Level.INFO, this.buff.toString());
    }

    private void print(HybridwsdlStub.HybridMO hbMO, String url) {
        this.buff.append("Send Hybrid WSDL[").append(url).append("],")
                .append(hbMO.getUsername()).append("|")
                .append(hbMO.getPassword()).append("|")
                .append(hbMO.getServiceNumber()).append("|")
                .append(hbMO.getContentID()).append("|")
                .append(hbMO.getSgwid()).append("|")
                .append(hbMO.getMsisdn()).append("|");
    }

    private void print(HybridwsdlStub.HybridRSP rsp) {
        this.buff.append(rsp.getSID()).append("|")
                .append(rsp.getSTATUS());
    }

    public HybridwsdlStub.SendMOResponse sendRegister(ServiceElement se, String msisdn) throws Exception {
        HybridwsdlStub.SendMOResponse rsp = null;
        try {
            OperConfig oc = se.status == ServiceElement.SERVICE_STATUS.ON.getDbId() ? se.oper_config : se.oper_config_test;
            if (oc.hybrid.getName() == null) {
                throw new Exception("Hybrid config link not found!!");
            }
            HybridwsdlStub.HybridMO hbMO = new HybridwsdlStub.HybridMO();
            hbMO.setUsername(oc.hybrid.getUser());
            hbMO.setPassword(oc.hybrid.getPassword());

            /**
             * always use thrd_prty_register/thrd_prty_unregister as register
             * command
             *
             * if ((se.srvc_type & SERVICE_TYPE.CPVALIDATE.getId()) > 0) {
             * hbMO.setServiceNumber(se.thrd_prty_register);
             * hbMO.setContentID(se.thrd_prty_register); } else {
             * hbMO.setServiceNumber(se.ivr_register);
             * hbMO.setContentID(se.ivr_register); }
             *
             */
            hbMO.setServiceNumber(se.thrd_prty_register);
            hbMO.setContentID(se.thrd_prty_register);
            hbMO.setSgwid(oc.hybrid.getSgwid().getId());
            hbMO.setMsisdn(msisdn);

            print(hbMO, oc.hybrid.getUrl());

            HybridwsdlStub.SendMO sendMO = new HybridwsdlStub.SendMO();
            sendMO.setSn(hbMO);

            HybridwsdlStub stub = new HybridwsdlStub(oc.hybrid.getUrl());
            rsp = stub.sendMO(sendMO);

            print(rsp.get_return());
        } catch (Exception e) {
            throw e;
        } finally {
            print();
        }

        return rsp;
    }

    public HybridwsdlStub.SendMOResponse sendUnregister(ServiceElement se, String msisdn) throws Exception {
        HybridwsdlStub.SendMOResponse rsp = null;
        log.log(Level.INFO, "Unregister Hybrid called, unsubscript -> {0}", msisdn);
        try {
            OperConfig oc = se.status == ServiceElement.SERVICE_STATUS.ON.getDbId() ? se.oper_config : se.oper_config_test;
            if (oc.hybrid.getName() == null) {
                throw new Exception("Hybrid config link not found!!");
            }
            HybridwsdlStub.HybridMO hbMO = new HybridwsdlStub.HybridMO();
            hbMO.setUsername(oc.hybrid.getUser());
            hbMO.setPassword(oc.hybrid.getPassword());
            /**
             * always use thrd_prty_register/thrd_prty_unregister as unregister
             * command if ((se.srvc_type & SERVICE_TYPE.CPVALIDATE.getId()) > 0)
             * { hbMO.setServiceNumber(se.thrd_prty_unregister);
             * hbMO.setContentID(se.thrd_prty_unregister); } else {
             * hbMO.setServiceNumber(se.ivr_unregister);
             * hbMO.setContentID(se.ivr_unregister); }
            *
             */
            hbMO.setServiceNumber(se.thrd_prty_unregister);
            hbMO.setContentID(se.thrd_prty_unregister);
            hbMO.setSgwid(oc.hybrid.getSgwid().getId());
            hbMO.setMsisdn(msisdn);

            print(hbMO, oc.hybrid.getUrl());

            HybridwsdlStub.SendMO sendMO = new HybridwsdlStub.SendMO();
            sendMO.setSn(hbMO);

            HybridwsdlStub stub = new HybridwsdlStub(oc.hybrid.getUrl());

            rsp = stub.sendMO(sendMO);

            print(rsp.get_return());

            if ((rsp.get_return() != null) && (rsp.get_return().getSTATUS().matches("(?i)SUCCESS"))) {
                new SubscriptionServices().doUnsub(msisdn, se.srvc_main_id, OperConfig.CARRIER.AIS);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            print();
        }

        return rsp;
    }
}