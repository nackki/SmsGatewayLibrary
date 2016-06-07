package hippoping.smsgw.api.subscription;

import com.truemove.css.mo.response.RsrDetailElement;
import com.truemove.css.thirdparty.response.Response;
import hippoping.smsgw.api.db.ServiceElement;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.SocketTimeoutException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import lib.common.DatetimeUtil;
import lib.common.HttpPost;

public class TruemoveHSubscriptionService {

    public Response sendCss(int srvc_main_id, String msisdn, String keyword)
            throws Exception {
        Response cssResponse = null;

        HttpPost hp = new HttpPost();
        try {
            hp.setUrl("http://truecssgateway:8080/trueh-app/true/css3rdInterface");
            hp.setContenttype("application/x-www-form-urlencoded");
            String data = "srvc_main_id=" + srvc_main_id + "&msisdn=" + msisdn + "&ud=" + keyword;

            OutputStream os = hp.getWriter();
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write(data);
            w.close();

            String resp = hp.getResponse();

            JAXBContext css_jc = JAXBContext.newInstance("com.truemove.css.thirdparty.response");
            Unmarshaller css_u = css_jc.createUnmarshaller();
            cssResponse = (Response) css_u.unmarshal(new StreamSource(new StringReader(resp)));
        } catch (SocketTimeoutException e) {
            throw new Exception("cannot establish the connection");
        } catch (Exception e) {
            throw e;
        } finally {
            hp.disconnect();
        }

        return cssResponse;
    }

    public RsrDetailElement sendRegister(ServiceElement se, String msisdn) throws Exception {
        RsrDetailElement reply = null;

        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message id=\"\"> <sms type=\"mo\">  <retry count=\"0\" max=\"0\"/>  <destination messageid=\"\">   <address>    <number type=\"abbreviated\">4240001</number>   </address>  </destination>  <source>   <address>    <number type=\"international\">66865210783</number>   </address>  </source>  <ud type=\"text\">R</ud>  <scts>2009-10-01T11:15:05Z</scts>  <service-id>0101077551</service-id> </sms> <from></from> <to></to></message>";

        JAXBContext jc = JAXBContext.newInstance("com.truemove.css.mo.request");
        Unmarshaller u = jc.createUnmarshaller();
        Marshaller m = jc.createMarshaller();

        com.truemove.css.mo.request.MessageElement me = (com.truemove.css.mo.request.MessageElement) u.unmarshal(new StreamSource(new StringReader(xmlStr)));

        me.getSms().getDestination().getAddress().getNumber().setValue(se.srvc_id);
        me.getSms().getUd().setContent(se.sms_register);
        me.getSms().setServiceId(se.srvc_id_mo);
        me.setFrom("Internal Process");
        String resp = "";

        HttpPost hp = new HttpPost();

        String url = "http://smsgatewaydtac:8080/trueh-app/true/mohandle";
        String dt = DatetimeUtil.getDateTime("yyyy-MM-dd'T'HH:mm:ss'Z'");
        me.getSms().setStcs(dt);
        me.getSms().getSource().getAddress().getNumber().setValue(msisdn);

        me.setTo(url);
        hp = new HttpPost();
        try {
            hp.setUrl(url);

            OutputStream os = hp.getWriter();

            m.marshal(me, os);
            resp = hp.getResponse();

            jc = JAXBContext.newInstance("com.truemove.css.mo.response");
            u = jc.createUnmarshaller();
            com.truemove.css.mo.response.MessageElement messageElement = (com.truemove.css.mo.response.MessageElement) u.unmarshal(new StreamSource(new StringReader(resp)));
            reply = messageElement.getRsr().getRsrDetail();
        } catch (SocketTimeoutException e) {
            throw new Exception("cannot establish the connection");
        } catch (Exception e) {
            throw e;
        } finally {
            hp.disconnect();
        }

        return reply;
    }

    public RsrDetailElement sendUnregister(ServiceElement se, String msisdn) throws Exception {
        RsrDetailElement reply = null;

        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message id=\"\"> <sms type=\"mo\">  <retry count=\"0\" max=\"0\"/>  <destination messageid=\"\">   <address>    <number type=\"abbreviated\">4240001</number>   </address>  </destination>  <source>   <address>    <number type=\"international\">66865210783</number>   </address>  </source>  <ud type=\"text\">R</ud>  <scts>2009-10-01T11:15:05Z</scts>  <service-id>0101077551</service-id> </sms> <from></from> <to></to></message>";

        JAXBContext jc = JAXBContext.newInstance("com.truemove.css.mo.request");
        Unmarshaller u = jc.createUnmarshaller();
        Marshaller m = jc.createMarshaller();

        com.truemove.css.mo.request.MessageElement me = (com.truemove.css.mo.request.MessageElement) u.unmarshal(new StreamSource(new StringReader(xmlStr)));

        me.getSms().getDestination().getAddress().getNumber().setValue(se.srvc_id);
        me.getSms().getUd().setContent(se.sms_unregister);
        me.getSms().setServiceId(se.srvc_id_mo);
        me.setFrom("Internal Process");
        String resp = "";

        HttpPost hp = new HttpPost();

        String url = "http://smsgatewaydtac:8080/trueh-app/true/mohandle";
        String dt = DatetimeUtil.getDateTime("yyyy-MM-dd'T'HH:mm:ss'Z'");
        me.getSms().setStcs(dt);
        me.getSms().getSource().getAddress().getNumber().setValue(msisdn);

        me.setTo(url);
        hp = new HttpPost();
        try {
            hp.setUrl(url);

            OutputStream os = hp.getWriter();

            m.marshal(me, os);
            resp = hp.getResponse();

            jc = JAXBContext.newInstance("com.truemove.css.mo.response");
            u = jc.createUnmarshaller();
            com.truemove.css.mo.response.MessageElement messageElement = (com.truemove.css.mo.response.MessageElement) u.unmarshal(new StreamSource(new StringReader(resp)));
            reply = messageElement.getRsr().getRsrDetail();
        } catch (SocketTimeoutException e) {
            throw new Exception("cannot establish the connection");
        } catch (Exception e) {
            throw e;
        } finally {
            hp.disconnect();
        }

        return reply;
    }
}