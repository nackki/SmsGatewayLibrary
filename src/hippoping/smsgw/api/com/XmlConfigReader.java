package hippoping.smsgw.api.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.CommonConfig;
import lib.common.XmlParse;

public class XmlConfigReader extends XmlParse
        implements Serializable {

    private static final Logger log = Logger.getLogger(XmlConfigReader.class.getName());
    protected String config_filename;
    protected Hashtable config = null;

    public Hashtable getConfig() {
        return this.config;
    }

    public XmlConfigReader(String filename) {
        this.config_filename = "${" + CommonConfig.OGS_CLUSTER_CONFIG_PATH + "}/" + filename;
        this.config = read();
    }

    public Hashtable read() {
        Hashtable ht;
        try {
            String ff = CommonConfig.getAbsoluteFile(CommonConfig.OGS_CLUSTER_CONFIG_PATH, this.config_filename);
            log.info("read config file -> " + ff);
            File file = new File(ff);
            if (file == null) {
                throw new Exception("error read file " + this.config_filename);
            }
            
            FileInputStream fis = new FileInputStream(file);
            try {
                ht = parse(fis);
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
            return null;
        }
        return ht;
    }
}