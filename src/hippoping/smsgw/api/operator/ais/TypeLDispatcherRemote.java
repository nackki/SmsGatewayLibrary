package hippoping.smsgw.api.operator.ais;

import hippoping.smsgw.api.db.ServiceElement;
import javax.ejb.Remote;

@Remote
public abstract interface TypeLDispatcherRemote {

    public abstract void putFile(ServiceElement paramServiceElement, String[] paramArrayOfString, TypeLMessageHeaderType.SSS_MSG_TYPE paramSSS_MSG_TYPE)
            throws Exception;
}