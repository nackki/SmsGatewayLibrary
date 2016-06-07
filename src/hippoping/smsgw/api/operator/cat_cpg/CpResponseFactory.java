package hippoping.smsgw.api.operator.cat_cpg;

import com.cat.cpg.mo.response.Cpresponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class CpResponseFactory
        implements Serializable {
    
    private static final Logger log = Logger.getLogger(CpResponseFactory.class.getName());

    protected String txid;
    protected int status;
    protected String status_desc;

    public CpResponseFactory(String txid, int status, String status_desc) {
        this.txid = txid;
        this.status = status;
        this.status_desc = status_desc;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public void write(PrintWriter out) throws ServletException, IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.cat.cpg.mo.response");
            Unmarshaller u = jc.createUnmarshaller();
            Marshaller m = jc.createMarshaller();

            Cpresponse cr = new Cpresponse();
            cr.setTxid(this.txid);
            cr.setStatus(String.valueOf(this.status));
            cr.setDescription(this.status_desc);

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(cr, out);

            StringWriter sw = new StringWriter();
            m.marshal(cr, sw);
            log.info(sw.toString());
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "write reponse error!!", e);
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