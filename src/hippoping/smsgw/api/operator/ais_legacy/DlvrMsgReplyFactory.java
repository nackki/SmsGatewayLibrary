package hippoping.smsgw.api.operator.ais_legacy;

import com.ais.legacy.dlvrmsg.response.XML;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class DlvrMsgReplyFactory
        implements Serializable {

    private static final Logger log = Logger.getLogger(DlvrMsgReplyFactory.class.getName());
    protected DlvrMsgRequest request;
    protected String status;
    protected String status_desc;

    public DlvrMsgReplyFactory(DlvrMsgRequest rq, String status, String status_desc) {
        this.request = rq;
        this.status = status;
        this.status_desc = status_desc;
    }

    public void write(PrintWriter out) throws ServletException, IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.ais.legacy.dlvrmsg.response");
            Marshaller m = jc.createMarshaller();
            XML rs = new XML();
            rs.setSTATUS(this.status);
            rs.setDETAIL(this.status_desc);

            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            m.marshal(rs, out);

            m.marshal(rs, System.out);
        } catch (JAXBException e) {
            log.severe(e.getMessage());
        }
    }

    public void setRequest(DlvrMsgRequest request) {
        this.request = request;
    }

    public String getStatus_desc() {
        return this.status_desc;
    }

    public String getStatus() {
        return this.status;
    }

    public DlvrMsgRequest getRequest() {
        return this.request;
    }
}