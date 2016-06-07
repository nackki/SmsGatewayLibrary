package hippoping.smsgw.api.mt.tps;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.CommonConfig;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class TpsConfiguration {

    private static final String TPS_CFG_FILE = "${ogs.cluster.config.config_path}/tps.xml";
    
    private static final Logger log = Logger.getLogger(TpsConfiguration.class.getName());
    private static TpsConfiguration tpsConfiguration = null;
    private static String tps = "";

    /* A private Constructor prevents any other 
     * class from instantiating.
     */
    private TpsConfiguration() {
    }

    private TpsConfiguration(CARRIER oper) {
        SAXBuilder builder = new SAXBuilder();

        String config = CommonConfig.getAbsoluteFile("ogs.cluster.config.config_path", TPS_CFG_FILE);
        try {
            File f = new File(config);
            if ((!f.exists()) || (!f.canRead())) {
                log.log(Level.INFO, "No such file process -> {0}", config);
                tpsConfiguration = new TpsConfiguration();
                throw new FileNotFoundException(config);
            }

            Document doc = builder.build(f);
            Element root = doc.getRootElement();

            tps = root.getChild(oper.name()).getTextTrim();
            log.log(Level.INFO, "Transaction Per Second(TPS):''{0}''", tps);
        } catch (IOException | JDOMException e) {
            log.severe(e.getMessage());
        }
    }

    /**
     * Get singleton instance object
     *
     * @param oper
     * @return
     */
    public static TpsConfiguration getInstance(CARRIER oper) {
        if (tpsConfiguration == null) {
            // Initializing with oper_id
            tpsConfiguration = new TpsConfiguration(oper);
        }
        return tpsConfiguration;
    }

    public String getTps() {
        return tps;
    }

}