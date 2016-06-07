package hippoping.smsgw.api.operator.ais;

import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.hybrid.HybridwsdlStub;
import javax.ejb.Remote;

@Remote
public abstract interface HybridDispatcherRemote
{
  public abstract HybridwsdlStub.SendMOResponse sendRegister(ServiceElement paramServiceElement, String paramString)
    throws Exception;

  public abstract HybridwsdlStub.SendMOResponse sendUnregister(ServiceElement paramServiceElement, String paramString)
    throws Exception;

  public abstract String Hello(String paramString);
}