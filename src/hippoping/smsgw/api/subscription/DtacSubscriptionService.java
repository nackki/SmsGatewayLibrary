package hippoping.smsgw.api.subscription;

import com.dtac.cpa.mo.Authentication;
import com.dtac.cpa.mo.CpaMobileRequest;
import com.dtac.cpa.response.CpaResponse;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import lib.common.DatetimeUtil;
import lib.common.HttpPost;
import lib.common.StringConvert;

public class DtacSubscriptionService {

    private static final Logger logger = Logger.getLogger(DtacSubscriptionService.class.getName());

    private static XMLSerializer getXMLSerializer(OutputStream os) {
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

    public CpaResponse sendRegister(ServiceElement se, String msisdn) throws Exception {
        CpaResponse reply = null;

        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><cpa-mobile-request>\n<txid>15788313699</txid>\n<authentication>\n<user>cms</user>\n<password>no-auth</password>\n</authentication>\n<destination>\n<msisdn>1911423</msisdn>\n<serviceid>1911423</serviceid>\n</destination>\n<originator>\n<msisdn>66832958281</msisdn>\n</originator>\n<message>\n<header>\n<timestamp>20100708161714</timestamp>\n</header>\n<sms>\n<msg>c</msg>\n<msgtype>E</msgtype>\n<encoding>0</encoding>\n</sms>\n</message>\n<startCallDateTime>20100708161714</startCallDateTime>\n</cpa-mobile-request>\n";

        JAXBContext jc = JAXBContext.newInstance("com.dtac.cpa.mo");
        Unmarshaller u = jc.createUnmarshaller();
        Marshaller m = jc.createMarshaller();

        CpaMobileRequest cpaMobileRequest = (CpaMobileRequest) u.unmarshal(new StreamSource(new StringReader(xmlStr)));

        OperConfig oc = new OperConfig(se.srvc_main_id, OperConfig.CARRIER.DTAC);
        Authentication auth = new Authentication();
        auth.setUser(oc.user);
        auth.setPassword(oc.password);
        cpaMobileRequest.setAuthentication(auth);

        String date = DatetimeUtil.getDateTime("yyyyMMddHHmmss");
        cpaMobileRequest.setTxid(date);
        cpaMobileRequest.getDestination().setMsisdn(se.srvc_id);
        cpaMobileRequest.getDestination().setServiceid(se.srvc_id);
        cpaMobileRequest.getOriginator().setMsisdn(msisdn);
        cpaMobileRequest.getMessage().getHeader().setTimestamp(date);
        cpaMobileRequest.getMessage().getSms().setMsg(se.sms_register);
        cpaMobileRequest.setStartCallDateTime(date);

        String url = "http://smsgatewaydtac:8080/Subscription-gateway-dtac-war/dtac/mohandle";
        HttpPost hp = new HttpPost();

        hp.setUrl(url);

        XMLOutputFactory out = XMLOutputFactory.newInstance();
        OutputStream os;
        XMLStreamWriter xmlwr;
        try {
            os = hp.getWriter();
            xmlwr = out.createXMLStreamWriter(os);

            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            XMLSerializer serializer = getXMLSerializer(os);

            m.marshal(cpaMobileRequest, serializer.asContentHandler());

            xmlwr.flush();

            StringWriter sw = new StringWriter();
            m.marshal(cpaMobileRequest, sw);
            logger.log(Level.INFO, sw.getBuffer().toString());
            sw.close();

            String resp = hp.getResponse();

            resp = StringConvert.stripNonValidXMLCharacters(resp.trim());

            jc = JAXBContext.newInstance("com.dtac.cpa.response");
            u = jc.createUnmarshaller();
            reply = (CpaResponse) u.unmarshal(new StreamSource(new StringReader(resp)));
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("server connection failure");
        } finally {
            hp.disconnect();
        }

        return reply;
    }

    public CpaResponse sendUnregister(ServiceElement se, String msisdn) throws Exception {
        CpaResponse reply = null;

        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><cpa-mobile-request>\n<txid>15788313699</txid>\n<authentication>\n<user>administrator</user>\n<password>nuskinth</password>\n</authentication>\n<destination>\n<msisdn>1911423</msisdn>\n<serviceid>1911423</serviceid>\n</destination>\n<originator>\n<msisdn>66832958281</msisdn>\n</originator>\n<message>\n<header>\n<timestamp>20100708161714</timestamp>\n</header>\n<sms>\n<msg>c</msg>\n<msgtype>E</msgtype>\n<encoding>0</encoding>\n</sms>\n</message>\n<startCallDateTime>20100708161714</startCallDateTime>\n</cpa-mobile-request>\n";

        JAXBContext jc = JAXBContext.newInstance("com.dtac.cpa.mo");
        Unmarshaller u = jc.createUnmarshaller();
        Marshaller m = jc.createMarshaller();

        CpaMobileRequest cpaMobileRequest = (CpaMobileRequest) u.unmarshal(new StreamSource(new StringReader(xmlStr)));

        OperConfig oc = new OperConfig(se.srvc_main_id, OperConfig.CARRIER.DTAC);
        Authentication auth = new Authentication();
        auth.setUser(oc.user);
        auth.setPassword(oc.password);
        cpaMobileRequest.setAuthentication(auth);

        String date = DatetimeUtil.getDateTime("yyyyMMddHHmmss");
        cpaMobileRequest.setTxid(date);
        cpaMobileRequest.getDestination().setMsisdn(se.srvc_id);
        cpaMobileRequest.getDestination().setServiceid(se.srvc_id);
        cpaMobileRequest.getOriginator().setMsisdn(msisdn);
        cpaMobileRequest.getMessage().getHeader().setTimestamp(date);
        cpaMobileRequest.getMessage().getSms().setMsg(se.sms_unregister);
        cpaMobileRequest.setStartCallDateTime(date);

        String url = "http://smsgatewaydtac:8080/Subscription-gateway-dtac-war/dtac/mohandle";
        HttpPost hp = new HttpPost();

        hp.setUrl(url);

        XMLOutputFactory out = XMLOutputFactory.newInstance();
        OutputStream os;
        XMLStreamWriter xmlwr;
        try {
            os = hp.getWriter();
            xmlwr = out.createXMLStreamWriter(os);

            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            XMLSerializer serializer = getXMLSerializer(os);

            m.marshal(cpaMobileRequest, serializer.asContentHandler());

            xmlwr.flush();

            StringWriter sw = new StringWriter();
            m.marshal(cpaMobileRequest, sw);
            logger.log(Level.INFO, sw.getBuffer().toString());
            sw.close();

            String resp = hp.getResponse();

            resp = StringConvert.stripNonValidXMLCharacters(resp.trim());

            jc = JAXBContext.newInstance("com.dtac.cpa.response");
            u = jc.createUnmarshaller();
            reply = (CpaResponse) u.unmarshal(new StreamSource(new StringReader(resp)));
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("server connection failure");
        } finally {
            hp.disconnect();
        }

        return reply;
    }
}