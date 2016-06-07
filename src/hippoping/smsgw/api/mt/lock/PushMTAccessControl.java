package hippoping.smsgw.api.mt.lock;

import hippoping.smsgw.api.db.OperConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.CommonConfig;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class PushMTAccessControl {

    private static final String PUSH_MT_LOCK = "${ogs.cluster.config.config_path}/push_mt_lock.xml";
    private static final Logger log = Logger.getLogger(PushMTAccessControl.class.getName());

    public static boolean isLocked(OperConfig.CARRIER oper) {
        SAXBuilder builder = new SAXBuilder();
        boolean lock = false;

        String config = CommonConfig.getAbsoluteFile("ogs.cluster.config.config_path", PUSH_MT_LOCK);
        try {
            File f = new File(config);
            if ((!f.exists()) || (!f.canRead())) {
                log.log(Level.INFO, "No such file process -> {0}", config);
                throw new FileNotFoundException(config);
            }

            Document doc = builder.build(f);
            Element root = doc.getRootElement();

            log.log(Level.INFO, "push_mt_lock flag:{0}", root.getChild(oper.name()).getTextTrim());
            lock = root.getChild(oper.name()).getTextTrim().equals("LOCK");
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        return lock;
    }
}