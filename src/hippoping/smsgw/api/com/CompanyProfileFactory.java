/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.com;

/**
 *
 * @author ITZONE
 */
public class CompanyProfileFactory {
    
    private static String config_filename = "company-profile.xml";
    private static XmlConfigReader ref;

    public CompanyProfileFactory() {
        // no code req'd
    }

    public static XmlConfigReader getCompanyProfile() {
        if (ref == null) // it's ok, we can call this constructor
        {
            ref = new XmlConfigReader(config_filename);
        }
        return ref;
    }

    public static XmlConfigReader getCompanyProfile(String filename) {
        if (ref == null) // it's ok, we can call this constructor
        {
            ref = new XmlConfigReader(filename);
        }
        return ref;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
        // that'll teach 'em
    }
}
