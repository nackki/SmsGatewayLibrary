/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.com;

import java.util.HashMap;

/**
 *
 * @author ITZONE
 */
public class SimpleXmlConfigFactory {

    private static HashMap<String, XmlConfigReader> ref = new HashMap<String, XmlConfigReader>();
    private static final int _MAX_BUFFER_SIZE = 100;

    public SimpleXmlConfigFactory() {
        // no code req'd
    }

    public static XmlConfigReader getInstance(String filename) {
        if (!ref.containsKey(filename)) // it's ok, we can call this constructor
        {
            if (ref.size() > _MAX_BUFFER_SIZE) {
                ref.clear();
            }
            ref.put(filename, new XmlConfigReader(filename));
        }
        return ref.get(filename);
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
        // that'll teach 'em
    }
}
