/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.operator.trueh_css;

import com.truemove.css.ussd.response.RsrElement;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.UssdReplyMessageFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author nacks_mcair
 */
public class UssdRsrReplyFactory extends RsrReplyFactory
        implements Serializable {

    protected com.truemove.css.ussd.request.MessageElement request;
    private static final Logger log = Logger.getLogger(UssdRsrReplyFactory.class.getName());

    public UssdRsrReplyFactory(com.truemove.css.ussd.request.MessageElement rq, int status, String status_desc) {
        this.request = rq;
        this.status = status;
        this.status_desc = status_desc;
    }

    @Override
    public void write(PrintWriter out) throws ServletException, IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.truemove.css.ussd.response");
            Unmarshaller u = jc.createUnmarshaller();
            Marshaller m = jc.createMarshaller();
            String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<message id=\"55443\">"
                    + "<rsr type=\"reply\">"
                    + "<service-id>0100010001</service-id>"
                    + "<destination messageid=\"1061779471538\">"
                    + "<address><number type=\"abbreviated\">9600</number></address>"
                    + "</destination>"
                    + "<source>"
                    + "<address><number type=\"international\">668xxxxxxx</number></address>"
                    + "</source>"
                    + "<rsr_detail status=\"success\">"
                    + "<code>0</code>"
                    + "<description>Success receive request</description >"
                    + "</rsr_detail>"
                    + "<ussd_session>"
                    + " <transaction_no>1234567890ABCD</transaction_no>"
                    + " <lang>TH</lang>"
                    + " <respcode>xxx</respcode>"
                    + " <respmsg>Thank you and wait sms confirm.</respmsg>"
                    + " <respdesc>success</respdesc>"
                    + "</ussd_session>"
                    + "</rsr>"
                    + "</message>";

            com.truemove.css.ussd.response.MessageElement rs
                    = (com.truemove.css.ussd.response.MessageElement) u.unmarshal(new StreamSource(new StringReader(xmlStr)));

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

            String msg = UssdReplyMessageFactory.DEF_MSG;
            /**
             * ignore the USSD direct reply here!! coz it doesn't support u32.
            try {
                msg = UssdReplyMessageFactory.getReplyMessage(this.request, CARRIER.TRUEH);
            } catch (Exception e) {
                log.log(Level.SEVERE, "UssdReplyMessageFactory has been error!!", e);
            }
                    */
            
            // USSD-session
            rsr.getUssdSession().setTransactionNo(this.request.getSms().getUssdSession().getTransactionNo());
            rsr.getUssdSession().setLang("TH");
            rsr.getUssdSession().setRespcode("601");
            rsr.getUssdSession().setRespmsg(msg);
            rsr.getUssdSession().setRespdesc("success");

            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            m.marshal(rs, out);

            StringWriter sw = new StringWriter();
            m.marshal(rs, sw);
            log.info(sw.toString());
        } catch (JAXBException e) {
            log.severe(e.getMessage());
        }
    }

    public void setRequest(com.truemove.css.ussd.request.MessageElement request) {
        this.request = request;
    }
}
