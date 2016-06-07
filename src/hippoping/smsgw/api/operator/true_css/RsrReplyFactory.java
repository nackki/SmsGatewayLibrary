package hippoping.smsgw.api.operator.true_css;

import com.truemove.css.mo.response.RsrElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class RsrReplyFactory
        implements Serializable {

    private com.truemove.css.mo.request.MessageElement request;
    protected int status;
    protected String status_desc;
    private static final Logger log = Logger.getLogger(RsrReplyFactory.class.getName());
    
    public RsrReplyFactory() {}

    public RsrReplyFactory(com.truemove.css.mo.request.MessageElement rq, int status, String status_desc) {
        this.request = rq;
        this.status = status;
        this.status_desc = status_desc;
    }

    public void write(PrintWriter out) throws ServletException, IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.truemove.css.mo.response");
            Unmarshaller u = jc.createUnmarshaller();
            Marshaller m = jc.createMarshaller();
            String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message id=\"55443\"><rsr type=\"reply\"><service-id>0100010001</service-id><destination messageid=\"1061779471538\"><address><number type=\"abbreviated\">9600</number></address></destination><source><address><number type=\"international\">668xxxxxxx</number></address></source> <rsr_detail status=\"success\"><code>0</code><description>Success receive request</description ></rsr_detail></rsr></message>";

            com.truemove.css.mo.response.MessageElement rs = (com.truemove.css.mo.response.MessageElement) u.unmarshal(new StreamSource(new StringReader(xmlStr)));

            rs.setId(this.request.getId());
            RsrElement rsr = rs.getRsr();
            rsr.setType("reply");
            rsr.setServiceId(this.request.getSms().getServiceId());
            rsr.getDestination().setMessageid(this.request.getSms().getDestination().getMessageid());
            rsr.getDestination().getAddress().getNumber().setValue(this.request.getSms().getDestination().getAddress().getNumber().getValue());
            rsr.getDestination().getAddress().getNumber().setType(this.request.getSms().getDestination().getAddress().getNumber().getType());
            rsr.getSource().getAddress().getNumber().setType(this.request.getSms().getSource().getAddress().getNumber().getType());
            rsr.getSource().getAddress().getNumber().setValue(this.request.getSms().getSource().getAddress().getNumber().getValue());
            if (this.status == 0) {
                rsr.getRsrDetail().setStatus("success");
            } else {
                rsr.getRsrDetail().setStatus("failure");
            }
            rsr.getRsrDetail().setCode(BigInteger.valueOf(this.status));
            rsr.getRsrDetail().setDescription(this.status_desc);

            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            m.marshal(rs, out);

            m.marshal(rs, System.out);
        } catch (JAXBException e) {
            log.severe(e.getMessage());
        }
    }

    public void setRequest(com.truemove.css.mo.request.MessageElement request) {
        this.request = request;
    }

    public String getStatus_desc() {
        return this.status_desc;
    }

    public int getStatus() {
        return this.status;
    }

    protected Object getRequest() {
        return this.request;
    }
}