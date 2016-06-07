package hippoping.smsgw.api.subscription;

import com.ais.legacy.dlvrmsg.response.XML;
import hippoping.smsgw.api.db.ServiceElement;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import lib.common.DatetimeUtil;
import lib.common.HttpPost;

public class AisLegacySubscriptionService {

    public XML sendRegister(ServiceElement se, String msisdn)
            throws Exception {
        XML xml = null;

        String url = "http://smsgatewayais:8080/smsgw-ais-legacy-war/legacy/mohandle";
        String data = "TRANSID=" + DatetimeUtil.getDateTime("yyyyMMddHHmmss") + "&CMD=DLVRMSG" + "&FET=SMS" + "&NTYPE=ONE2CALL" + "&FROM=" + msisdn + "&TO=" + se.srvc_id + "&CODE=REQUEST" + "&CTYPE=TEXT" + "&CONTENT=" + se.sms_register;

        HttpPost hp = new HttpPost();
        try {
            hp.setContenttype("application/x-www-form-urlencoded");
            hp.setUrl(url);

            OutputStream os = hp.getWriter();
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write(data);
            w.close();

            String resp = hp.getResponse();

            JAXBContext jc = JAXBContext.newInstance("com.ais.legacy.dlvrmsg.response");
            Unmarshaller um = jc.createUnmarshaller();
            xml = (XML) um.unmarshal(new StreamSource(new StringReader(resp)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hp.disconnect();
        }

        return xml;
    }

    public XML sendUnregister(ServiceElement se, String msisdn) throws Exception {
        XML xml = null;

        String url = "http://smsgatewayais:8080/smsgw-ais-legacy-war/legacy/mohandle";
        String data = "TRANSID=" + DatetimeUtil.getDateTime("yyyyMMddHHmmss") + "&CMD=DLVRMSG" + "&FET=SMS" + "&NTYPE=ONE2CALL" + "&FROM=" + msisdn + "&TO=" + se.srvc_id + "&CODE=REQUEST" + "&CTYPE=TEXT" + "&CONTENT=" + se.sms_unregister;

        HttpPost hp = new HttpPost();
        try {
            hp.setContenttype("application/x-www-form-urlencoded");
            hp.setUrl(url);

            OutputStream os = hp.getWriter();
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write(data);
            w.close();

            String resp = hp.getResponse();

            JAXBContext jc = JAXBContext.newInstance("com.ais.legacy.dlvrmsg.response");
            Unmarshaller um = jc.createUnmarshaller();
            xml = (XML) um.unmarshal(new StreamSource(new StringReader(resp)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hp.disconnect();
        }

        return xml;
    }
}