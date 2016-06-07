package hippoping.smsgw.api.operator.dtac_sdp;

import com.dtac.sdp.response.CpaResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class CpaResponseFactory
        implements Serializable {

    protected String txid;
    protected int status;
    protected String status_desc;

    public CpaResponseFactory(String txid, int status, String status_desc) {
        this.txid = txid;
        this.status = status;
        this.status_desc = status_desc;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public void write(PrintWriter out) throws ServletException, IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.dtac.sdp.response");
            Unmarshaller u = jc.createUnmarshaller();
            Marshaller m = jc.createMarshaller();
            StringBuffer xmlStr = new StringBuffer(
                    "<cpa-response>"
                    + "<txid>xxx</txid>"
                    + "<status>200</status>"
                    + "<status-description>Success</status-description>"
                    + "</cpa-response>");

            CpaResponse cr = (CpaResponse) u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
            cr.setStatus(BigInteger.valueOf(this.status));
            cr.setStatusDescription(this.status_desc);
            cr.setTxid(this.txid);

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(cr, out);

            StringWriter sw = new StringWriter();
            m.marshal(cr, sw);
            System.out.println("response: " + sw.toString());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public String getStatus_desc() {
        return this.status_desc;
    }

    public int getStatus() {
        return this.status;
    }

    public String getTxid() {
        return this.txid;
    }
}