package hippoping.smsgw.api.portal;

import com.hippoping.ServicePortal.request.NumberElement;
import hippoping.smsgw.api.db.MessageSms;
import hippoping.smsgw.api.db.MessageWap;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceContentAction;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.ThirdPartyConfig;
import hippoping.smsgw.api.db.Tx3rdPartyMo;
import hippoping.smsgw.api.db.TxQueue;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import lib.common.DatetimeUtil;
import lib.common.HttpClientConnection;
import lib.common.HttpClientConnectionPool;
import lib.common.HttpClientIndexing;
import lib.common.HttpPost;
import lib.common.StringConvert;
import lib.common.StringUtil;

public class ServicePortal {

    private static final Logger log = Logger.getLogger(ServicePortal.class.getClass().getName());
    protected int ctnt_id = -1;
    protected ServiceContentAction.ACTION_TYPE ctnt_type;
    protected TxQueue.TX_STATUS status;

    public TxQueue.TX_STATUS getStatus() {
        return this.status;
    }

    public int getCtnt_id() {
        return this.ctnt_id;
    }

    public ServiceContentAction.ACTION_TYPE getCtnt_type() {
        return this.ctnt_type;
    }

    public ServicePortal(Tx3rdPartyMo txq) {
        ThirdPartyConfig tpc = null;
        try {
            tpc = new ThirdPartyConfig(txq.getCtnt_3rdp_id());
        } catch (Exception e) {
        }
        if ((tpc == null) || (tpc.getMethod().matches("(?i)POST"))) {
            send(txq);
        } else {
            this.status = TxQueue.TX_STATUS.ERROR;

            Map map = new HashMap();
            map.put("srvcid", String.valueOf(txq.getSrvc_main_id()));
            map.put("operid", String.valueOf(txq.getOper().getId()));
            map.put("msisdn", txq.getMsisdn());

            String url = StringUtil.fillMessageVariables(map, tpc.getUrl());
            url = StringUtil.fillMessageSqlAnnotate(url);
            try {
                HttpClientConnectionPool pool = new HttpClientConnectionPool();

                HttpClientIndexing key = new HttpClientIndexing();
                key.setConnection("close");
                key.setUrl(url);
                key.setUserpswd(tpc.getUser(), tpc.getPassword());

                HttpClientConnection conn = pool.open(key);
                conn.setContenttype("application/x-www-form-urlencoded");
                conn.setTimeout(60000);
                conn.setMethod("GET");
                try {
                    OutputStream os = conn.getWriter();
                    Writer w = new OutputStreamWriter(os, "UTF-8");

                    w.close();

                    String resp = conn.getResponse();
                    this.status = TxQueue.TX_STATUS.SENT;
                } catch (Exception e) {
                    throw e;
                } finally {
                    conn.close();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Service portal error!!", e);
            } finally {
                try {
                    txq.setStatus(this.status);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "cannot update status to trns_3rd_mo record!!", e);
                }
            }
        }
    }

    private void readResponse(Tx3rdPartyMo txq, String resp, int uid) throws Exception {
        if (resp.trim().isEmpty()) {
            return;
        }

        try {
            JAXBContext jc = JAXBContext.newInstance("com.hippoping.ServicePortal.response");

            Unmarshaller u = jc.createUnmarshaller();

            com.hippoping.ServicePortal.response.MessagePortalElement response = (com.hippoping.ServicePortal.response.MessagePortalElement) u.unmarshal(new StreamSource(new StringReader(resp)));

            txq.updateResponse(response.getCode().intValue(), response.getDescription());
            switch (response.getMessageReply().getUd().getType()) {
                case "text":
                    this.ctnt_type = ServiceContentAction.ACTION_TYPE.SMS;
                    if (response.getMessageReply().getUd().getContent().trim().isEmpty()) {
                        this.ctnt_id = 0;
                        this.status = TxQueue.TX_STATUS.SENT;
                        return;
                    }
                    this.ctnt_id = new MessageSms().add(StringConvert.ASCII2Unicode(response.getMessageReply().getUd().getContent()), 1, uid);
                    break;
                case "wap":
                    jc = JAXBContext.newInstance("com.hippoping.ServicePortal.wap.response");
                    u = jc.createUnmarshaller();
                    com.hippoping.ServicePortal.wap.response.MessagePortalElement wap = (com.hippoping.ServicePortal.wap.response.MessagePortalElement) u.unmarshal(new StreamSource(new StringReader(resp)));
                    this.ctnt_type = ServiceContentAction.ACTION_TYPE.WAP;
                    if (wap != null) {
                        this.ctnt_id = new MessageWap().add(wap.getMessageReply().getUd().getTitle(), wap.getMessageReply().getUd().getUrl(), "", 1, uid);
                    }
                    break;
                case "sms":
                    this.ctnt_id = -1;
                    this.ctnt_type = ServiceContentAction.ACTION_TYPE.SMS;
                    break;
                case "mms":
                    this.ctnt_id = -1;
                    this.ctnt_type = ServiceContentAction.ACTION_TYPE.MMS;
                    break;
            }

            this.status = TxQueue.TX_STATUS.SENT;
        } catch (NullPointerException e) {
            throw new Exception("<UD> type is not valid!!");
        } catch (Exception e) {
            this.status = TxQueue.TX_STATUS.ERROR;
            throw e;
        } finally {
        }
    }

    public void send(Tx3rdPartyMo txq) {
        this.status = TxQueue.TX_STATUS.ERROR;
        String spXmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message-portal id=\"3104400\"><destination><address><number type=\"abbreviated\">1911470</number></address></destination><source><address><number type=\"international\">6691161131</number></address></source><ud type=\"text\" encoding=\"default\">Hello World</ud><timestamp>yyyyMMddHHmmss</timestamp><service-id>0101102156</service-id><operator>TRUEMOVE</operator></message-portal>";
        try {
            JAXBContext sp_jc = JAXBContext.newInstance("com.hippoping.ServicePortal.request");
            Unmarshaller sp_u = sp_jc.createUnmarshaller();
            Marshaller sp_m = sp_jc.createMarshaller();
            com.hippoping.ServicePortal.request.MessagePortalElement msgPortalElement = (com.hippoping.ServicePortal.request.MessagePortalElement) sp_u.unmarshal(new StreamSource(new StringReader(spXmlStr)));

            sp_m.setProperty("jaxb.formatted.output", Boolean.FALSE);

            msgPortalElement.setId(txq.getOper().toString() + "SMSGW:" + txq.getRx_id());
            msgPortalElement.getUd().setType(txq.getMesg_type());
            msgPortalElement.getUd().setContent(txq.getMesg());
            if ((txq.getMesg_type().equals("ussd")) || (txq.getMesg_type().equals("ivr"))) {
                msgPortalElement.getUd().setEncoding("default");
            } else if (txq.getMesg_type().equals("sms")) {
                String encoding = "default";
                if (!StringConvert.isEnglishText(txq.getMesg())) {
                    encoding = "TIS-620";
                }
                msgPortalElement.getUd().setEncoding(encoding);
            }
            ServiceElement se = new ServiceElement(txq.getSrvc_main_id(), txq.getOper().getId(), ServiceElement.SERVICE_TYPE.ALL.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

            NumberElement des_number = new NumberElement();
            des_number.setType("abbreviated");
            des_number.setValue(txq.getOper() == CARRIER.DTAC_SDP
                    && (se.thrd_prty_register != null && !se.thrd_prty_register.isEmpty())
                    ? se.thrd_prty_register : se.srvc_id);

            NumberElement src_number = new NumberElement();
            src_number.setType("international");
            src_number.setValue(txq.getMsisdn());

            msgPortalElement.getDestination().getAddress().getNumber().clear();
            msgPortalElement.getSource().getAddress().getNumber().clear();

            msgPortalElement.getDestination().getAddress().getNumber().add(des_number);
            msgPortalElement.getSource().getAddress().getNumber().add(src_number);
            msgPortalElement.setTimestamp(DatetimeUtil.print("yyyyMMddHHmmss", DatetimeUtil.toDate(txq.getIssue_dt())));
            msgPortalElement.setServiceId(se.srvc_id);
            msgPortalElement.setOperator(txq.getOper().toString());

            ThirdPartyConfig tpconf = new ThirdPartyConfig(txq.getCtnt_3rdp_id());
            HttpPost hp = new HttpPost();
            try {
                // enhance Service-Portal default XML post
                Map map = new HashMap();
                map.put("srvcid", String.valueOf(txq.getSrvc_main_id()));
                map.put("operid", String.valueOf(txq.getOper().getId()));
                map.put("msisdn", txq.getMsisdn());

                String url = StringUtil.fillMessageVariables(map, tpconf.getUrl());
                url = StringUtil.fillMessageSqlAnnotate(url);
                
                log.info("post to URL:" + url);

                hp.setUrl(url);
                hp.setContenttype("text/xml");
                hp.setMethod("POST");
                if ((tpconf.getUser() != null)
                        && (!tpconf.getUser().trim().isEmpty())) {
                    hp.setUserpswd(tpconf.getUser(), tpconf.getPassword());
                }

                OutputStream os = hp.getWriter();

                sp_m.marshal(msgPortalElement, os);

                sp_m.marshal(msgPortalElement, System.out);

                readResponse(txq, StringConvert.stripNonValidXMLCharacters(hp.getResponse()), se.getOwner().getUid());
            } catch (Exception e) {
                throw e;
            } finally {
                hp.disconnect();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "sending to 3rd party failed!! --> {0}", e.getMessage());
        } finally {
            try {
                txq.setStatus(this.status);
            } catch (Exception e) {
                log.log(Level.SEVERE, "cannot update status to trns_3rd_mo record!!", e);
            }
        }
    }

    public static com.hippoping.ServicePortal.request.MessagePortalElement Parse(String content) throws JAXBException {
        com.hippoping.ServicePortal.request.MessagePortalElement mpe = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("com.hippoping.ServicePortal.request");

            Unmarshaller u = jc.createUnmarshaller();

            mpe = (com.hippoping.ServicePortal.request.MessagePortalElement) u.unmarshal(new StreamSource(new StringReader(content)));
        } catch (JAXBException je) {
            throw je;
        }

        return mpe;
    }
}
