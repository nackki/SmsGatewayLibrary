package hippoping.smsgw.api.mt.exsql;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lib.common.CommonConfig;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class PushMTExtendSql {

    private static final String PUSH_MT_EXSQL = "${ogs.cluster.config.config_path}/push_mt_sql.xml";

    private static final Logger log = Logger.getLogger(PushMTExtendSql.class.getName());
    private static PushMTExtendSql pushMTExtendSql = null;
    private static final String NODE_TXQUEUE = "txsql";
    private static final String NODE_EXTEND = "exsql";
    private static String exsql = "";
    private static String txsql = "";

    private static String AP_NAME = "";

    /* A private Constructor prevents any other 
     * class from instantiating.
     */
    private PushMTExtendSql() {
        try {
            Context c = new InitialContext();
            AP_NAME = (String) c.lookup("java:app/AppName");
        } catch (NamingException ne) {
            log.log(Level.SEVERE, "exception caught!!", ne);
        }
    }

    private PushMTExtendSql(CARRIER oper) {
        this();
        SAXBuilder builder = new SAXBuilder();

        String config = CommonConfig.getAbsoluteFile("ogs.cluster.config.config_path", PUSH_MT_EXSQL);
        try {
            File f = new File(config);
            if ((!f.exists()) || (!f.canRead())) {
                log.log(Level.INFO, "No such file process -> {0}", config);
                pushMTExtendSql = new PushMTExtendSql();
                throw new FileNotFoundException(config);
            }

            Document doc = builder.build(f);
            Element root = doc.getRootElement();
            
            Element oper_node = root.getChild(oper.name());
//            // supported especially for Application name
//            if (AP_NAME != null && !AP_NAME.isEmpty()) {
//                log.log(Level.INFO, "load especially config for ''{0}''", AP_NAME);
//                oper_node = root.getChild(oper.name()).getChild(AP_NAME);
//            }
            
            // exsql
            Element extendSql_node = oper_node.getChild(NODE_EXTEND);
            if (extendSql_node != null ) {
                exsql = extendSql_node.getTextTrim();
            }
            
            // txsql
            Element txqSql_node = oper_node.getChild(NODE_TXQUEUE);
            if (txqSql_node != null ) {
                txsql = txqSql_node.getTextTrim();
            }

            log.log(Level.INFO, "extended SQL:''{0}''", exsql);
            log.log(Level.INFO, "tx_queue SQL:''{0}''", txsql);
        } catch (IOException | JDOMException e) {
            log.log(Level.SEVERE, "error occur!!", e);
        }
    }

    /**
     * Get singleton instance object
     *
     * @param oper
     * @return
     */
    public static PushMTExtendSql getInstance(CARRIER oper) {
        if (pushMTExtendSql == null) {
            // Initializing with oper_id
            pushMTExtendSql = new PushMTExtendSql(oper);
        }
        return pushMTExtendSql;
    }

    public String getExsql() {
        return exsql;
    }

    public String getTxsql() {
        return txsql;
    }
}
