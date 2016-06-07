/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.Message.SMS_TYPE;
import java.io.File;

/**
 *
 * @author nack_ki
 */
public interface MessageInterface {

    public int add(String content, SMS_TYPE type, int disposable, File file, int uid) throws Exception;
    public int remove() throws Exception;
    public boolean isContentBlank(int id);
    public int sync() throws Exception;
}
