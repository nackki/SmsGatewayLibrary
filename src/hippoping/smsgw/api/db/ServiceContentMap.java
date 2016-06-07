/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import com.dtac.sdp.common.Originate;
import com.dtac.sdp.subscription.status.reply.SubscriptionStatus;
import com.truemove.css.thirdparty.response.Response;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.RxQueue.RX_TYPE;
import hippoping.smsgw.api.db.SubscriptionServices.SUB_RESULT;
import hippoping.smsgw.api.db.report.SummaryReport;
import hippoping.smsgw.api.operator.cat_cpg.CatSubscriber;
import hippoping.smsgw.api.operator.dtac_sdp.SubscriberSynchronize;
import hippoping.smsgw.api.operator.dtac_sdp.XMLSupplierRemote;
import hippoping.smsgw.api.operator.true_css.CssDispatcherBeanRemote;
import hippoping.smsgw.api.portal.ServicePortal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.StringConvert;

/**
 *
 * @author nacks_mcair
 */
public class ServiceContentMap {

    private static final Logger log = Logger.getLogger(ServiceContentMap.class.getName());

    protected ServiceElement se;
    protected ServiceContentAction sca;
    protected DroConfigure dro;
    protected SCM_TYPE scm_type;
    // Keying properties
    private String srvc_id_mo;
    private String keyword;
    private RX_TYPE kw_type;

    @Override
    public int hashCode() {
        return genHash(this.srvc_id_mo, CARRIER.fromId(se.oper_id), kw_type, keyword);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof ServiceContentMap)) {
            ServiceContentMap scm = (ServiceContentMap) obj;

            return hashCode() == scm.hashCode();
        }
        return false;
    }

    public static int genHash(String srvc_id_mo, CARRIER oper, RX_TYPE kw_type, String keyword) {
        int hash = (srvc_id_mo + oper.name() + kw_type.name() + keyword).hashCode();
        try {
            hash = StringConvert.computeHash(srvc_id_mo + oper.name() + kw_type.name() + keyword);
        } catch (NoSuchAlgorithmException e) {
            log.log(Level.SEVERE, "hashcode function error!!", e);
        }
        return hash;
    }

    public ServiceContentMap() {

    }

    public ServiceContentMap(final String srvc_id_mo, final RX_TYPE kw_type, final String keyword,
            final ServiceElement se, final ServiceContentAction sca, final DroConfigure dro, final SCM_TYPE scm_type) {
        // find related service
        // create SCA (in case of PULL with fixed message / Forward)
        // get DRO configure
        this.srvc_id_mo = srvc_id_mo;
        this.kw_type = kw_type;
        this.keyword = keyword;
        this.se = se;
        this.sca = sca;
        this.dro = dro;
        this.scm_type = scm_type;
    }

    /**
     * doAction() -- simply do the cached buffer action.
     *
     * @param msisdn
     * @param rx_id
     * @param ivr_resp_id
     * @param bean
     */
    public void doAction(String msisdn, long rx_id, String ivr_resp_id, Object bean) {
        doAction(msisdn, rx_id, ivr_resp_id, bean, null);
    }

    /**
     * doAction() -- especially for DTAC which have to referred
     * Session-ID(txId).
     *
     * @param msisdn
     * @param rx_id
     * @param ivr_resp_id
     * @param bean
     * @param ref_txid
     */
    public void doAction(String msisdn, long rx_id, String ivr_resp_id, Object bean, String ref_txid) {
        try {
            RxQueue rxq = new RxQueue(rx_id);

            SubscriptionServices.SUB_RESULT ret;
            int ctnt_id;
            ServiceContentAction reply_action;

            if (scm_type == SCM_TYPE.PULL || scm_type == SCM_TYPE.FORWARD) {
                String content_type = null;
                String content_id = null;
                // insert table trns_sms_download
                TxSmsDownload.add(msisdn, keyword, sca.getSrvc_main_id(), se.oper_id, rx_id);
                // update extracted MO record to database //////////////////////
                RxMoQueue.add(rx_id, sca.getSrvc_main_id(), msisdn, content_type, content_id, keyword);
                // update report in case of MO charge
                if (se.chrg_flg.equals("MO")) {
                    SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(se.oper_id), new Date(), "mt_chrg_total", 1);
                    SummaryReport.updateSummary(se.srvc_main_id, CARRIER.fromId(se.oper_id), new Date(), "mt_chrg_balance", 1);
                }
            }

            Originate.TYPE originateType = Originate.TYPE.SMS;
            if (scm_type == SCM_TYPE.SUB || scm_type == SCM_TYPE.UNSUB) {
                switch (kw_type) {
                    case IVR:
                        originateType = Originate.TYPE.IVR;
                        break;
                    case USSD:
                        originateType = Originate.TYPE.USSD;
                        break;
                }
            }

            switch (scm_type) {
                case SUB:
                    log.log(Level.INFO, "srvc_main_id[{0}] MO register", se.srvc_main_id);

                    rxq.addType(RX_TYPE.SUB);

                    boolean need_css_sub = false;
                    boolean isok_css_sub = false;

                    // inform operator
                    if ((se.srvc_type & ServiceElement.SERVICE_TYPE.NOCSS.getId()) == 0) {
                        need_css_sub = true;
                        switch (CARRIER.fromId(se.oper_id)) {
                            case DTAC:
                            case DTAC_SDP:
                                if (bean != null && bean instanceof XMLSupplierRemote) {
                                    XMLSupplierRemote xMLSupplierBean = (XMLSupplierRemote) bean;

                                    Subscriber[] subs = {new Subscriber(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id))};
                                    SubscriptionStatus subscriptionStatus = null;
                                    List<SubscriptionStatus> subStatusList
                                            = xMLSupplierBean.sendRegister(se, subs, ref_txid, String.valueOf(originateType.getId()));
                                    if (subStatusList != null && subStatusList.size() > 0) {
                                        subscriptionStatus = subStatusList.get(0);
                                    }

                                    if (subscriptionStatus != null) {
                                        if (subscriptionStatus.getStatus().equals("A")
                                                || subscriptionStatus.getStatus().equals("P")) {
                                            rxq.addType(RX_TYPE.CSS_OK);
                                        }
                                        if (subscriptionStatus.getStatus().equals("A")
                                                || subscriptionStatus.getStatus().equals("P")
                                                || (subscriptionStatus.getStatus().equals("N")
                                                && subscriptionStatus.getStatusCode().equals("665"))) {
                                            isok_css_sub = true;
                                        }
                                        new SubscriberSynchronize().updateProfile(subscriptionStatus, se.srvc_main_id,
                                                (subscriptionStatus.getStatus().equals("A")
                                                || subscriptionStatus.getStatus().equals("P")
                                                || (subscriptionStatus.getStatus().equals("N")
                                                && subscriptionStatus.getStatusCode().equals("665"))) // already registered
                                                ? SubscriberGroup.SUB_STATUS.REGISTER
                                                : SubscriberGroup.SUB_STATUS.UNREGISTER);
                                    } else {
                                        new SubscriptionServices().doUnsub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));
                                    }

                                }

                                break;

                            case TRUE:
                            case TRUEH:
                                try {
                                    if (bean != null && bean instanceof CssDispatcherBeanRemote) {
                                        CssDispatcherBeanRemote cssDispatcherBean = (CssDispatcherBeanRemote) bean;

                                        log.log(Level.INFO, "Call CSS Dispatcher|{0}|{1}|{2}|{3}",
                                                new Object[]{se.srvc_main_id, msisdn, se.thrd_prty_register, CARRIER.TRUEH.name()});
                                        Response response = cssDispatcherBean.sendPacket(se.srvc_main_id, msisdn, se.thrd_prty_register, CARRIER.TRUEH);

                                        if (response != null && response.getBody().getStatus().equals("0")) {
                                            rxq.addType(RX_TYPE.CSS_OK);
                                        }

                                        if (response != null && (response.getBody().getStatus().equals("0")
                                                || response.getBody().getStatus().equals("8"))) {
                                            isok_css_sub = true;
                                        }

                                        if (response != null && !response.getBody().getStatus().equals("0")
                                                && !response.getBody().getStatus().equals("8")) { // subscription already exist
                                            log.log(Level.SEVERE, response.getBody().getDescription());
                                        }
                                    }
                                } catch (Exception e) {
                                    log.log(Level.SEVERE, "Truemove CSS registration process failed[{0}]", e.getMessage());
                                }

                                break;
                        }
                    }

                    Subscriber subscriber = null;
                    if (!need_css_sub || (need_css_sub && isok_css_sub)) {
                        ret = new SubscriptionServices().doSub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));
                        log.log(Level.INFO, "sub result:{0}", ret.toString());

                        // get subscriber
                        subscriber = new Subscriber(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));
                    } else {
                        ret = SUB_RESULT.INVALID;
                    }

                    if (ret != SUB_RESULT.INVALID && ret != SUB_RESULT.DUPLICATED) {
                        rxq.addType(RX_TYPE.LOCAL_OK);
                    }

                    // reply THK if it sets
                    reply_action = new ServiceContentAction();
                    reply_action.setAction_type(ServiceContentAction.ACTION_TYPE.SMS);
                    reply_action.setOper(CARRIER.fromId(se.oper_id));
                    reply_action.setSrvc_main_id(se.srvc_main_id);

                    // send register success message to subscriber
                    String msg;
                    try {
                        switch (CARRIER.fromId(se.oper_id)) {
                            case DTAC:
                            case DTAC_SDP:
                                switch (ret) {
                                    case NEW_TRIAL: // free
                                    case RENEW_TRIAL:  // free
                                        if (se.msg_sub_ft != null && se.msg_sub_ft.isEmpty()) {
                                            msg = subscriber.fillVariables(se.msg_sub_ft);
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, "MO", rx_id);
                                        }
                                        break;
                                    case NEW: // MT charge
                                    case RENEW:  // MT charge
                                        if (se.msg_sub_nm != null && se.msg_sub_nm.isEmpty()) {
                                            msg = subscriber.fillVariables(se.msg_sub_nm);
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, (se.isNormalSubscription()) ? "MT" : "MO", rx_id);

                                        }
                                        break;
                                    case DUPLICATED:  // free
                                        if (se.msg_err_dup != null && se.msg_err_dup.isEmpty()) {
                                            msg = se.msg_err_dup;
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, "MO", dro.getError(), rx_id, ref_txid);
                                        }
                                        break;
                                    case INVALID:  // free
                                        if (se.msg_err_no_srvc != null && se.msg_err_no_srvc.isEmpty()) {
                                            msg = se.msg_err_no_srvc;
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, "MO", dro.getError(), rx_id, ref_txid);
                                        }
                                        break;
                                }

                                break;

                            case TRUE:
                            case TRUEH:
                                switch (ret) {
                                    case NEW_TRIAL: // free
                                        msg = subscriber.fillVariables(se.msg_sub_ft);
                                        ctnt_id = new MessageSms().add(msg, 1);
                                        reply_action.setContentId(ctnt_id);
                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", ivr_resp_id, dro.getSub_ft(), rx_id);
                                        break;
                                    case NEW: // MT charge
                                        msg = subscriber.fillVariables(se.msg_sub_nm);
                                        ctnt_id = new MessageSms().add(msg, 1);
                                        reply_action.setContentId(ctnt_id);
                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, (se.isNormalSubscription()) ? "MT" : "MO", ivr_resp_id, dro.getSub_nm(), rx_id);
                                        break;
                                    case DUPLICATED:  // free
                                        msg = se.msg_err_dup;
                                        ctnt_id = new MessageSms().add(msg, 1);
                                        reply_action.setContentId(ctnt_id);
                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", ivr_resp_id, dro.getSub_dup(), rx_id);
                                        break;
                                    case RENEW:  // MT charge
                                        msg = subscriber.fillVariables(se.msg_sub_nm);
                                        ctnt_id = new MessageSms().add(msg, 1);
                                        reply_action.setContentId(ctnt_id);
                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, (se.isNormalSubscription()) ? "MT" : "MO", ivr_resp_id, dro.getSub_renew_nm(), rx_id);
                                        break;
                                    case RENEW_TRIAL:  // free
                                        msg = subscriber.fillVariables(se.msg_sub_ft);
                                        ctnt_id = new MessageSms().add(msg, 1);
                                        reply_action.setContentId(ctnt_id);
                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", ivr_resp_id, dro.getSub_renew_ft(), rx_id);
                                        break;
                                    case INVALID:  // free
                                        msg = se.msg_err_no_srvc;
                                        ctnt_id = new MessageSms().add(msg, 1);
                                        reply_action.setContentId(ctnt_id);
                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", ivr_resp_id, dro.getSub_dup(), rx_id);
                                        break;
                                }
                                break;

                            case CAT:
                                // set ref_txid -> mmbr_cat.extr_id
                                CatSubscriber catsub = new CatSubscriber(subscriber.msisdn, subscriber.srvc_main_id, CARRIER.CAT);
                                catsub.setExtr_id(ref_txid);
                                log.log(Level.INFO, "update ref_txid|{0}|{1}|{2}|{3} row updated.",
                                        new Object[]{subscriber.msisdn, subscriber.srvc_main_id, ref_txid, catsub.syncExtrId()});

                                switch (ret) {
                                    case NEW_TRIAL: // free
                                    case RENEW_TRIAL:  // free
                                        if (se.msg_sub_ft != null && se.msg_sub_ft.isEmpty()) {
                                            msg = subscriber.fillVariables(se.msg_sub_ft);
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, "MO",
                                                    dro.getSub_renew_ft(), rx_id, ref_txid);
                                        }
                                        break;
                                    case NEW: // MT charge
                                    case RENEW:  // MT charge
                                        if (se.msg_sub_nm != null && se.msg_sub_nm.isEmpty()) {
                                            msg = subscriber.fillVariables(se.msg_sub_nm);
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, (se.isNormalSubscription()) ? "MT" : "MO",
                                                    dro.getSub_renew_nm(), rx_id, ref_txid);

                                        }
                                        break;
                                    case DUPLICATED:  // free
                                        if (se.msg_err_dup != null && se.msg_err_dup.isEmpty()) {
                                            msg = se.msg_err_dup;
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, "MO",
                                                    dro.getError(), rx_id, ref_txid);
                                        }
                                        break;
                                    case INVALID:  // free
                                        if (se.msg_err_no_srvc != null && se.msg_err_no_srvc.isEmpty()) {
                                            msg = se.msg_err_no_srvc;
                                            ctnt_id = new MessageSms().add(msg, 1);
                                            reply_action.setContentId(ctnt_id);
                                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn, "MO",
                                                    dro.getError(), rx_id, ref_txid);
                                        }
                                        break;
                                }

                                break;
                        }
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Register process warning!!, {0}", e);
                    }

                    break;
                case UNSUB:
                    log.log(Level.INFO, "srvc_main_id[{0}] MO unregister", se.srvc_main_id);
                    rxq.addType(RX_TYPE.UNSUB);

                    try {
                        switch (CARRIER.fromId(se.oper_id)) {
                            case TRUE:
                            case TRUEH:

                                ret = new SubscriptionServices().doUnsub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));

                                // send free MT to customer
                                switch (ret) {
                                    case SUCCESS:
                                        rxq.addType(RX_TYPE.LOCAL_OK);

                                        // send unregister message to Truemove 3rd party
                                        if ((SubscriberFactory.getAmountOfServicesSubscribe(se.srvc_main_id, msisdn, CARRIER.fromId(se.oper_id)) == 0) // To prevent the CSS unregister when the MSISDN has more than 1 services activated subscribe on the same service id e.g. 4847833
                                                && ((se.srvc_type & ServiceElement.SERVICE_TYPE.NOCSS.getId()) == 0) // To prevent the CSS unregister when it has no CSS subscription
                                                ) {
                                            try {
                                                if (bean != null && bean instanceof CssDispatcherBeanRemote) {
                                                    CssDispatcherBeanRemote cssDispatcherBean = (CssDispatcherBeanRemote) bean;
                                                    log.log(Level.INFO, "Call CSS Dispatcher|{0}|{1}|{2}|{3}",
                                                            new Object[]{se.srvc_main_id, msisdn, se.thrd_prty_unregister, CARRIER.fromId(se.oper_id).name()});
                                                    Response response = cssDispatcherBean.sendPacket(se.srvc_main_id, msisdn,
                                                            se.thrd_prty_unregister, CARRIER.fromId(se.oper_id));

                                                    if (response != null && response.getBody().getStatus().equals("0")) {
                                                        rxq.addType(RX_TYPE.CSS_OK);
                                                    }

                                                    if (response != null && !response.getBody().getStatus().equals("0")
                                                            && !response.getBody().getStatus().equals("9")) { //Subscription doesn't exist
                                                        log.log(Level.WARNING, response.getBody().getDescription());
                                                    }
                                                }
                                            } catch (Exception e) {
                                                log.log(Level.SEVERE, "Truemove CSS registration process failed[{0}]", e);
                                            }
                                        }

                                        if (se.free_trial > 0) { // free
                                            ctnt_id = new MessageSms().add(se.msg_usub_ft, 1);
                                        } else { // free
                                            ctnt_id = new MessageSms().add(se.msg_usub_nm, 1);
                                        }

                                        reply_action = new ServiceContentAction();
                                        reply_action.setAction_type(ServiceContentAction.ACTION_TYPE.SMS);
                                        reply_action.setOper(CARRIER.fromId(se.oper_id));
                                        reply_action.setContentId(ctnt_id);
                                        reply_action.setSrvc_main_id(se.srvc_main_id);

                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", ivr_resp_id, dro.getUnregister(), rx_id);

                                        break; // break of result SUCCESS
                                    case INVALID:
                                        // invalid MSISDN
                                        log.log(Level.INFO, "no MSISDN found!!");

                                        break; // break of result INVALID
                                }
                                break; // break of OPER

                            case DTAC:
                            case DTAC_SDP:

                                SubscriptionStatus subscriptionStatus = null;

                                String[] msisdns = {msisdn};

                                // Post cpa-unregister
                                // Fake subscription service
                                ret = SubscriptionServices.SUB_RESULT.INVALID;
                                if ((se.srvc_type & ServiceElement.SERVICE_TYPE.NOCSS.getId()) > 0) {
                                    ret = new SubscriptionServices().doUnsub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));
                                    rxq.addType(RX_TYPE.LOCAL_OK);
                                } else {

                                    if (bean != null && bean instanceof XMLSupplierRemote) {
                                        XMLSupplierRemote xMLSupplierBean = (XMLSupplierRemote) bean;

                                        List<SubscriptionStatus> subStatusList = xMLSupplierBean.sendUnregister(se, msisdns, ref_txid, String.valueOf(originateType.getId()));
                                        if (subStatusList != null && subStatusList.size() > 0) {
                                            subscriptionStatus = subStatusList.get(0);
                                        }

                                        if (subscriptionStatus != null) { // unregister success on DTAC site
                                            ret = SubscriptionServices.SUB_RESULT.SUCCESS;

                                            if (subscriptionStatus.getStatusCode().equals("200")) {
                                                rxq.addType(RX_TYPE.CSS_OK);
                                            }

                                            if (subscriptionStatus.getStatusCode().equals("200")
                                                    || (subscriptionStatus.getStatus().equals("N") && subscriptionStatus.getStatusCode().equals("666")) // Subscriber not found
                                                    ) {
                                                // Validate Local member
                                                try {
                                                    subscriber = new Subscriber(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));

                                                    // update profile if found it on local
                                                    new SubscriberSynchronize().updateProfile(subscriptionStatus, se.srvc_main_id, SubscriberGroup.SUB_STATUS.UNREGISTER);
                                                    rxq.addType(RX_TYPE.LOCAL_OK);
                                                } catch (Exception e) {
                                                    // Insert new subscriber
                                                    //log.log(Level.INFO, "call doSub() for {0}, return {1}",
                                                    //      new Object[]{msisdn, (new SubscriptionServices().doSub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id))).toString()});
                                                }

                                            }
                                        }
                                    }
                                }

                                // response the THANKYOU back to customer if the message is set
                                // send free MT to customer
                                ctnt_id = new MessageSms().add(((ret == SubscriptionServices.SUB_RESULT.INVALID) ? se.msg_err_no_srvc : ((se.free_trial > 0) ? se.msg_usub_ft : se.msg_usub_nm)), 1);

                                if (ctnt_id > 0) {
                                    reply_action = new ServiceContentAction();
                                    reply_action.setAction_type(ServiceContentAction.ACTION_TYPE.SMS);
                                    reply_action.setOper(CARRIER.fromId(se.oper_id));
                                    reply_action.setContentId(ctnt_id);
                                    reply_action.setSrvc_main_id(se.srvc_main_id);

                                    new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", rx_id);
                                }

                                break;

                            case CAT:

                                ret = new SubscriptionServices().doUnsub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));

                                // send free MT to customer
                                switch (ret) {
                                    case SUCCESS:
                                        rxq.addType(RX_TYPE.LOCAL_OK);
                                        if (se.free_trial > 0) { // free
                                            ctnt_id = new MessageSms().add(se.msg_usub_ft, 1);
                                        } else { // free
                                            ctnt_id = new MessageSms().add(se.msg_usub_nm, 1);
                                        }

                                        reply_action = new ServiceContentAction();
                                        reply_action.setAction_type(ServiceContentAction.ACTION_TYPE.SMS);
                                        reply_action.setOper(CARRIER.fromId(se.oper_id));
                                        reply_action.setContentId(ctnt_id);
                                        reply_action.setSrvc_main_id(se.srvc_main_id);

                                        new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn, "MO", dro.getUnregister(), rx_id, ref_txid);

                                        break; // break of result SUCCESS
                                    case INVALID:
                                        // invalid MSISDN
                                        log.log(Level.INFO, "no MSISDN found!!");

                                        break; // break of result INVALID
                                }
                                break; // break of OPER
                        }

                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Unregister process error:{0}", e);
                    }
                    break;
                case PULL:
                    if (sca.isFixedMessage()) {

                        switch (CARRIER.fromId(se.oper_id)) {
                            case TRUE:
                            case TRUEH:
                                new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn,
                                        (sca.getChrg_flg() == null) ? se.chrg_flg : sca.getChrg_flg(),
                                        ivr_resp_id, dro.getPullsms(), rx_id);

                                break;

                            case DTAC:
                            case DTAC_SDP:
                            case CAT:
                                new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn,
                                        (sca.getChrg_flg() == null) ? se.chrg_flg : sca.getChrg_flg(),
                                        dro.getPullsms(), rx_id, ref_txid);

                                break;
                        }
                    } else {
                        String ivr = (kw_type == RX_TYPE.IVR) ? keyword : null;
                        String sms = (kw_type == RX_TYPE.SMS || kw_type == RX_TYPE.USSD) ? keyword : null;

                        // update new SMS text
                        reply_action = new ServiceContentAction(se.srvc_main_id, ivr, sms, null, se.oper_id);

                        switch (CARRIER.fromId(se.oper_id)) {
                            case TRUE:
                            case TRUEH:
                                new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn,
                                        (reply_action.getChrg_flg() == null) ? se.chrg_flg : reply_action.getChrg_flg(),
                                        ivr_resp_id, dro.getPullsms(), rx_id);

                                break;

                            case DTAC:
                            case DTAC_SDP:
                            case CAT:
                                new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn,
                                        (reply_action.getChrg_flg() == null) ? se.chrg_flg : reply_action.getChrg_flg(),
                                        dro.getPullsms(), rx_id, ref_txid);

                                break;
                        }
                    }
                    break;
                case FORWARD:
                    ret = new SubscriptionServices().doSub(msisdn, se.srvc_main_id, CARRIER.fromId(se.oper_id));
                    log.log(Level.INFO, "sub result:{0}", ret.toString());

                    // Forward MO
                    // insert queue into table trns_3rdp_mo
                    long id = Tx3rdPartyMo.add(msisdn, kw_type.name().toLowerCase(), keyword, se.srvc_main_id, CARRIER.fromId(se.oper_id), rx_id, sca.contentId);
                    Tx3rdPartyMo tx3rdPartyMo = new Tx3rdPartyMo(id);
                    ServicePortal servicePortal = new ServicePortal(tx3rdPartyMo);
                    reply_action = new ServiceContentAction();
                    reply_action.setOper(CARRIER.fromId(se.oper_id));
                    reply_action.setSrvc_main_id(se.srvc_main_id);

                    // reply message back to customer
                    switch (servicePortal.getStatus()) {
                        case SENT:
                            reply_action.setContentId(servicePortal.getCtnt_id());
                            reply_action.setAction_type(servicePortal.getCtnt_type());

                            switch (CARRIER.fromId(se.oper_id)) {
                                case TRUE:
                                case TRUEH:
                                    new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn,
                                            (reply_action.getChrg_flg() == null) ? se.chrg_flg : reply_action.getChrg_flg(),
                                            dro.getForward(), rx_id);

                                    break;

                                case DTAC:
                                case DTAC_SDP:
                                case CAT:
                                    new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn,
                                            (reply_action.getChrg_flg() == null) ? se.chrg_flg : reply_action.getChrg_flg(),
                                            dro.getForward(), rx_id, ref_txid);

                                    break;
                            }

                            break;

                        default:
                            reply_action.setContentId(new MessageSms().add(se.msg_err_no_srvc, 1));
                            reply_action.setAction_type(ServiceContentAction.ACTION_TYPE.SMS);

                            switch (CARRIER.fromId(se.oper_id)) {
                                case TRUE:
                                case TRUEH:
                                    new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn,
                                            "MO", ivr_resp_id, dro.getError(), rx_id);

                                    break;

                                case DTAC:
                                case DTAC_SDP:
                                case CAT:
                                    new TxQueue().add(reply_action, se.srvc_main_id, se.oper_id, msisdn,
                                            "MO", dro.getError(), rx_id, ref_txid);

                                    break;
                            }

                    }

                    break;
                case UNKNOWN:
                    switch (CARRIER.fromId(se.oper_id)) {
                        case TRUE:
                        case TRUEH:
                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn,
                                    "MO", ivr_resp_id, dro.getError(), rx_id);
                            break;

                        case DTAC:
                        case DTAC_SDP:
                        case CAT:
                            new TxQueue().add(sca, se.srvc_main_id, se.oper_id, msisdn,
                                    "MO", dro.getError(), rx_id, ref_txid);

                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught!!", e);
        }
    }

    public static enum SCM_TYPE {

        UNKNOWN(0),
        SUB(1),
        UNSUB(2),
        PULL(3),
        FORWARD(4);
        private final int id;

        private SCM_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static SCM_TYPE fromId(int id) {
            for (SCM_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }

    public ServiceElement getServiceElement() {
        return se;
    }

    public ServiceContentAction getSca() {
        return sca;
    }

    public SCM_TYPE getScm_type() {
        return scm_type;
    }

    public String getSrvc_id_mo() {
        return srvc_id_mo;
    }

    public String getKeyword() {
        return keyword;
    }

    public RX_TYPE getKw_type() {
        return kw_type;
    }

}
