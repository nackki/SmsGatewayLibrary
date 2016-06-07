package hippoping.smsgw.api.hybrid;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBDataSource;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.Utils;

public class HybridwsdlStub extends Stub {

    protected AxisOperation[] _operations;
    private HashMap faultExceptionNameMap = new HashMap();
    private HashMap faultExceptionClassNameMap = new HashMap();
    private HashMap faultMessageMap = new HashMap();
    private static int counter = 0;
    private QName[] opNameArray = null;

    private static synchronized String getUniqueSuffix() {
        if (counter > 99999) {
            counter = 0;
        }
        counter += 1;
        return Long.toString(System.currentTimeMillis()) + "_" + counter;
    }

    private void populateAxisService()
            throws AxisFault {
        this._service = new AxisService("Hybridwsdl" + getUniqueSuffix());
        addAnonymousOperations();

        this._operations = new AxisOperation[1];

        AxisOperation __operation = new OutInAxisOperation();

        __operation.setName(new QName("urn:hybridwsdl", "sendMO"));
        this._service.addOperation(__operation);

        this._operations[0] = __operation;
    }

    private void populateFaults() {
    }

    public HybridwsdlStub(ConfigurationContext configurationContext, String targetEndpoint)
            throws AxisFault {
        this(configurationContext, targetEndpoint, false);
    }

    public HybridwsdlStub(ConfigurationContext configurationContext, String targetEndpoint, boolean useSeparateListener)
            throws AxisFault {
        populateAxisService();
        populateFaults();

        this._serviceClient = new ServiceClient(configurationContext, this._service);

        this._serviceClient.getOptions().setTo(new EndpointReference(targetEndpoint));

        this._serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
    }

    public HybridwsdlStub(ConfigurationContext configurationContext)
            throws AxisFault {
        this(configurationContext, "http://202.149.24.221/hybwebservice/hybserver.php");
    }

    public HybridwsdlStub()
            throws AxisFault {
        this("http://202.149.24.221/hybwebservice/hybserver.php");
    }

    public HybridwsdlStub(String targetEndpoint)
            throws AxisFault {
        this(null, targetEndpoint);
    }

    public SendMOResponse sendMO(SendMO sendMO0)
            throws RemoteException {
        MessageContext _messageContext = null;
        try {
            OperationClient _operationClient = this._serviceClient.createClient(this._operations[0].getName());
            _operationClient.getOptions().setAction("urn:hybridwsdl#sendMO");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

            addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

            _messageContext = new MessageContext();

            SOAPEnvelope env = null;

            env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), sendMO0, optimizeContent(new QName("urn:hybridwsdl", "sendMO")));

            this._serviceClient.addHeadersToEnvelope(env);

            _messageContext.setEnvelope(env);

            _operationClient.addMessageContext(_messageContext);

            _operationClient.execute(true);

            MessageContext _returnMessageContext = _operationClient.getMessageContext("In");

            SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

            Object object = fromOM(_returnEnv.getBody().getFirstElement(), SendMOResponse.class, getEnvelopeNamespaces(_returnEnv));

            return (SendMOResponse) object;
        } catch (AxisFault f) {
            OMElement faultElt = f.getDetail();
            if (faultElt != null) {
                if (this.faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    try {
                        String exceptionClassName = (String) this.faultExceptionClassNameMap.get(faultElt.getQName());
                        Class exceptionClass = Class.forName(exceptionClassName);
                        Exception ex = (Exception) exceptionClass.newInstance();

                        String messageClassName = (String) this.faultMessageMap.get(faultElt.getQName());
                        Class messageClass = Class.forName(messageClassName);
                        Object messageObject = fromOM(faultElt, messageClass, null);
                        Method m = exceptionClass.getMethod("setFaultMessage", new Class[]{messageClass});

                        m.invoke(ex, new Object[]{messageObject});

                        throw new RemoteException(ex.getMessage(), ex);
                    } catch (ClassCastException e) {
                        throw f;
                    } catch (ClassNotFoundException e) {
                        throw f;
                    } catch (NoSuchMethodException e) {
                        throw f;
                    } catch (InvocationTargetException e) {
                        throw f;
                    } catch (IllegalAccessException e) {
                        throw f;
                    } catch (InstantiationException e) {
                        throw f;
                    }
                }
                throw f;
            }

            throw f;
        } finally {
            _messageContext.getTransportOut().getSender().cleanup(_messageContext);
        }
    }

    public void startsendMO(SendMO sendMO0, final HybridwsdlCallbackHandler callback)
            throws RemoteException {
        OperationClient _operationClient = this._serviceClient.createClient(this._operations[0].getName());
        _operationClient.getOptions().setAction("urn:hybridwsdl#sendMO");
        _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

        addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

        SOAPEnvelope env = null;
        final MessageContext _messageContext = new MessageContext();

        env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), sendMO0, optimizeContent(new QName("urn:hybridwsdl", "sendMO")));

        this._serviceClient.addHeadersToEnvelope(env);

        _messageContext.setEnvelope(env);

        _operationClient.addMessageContext(_messageContext);

        _operationClient.setCallback(new AxisCallback() {
            public void onMessage(MessageContext resultContext) {
                try {
                    SOAPEnvelope resultEnv = resultContext.getEnvelope();

                    Object object =
                            fromOM(resultEnv.getBody().getFirstElement(), SendMOResponse.class, getEnvelopeNamespaces(resultEnv));

                    callback.receiveResultsendMO((HybridwsdlStub.SendMOResponse) object);
                } catch (AxisFault e) {
                    callback.receiveErrorsendMO(e);
                }
            }

            public void onError(Exception error) {
                if ((error instanceof AxisFault)) {
                    AxisFault f = (AxisFault) error;
                    OMElement faultElt = f.getDetail();
                    if (faultElt != null) {
                        if (HybridwsdlStub.this.faultExceptionNameMap.containsKey(faultElt.getQName())) {
                            try {
                                String exceptionClassName = (String) HybridwsdlStub.this.faultExceptionClassNameMap.get(faultElt.getQName());
                                Class exceptionClass = Class.forName(exceptionClassName);
                                Exception ex = (Exception) exceptionClass.newInstance();

                                String messageClassName = (String) HybridwsdlStub.this.faultMessageMap.get(faultElt.getQName());
                                Class messageClass = Class.forName(messageClassName);
                                Object messageObject = HybridwsdlStub.this.fromOM(faultElt, messageClass, null);
                                Method m = exceptionClass.getMethod("setFaultMessage", new Class[]{messageClass});

                                m.invoke(ex, new Object[]{messageObject});

                                callback.receiveErrorsendMO(new RemoteException(ex.getMessage(), ex));
                            } catch (ClassCastException e) {
                                callback.receiveErrorsendMO(f);
                            } catch (ClassNotFoundException e) {
                                callback.receiveErrorsendMO(f);
                            } catch (NoSuchMethodException e) {
                                callback.receiveErrorsendMO(f);
                            } catch (InvocationTargetException e) {
                                callback.receiveErrorsendMO(f);
                            } catch (IllegalAccessException e) {
                                callback.receiveErrorsendMO(f);
                            } catch (InstantiationException e) {
                                callback.receiveErrorsendMO(f);
                            } catch (AxisFault e) {
                                callback.receiveErrorsendMO(f);
                            }
                        } else {
                            callback.receiveErrorsendMO(f);
                        }
                    } else {
                        callback.receiveErrorsendMO(f);
                    }
                } else {
                    callback.receiveErrorsendMO(error);
                }
            }

            public void onFault(MessageContext faultContext) {
                AxisFault fault = Utils.getInboundFaultFromMessageContext(faultContext);
                onError(fault);
            }

            public void onComplete() {
                try {
                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                } catch (AxisFault axisFault) {
                    callback.receiveErrorsendMO(axisFault);
                }
            }
        });
        CallbackReceiver _callbackReceiver = null;
        if ((this._operations[0].getMessageReceiver() == null) && (_operationClient.getOptions().isUseSeparateListener())) {
            _callbackReceiver = new CallbackReceiver();
            this._operations[0].setMessageReceiver(_callbackReceiver);
        }

        _operationClient.execute(false);
    }

    private Map getEnvelopeNamespaces(SOAPEnvelope env) {
        Map returnMap = new HashMap();
        Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace ns = (OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

    private boolean optimizeContent(QName opName) {
        if (this.opNameArray == null) {
            return false;
        }
        for (int i = 0; i < this.opNameArray.length; i++) {
            if (opName.equals(this.opNameArray[i])) {
                return true;
            }
        }
        return false;
    }

    private OMElement toOM(SendMO param, boolean optimizeContent)
            throws AxisFault {
        try {
            return param.getOMElement(SendMO.MY_QNAME, OMAbstractFactory.getOMFactory());
        } catch (ADBException e) {
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(SendMOResponse param, boolean optimizeContent)
            throws AxisFault {
        try {
            return param.getOMElement(SendMOResponse.MY_QNAME, OMAbstractFactory.getOMFactory());
        } catch (ADBException e) {
            throw AxisFault.makeFault(e);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, SendMO param, boolean optimizeContent)
            throws AxisFault {
        try {
            SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
            emptyEnvelope.getBody().addChild(param.getOMElement(SendMO.MY_QNAME, factory));
            return emptyEnvelope;
        } catch (ADBException e) {
            throw AxisFault.makeFault(e);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory) {
        return factory.getDefaultEnvelope();
    }

    private Object fromOM(OMElement param, Class type, Map extraNamespaces)
            throws AxisFault {
        try {
            if (SendMO.class.equals(type)) {
                return HybridwsdlStub.SendMO.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

            if (SendMOResponse.class.equals(type)) {
                return HybridwsdlStub.SendMOResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
        return null;
    }

    public static class SendMO
            implements ADBBean {

        public static final QName MY_QNAME = new QName("urn:hybridwsdl", "sendMO", "ns1");
        protected HybridwsdlStub.HybridMO localSn;

        private static String generatePrefix(String namespace) {
            if (namespace.equals("urn:hybridwsdl")) {
                return "ns1";
            }
            return BeanUtil.getUniquePrefix();
        }

        public HybridwsdlStub.HybridMO getSn() {
            return this.localSn;
        }

        public void setSn(HybridwsdlStub.HybridMO param) {
            this.localSn = param;
        }

        public static boolean isReaderMTOMAware(XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = Boolean.TRUE.equals(reader.getProperty("IsDatahandlersAwareParsing"));
            } catch (IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        public OMElement getOMElement(QName parentQName, final OMFactory factory)
                throws ADBException {
            OMDataSource dataSource = new ADBDataSource(this, MY_QNAME) {
                public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                    HybridwsdlStub.SendMO.this.serialize(HybridwsdlStub.SendMO.MY_QNAME, factory, xmlWriter);
                }
            };
            return new OMSourcedElementImpl(MY_QNAME, factory, dataSource);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter)
                throws XMLStreamException, ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
                throws XMLStreamException, ADBException {
            String prefix = null;
            String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if ((namespace != null) && (namespace.trim().length() > 0)) {
                String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (serializeType) {
                String namespacePrefix = registerPrefix(xmlWriter, "urn:hybridwsdl");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":sendMO", xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "sendMO", xmlWriter);
                }

            }

            if (this.localSn == null) {
                throw new ADBException("sn cannot be null!!");
            }
            this.localSn.serialize(new QName("", "sn"), factory, xmlWriter);

            xmlWriter.writeEndElement();
        }

        private void writeAttribute(String prefix, String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        private void writeAttribute(String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        private void writeQNameAttribute(String namespace, String attName, QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String attributeNamespace = qname.getNamespaceURI();
            String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }

            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        private void writeQName(QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + ConverterUtil.convertToString(qname));
                } else {
                    xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(QName[] qnames, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (qnames != null) {
                StringBuffer stringToWrite = new StringBuffer();
                String namespaceURI = null;
                String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        private String registerPrefix(XMLStreamWriter xmlWriter, String namespace)
                throws XMLStreamException {
            String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = BeanUtil.getUniquePrefix();
                }

                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            return prefix;
        }

        public XMLStreamReader getPullParser(QName qName)
                throws ADBException {
            ArrayList elementList = new ArrayList();
            ArrayList attribList = new ArrayList();

            elementList.add(new QName("", "sn"));

            if (this.localSn == null) {
                throw new ADBException("sn cannot be null!!");
            }
            elementList.add(this.localSn);

            return new ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        public static class Factory {

            public static HybridwsdlStub.SendMO parse(XMLStreamReader reader)
                    throws Exception {
                HybridwsdlStub.SendMO object = new HybridwsdlStub.SendMO();

                String nillableValue = null;
                String prefix = "";
                String namespaceuri = "";
                try {
                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");

                        if (fullTypeName != null) {
                            String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;

                            String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                            if (!"sendMO".equals(type)) {
                                String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (HybridwsdlStub.SendMO) HybridwsdlStub.ExtensionMapper.getTypeObject(nsUri, type, reader);
                            }

                        }

                    }

                    Vector handledAttributes = new Vector();

                    reader.next();

                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if ((reader.isStartElement()) && (new QName("", "sn").equals(reader.getName()))) {
                        object.setSn(HybridwsdlStub.HybridMO.Factory.parse(reader));

                        reader.next();
                    } else {
                        throw new ADBException("Unexpected subelement " + reader.getLocalName());
                    }

                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if (reader.isStartElement()) {
                        throw new ADBException("Unexpected subelement " + reader.getLocalName());
                    }

                } catch (XMLStreamException e) {
                    throw new Exception(e);
                }

                return object;
            }
        }
    }

    public static class HybridMO
            implements ADBBean {

        protected String localServiceNumber;
        protected String localUsername;
        protected String localPassword;
        protected String localMsisdn;
        protected String localContentID;
        protected String localSgwid;

        private static String generatePrefix(String namespace) {
            if (namespace.equals("urn:hybridwsdl")) {
                return "ns1";
            }
            return BeanUtil.getUniquePrefix();
        }

        public String getServiceNumber() {
            return this.localServiceNumber;
        }

        public void setServiceNumber(String param) {
            this.localServiceNumber = param;
        }

        public String getUsername() {
            return this.localUsername;
        }

        public void setUsername(String param) {
            this.localUsername = param;
        }

        public String getPassword() {
            return this.localPassword;
        }

        public void setPassword(String param) {
            this.localPassword = param;
        }

        public String getMsisdn() {
            return this.localMsisdn;
        }

        public void setMsisdn(String param) {
            this.localMsisdn = param;
        }

        public String getContentID() {
            return this.localContentID;
        }

        public void setContentID(String param) {
            this.localContentID = param;
        }

        public String getSgwid() {
            return this.localSgwid;
        }

        public void setSgwid(String param) {
            this.localSgwid = param;
        }

        public static boolean isReaderMTOMAware(XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = Boolean.TRUE.equals(reader.getProperty("IsDatahandlersAwareParsing"));
            } catch (IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        public OMElement getOMElement(QName parentQName, final OMFactory factory)
                throws ADBException {
            OMDataSource dataSource = new ADBDataSource(this, parentQName) {
                public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                    HybridwsdlStub.HybridMO.this.serialize(this.parentQName, factory, xmlWriter);
                }
            };
            return new OMSourcedElementImpl(parentQName, factory, dataSource);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter)
                throws XMLStreamException, ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
                throws XMLStreamException, ADBException {
            String prefix = null;
            String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if ((namespace != null) && (namespace.trim().length() > 0)) {
                String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (serializeType) {
                String namespacePrefix = registerPrefix(xmlWriter, "urn:hybridwsdl");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":hybridMO", xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "hybridMO", xmlWriter);
                }

            }

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "serviceNumber", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "serviceNumber");
                }
            } else {
                xmlWriter.writeStartElement("serviceNumber");
            }

            if (this.localServiceNumber == null) {
                throw new ADBException("serviceNumber cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localServiceNumber);

            xmlWriter.writeEndElement();

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "username", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "username");
                }
            } else {
                xmlWriter.writeStartElement("username");
            }

            if (this.localUsername == null) {
                throw new ADBException("username cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localUsername);

            xmlWriter.writeEndElement();

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "password", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "password");
                }
            } else {
                xmlWriter.writeStartElement("password");
            }

            if (this.localPassword == null) {
                throw new ADBException("password cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localPassword);

            xmlWriter.writeEndElement();

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "msisdn", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "msisdn");
                }
            } else {
                xmlWriter.writeStartElement("msisdn");
            }

            if (this.localMsisdn == null) {
                throw new ADBException("msisdn cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localMsisdn);

            xmlWriter.writeEndElement();

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "contentID", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "contentID");
                }
            } else {
                xmlWriter.writeStartElement("contentID");
            }

            if (this.localContentID == null) {
                throw new ADBException("contentID cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localContentID);

            xmlWriter.writeEndElement();

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "sgwid", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "sgwid");
                }
            } else {
                xmlWriter.writeStartElement("sgwid");
            }

            if (this.localSgwid == null) {
                throw new ADBException("sgwid cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localSgwid);

            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement();
        }

        private void writeAttribute(String prefix, String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        private void writeAttribute(String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        private void writeQNameAttribute(String namespace, String attName, QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String attributeNamespace = qname.getNamespaceURI();
            String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }

            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        private void writeQName(QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + ConverterUtil.convertToString(qname));
                } else {
                    xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(QName[] qnames, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (qnames != null) {
                StringBuffer stringToWrite = new StringBuffer();
                String namespaceURI = null;
                String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        private String registerPrefix(XMLStreamWriter xmlWriter, String namespace)
                throws XMLStreamException {
            String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = BeanUtil.getUniquePrefix();
                }

                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            return prefix;
        }

        public XMLStreamReader getPullParser(QName qName)
                throws ADBException {
            ArrayList elementList = new ArrayList();
            ArrayList attribList = new ArrayList();

            elementList.add(new QName("", "serviceNumber"));

            if (this.localServiceNumber != null) {
                elementList.add(ConverterUtil.convertToString(this.localServiceNumber));
            } else {
                throw new ADBException("serviceNumber cannot be null!!");
            }

            elementList.add(new QName("", "username"));

            if (this.localUsername != null) {
                elementList.add(ConverterUtil.convertToString(this.localUsername));
            } else {
                throw new ADBException("username cannot be null!!");
            }

            elementList.add(new QName("", "password"));

            if (this.localPassword != null) {
                elementList.add(ConverterUtil.convertToString(this.localPassword));
            } else {
                throw new ADBException("password cannot be null!!");
            }

            elementList.add(new QName("", "msisdn"));

            if (this.localMsisdn != null) {
                elementList.add(ConverterUtil.convertToString(this.localMsisdn));
            } else {
                throw new ADBException("msisdn cannot be null!!");
            }

            elementList.add(new QName("", "contentID"));

            if (this.localContentID != null) {
                elementList.add(ConverterUtil.convertToString(this.localContentID));
            } else {
                throw new ADBException("contentID cannot be null!!");
            }

            elementList.add(new QName("", "sgwid"));

            if (this.localSgwid != null) {
                elementList.add(ConverterUtil.convertToString(this.localSgwid));
            } else {
                throw new ADBException("sgwid cannot be null!!");
            }

            return new ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        public static class Factory {

            public static HybridwsdlStub.HybridMO parse(XMLStreamReader reader)
                    throws Exception {
                HybridwsdlStub.HybridMO object = new HybridwsdlStub.HybridMO();

                String nillableValue = null;
                String prefix = "";
                String namespaceuri = "";
                try {
                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");

                        if (fullTypeName != null) {
                            String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;

                            String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                            if (!"hybridMO".equals(type)) {
                                String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (HybridwsdlStub.HybridMO) HybridwsdlStub.ExtensionMapper.getTypeObject(nsUri, type, reader);
                            }

                        }

                    }

                    Vector handledAttributes = new Vector();

                    reader.next();

                    while (!reader.isEndElement()) {
                        if (reader.isStartElement()) {
                            if ((reader.isStartElement()) && (new QName("", "serviceNumber").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setServiceNumber(ConverterUtil.convertToString(content));

                                reader.next();
                            } else if ((reader.isStartElement()) && (new QName("", "username").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setUsername(ConverterUtil.convertToString(content));

                                reader.next();
                            } else if ((reader.isStartElement()) && (new QName("", "password").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setPassword(ConverterUtil.convertToString(content));

                                reader.next();
                            } else if ((reader.isStartElement()) && (new QName("", "msisdn").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setMsisdn(ConverterUtil.convertToString(content));

                                reader.next();
                            } else if ((reader.isStartElement()) && (new QName("", "contentID").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setContentID(ConverterUtil.convertToString(content));

                                reader.next();
                            } else if ((reader.isStartElement()) && (new QName("", "sgwid").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setSgwid(ConverterUtil.convertToString(content));

                                reader.next();
                            } else {
                                throw new ADBException("Unexpected subelement " + reader.getLocalName());
                            }
                        } else {
                            reader.next();
                        }

                    }

                } catch (XMLStreamException e) {
                    throw new Exception(e);
                }

                return object;
            }
        }
    }

    public static class SendMOResponse
            implements ADBBean, Serializable {

        public static final QName MY_QNAME = new QName("urn:hybridwsdl", "sendMOResponse", "ns1");
        protected HybridwsdlStub.HybridRSP local_return;

        private static String generatePrefix(String namespace) {
            if (namespace.equals("urn:hybridwsdl")) {
                return "ns1";
            }
            return BeanUtil.getUniquePrefix();
        }

        public HybridwsdlStub.HybridRSP get_return() {
            return this.local_return;
        }

        public void set_return(HybridwsdlStub.HybridRSP param) {
            this.local_return = param;
        }

        public static boolean isReaderMTOMAware(XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = Boolean.TRUE.equals(reader.getProperty("IsDatahandlersAwareParsing"));
            } catch (IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        public OMElement getOMElement(QName parentQName, final OMFactory factory)
                throws ADBException {
            OMDataSource dataSource = new ADBDataSource(this, MY_QNAME) {
                public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                    HybridwsdlStub.SendMOResponse.this.serialize(HybridwsdlStub.SendMOResponse.MY_QNAME, factory, xmlWriter);
                }
            };
            return new OMSourcedElementImpl(MY_QNAME, factory, dataSource);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter)
                throws XMLStreamException, ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
                throws XMLStreamException, ADBException {
            String prefix = null;
            String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if ((namespace != null) && (namespace.trim().length() > 0)) {
                String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (serializeType) {
                String namespacePrefix = registerPrefix(xmlWriter, "urn:hybridwsdl");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":sendMOResponse", xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "sendMOResponse", xmlWriter);
                }

            }

            if (this.local_return == null) {
                throw new ADBException("return cannot be null!!");
            }
            this.local_return.serialize(new QName("", "return"), factory, xmlWriter);

            xmlWriter.writeEndElement();
        }

        private void writeAttribute(String prefix, String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        private void writeAttribute(String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        private void writeQNameAttribute(String namespace, String attName, QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String attributeNamespace = qname.getNamespaceURI();
            String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }

            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        private void writeQName(QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + ConverterUtil.convertToString(qname));
                } else {
                    xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(QName[] qnames, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (qnames != null) {
                StringBuffer stringToWrite = new StringBuffer();
                String namespaceURI = null;
                String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        private String registerPrefix(XMLStreamWriter xmlWriter, String namespace)
                throws XMLStreamException {
            String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = BeanUtil.getUniquePrefix();
                }

                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            return prefix;
        }

        public XMLStreamReader getPullParser(QName qName)
                throws ADBException {
            ArrayList elementList = new ArrayList();
            ArrayList attribList = new ArrayList();

            elementList.add(new QName("", "return"));

            if (this.local_return == null) {
                throw new ADBException("return cannot be null!!");
            }
            elementList.add(this.local_return);

            return new ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        public static class Factory {

            public static HybridwsdlStub.SendMOResponse parse(XMLStreamReader reader)
                    throws Exception {
                HybridwsdlStub.SendMOResponse object = new HybridwsdlStub.SendMOResponse();

                String nillableValue = null;
                String prefix = "";
                String namespaceuri = "";
                try {
                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");

                        if (fullTypeName != null) {
                            String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;

                            String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                            if (!"sendMOResponse".equals(type)) {
                                String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (HybridwsdlStub.SendMOResponse) HybridwsdlStub.ExtensionMapper.getTypeObject(nsUri, type, reader);
                            }

                        }

                    }

                    Vector handledAttributes = new Vector();

                    reader.next();

                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if ((reader.isStartElement()) && (new QName("", "return").equals(reader.getName()))) {
                        object.set_return(HybridwsdlStub.HybridRSP.Factory.parse(reader));

                        reader.next();
                    } else {
                        throw new ADBException("Unexpected subelement " + reader.getLocalName());
                    }

                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if (reader.isStartElement()) {
                        throw new ADBException("Unexpected subelement " + reader.getLocalName());
                    }

                } catch (XMLStreamException e) {
                    throw new Exception(e);
                }

                return object;
            }
        }
    }

    public static class ExtensionMapper {

        public static Object getTypeObject(String namespaceURI, String typeName, XMLStreamReader reader)
                throws Exception {
            if (("urn:hybridwsdl".equals(namespaceURI)) && ("hybridRSP".equals(typeName))) {
                return HybridwsdlStub.HybridRSP.Factory.parse(reader);
            }

            if (("urn:hybridwsdl".equals(namespaceURI)) && ("hybridMO".equals(typeName))) {
                return HybridwsdlStub.HybridMO.Factory.parse(reader);
            }

            throw new ADBException("Unsupported type " + namespaceURI + " " + typeName);
        }
    }

    public static class HybridRSP
            implements ADBBean {

        protected String localSTATUS;
        protected String localSID;

        private static String generatePrefix(String namespace) {
            if (namespace.equals("urn:hybridwsdl")) {
                return "ns1";
            }
            return BeanUtil.getUniquePrefix();
        }

        public String getSTATUS() {
            return this.localSTATUS;
        }

        public void setSTATUS(String param) {
            this.localSTATUS = param;
        }

        public String getSID() {
            return this.localSID;
        }

        public void setSID(String param) {
            this.localSID = param;
        }

        public static boolean isReaderMTOMAware(XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = Boolean.TRUE.equals(reader.getProperty("IsDatahandlersAwareParsing"));
            } catch (IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        public OMElement getOMElement(QName parentQName, final OMFactory factory)
                throws ADBException {
            OMDataSource dataSource = new ADBDataSource(this, parentQName) {
                public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                    HybridwsdlStub.HybridRSP.this.serialize(this.parentQName, factory, xmlWriter);
                }
            };
            return new OMSourcedElementImpl(parentQName, factory, dataSource);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter)
                throws XMLStreamException, ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(QName parentQName, OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
                throws XMLStreamException, ADBException {
            String prefix = null;
            String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if ((namespace != null) && (namespace.trim().length() > 0)) {
                String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (serializeType) {
                String namespacePrefix = registerPrefix(xmlWriter, "urn:hybridwsdl");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":hybridRSP", xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "hybridRSP", xmlWriter);
                }

            }

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "STATUS", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "STATUS");
                }
            } else {
                xmlWriter.writeStartElement("STATUS");
            }

            if (this.localSTATUS == null) {
                throw new ADBException("STATUS cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localSTATUS);

            xmlWriter.writeEndElement();

            namespace = "";
            if (!namespace.equals("")) {
                prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    xmlWriter.writeStartElement(prefix, "SID", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                } else {
                    xmlWriter.writeStartElement(namespace, "SID");
                }
            } else {
                xmlWriter.writeStartElement("SID");
            }

            if (this.localSID == null) {
                throw new ADBException("SID cannot be null!!");
            }

            xmlWriter.writeCharacters(this.localSID);

            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement();
        }

        private void writeAttribute(String prefix, String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        private void writeAttribute(String namespace, String attName, String attValue, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        private void writeQNameAttribute(String namespace, String attName, QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String attributeNamespace = qname.getNamespaceURI();
            String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }

            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        private void writeQName(QName qname, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + ConverterUtil.convertToString(qname));
                } else {
                    xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(QName[] qnames, XMLStreamWriter xmlWriter)
                throws XMLStreamException {
            if (qnames != null) {
                StringBuffer stringToWrite = new StringBuffer();
                String namespaceURI = null;
                String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        private String registerPrefix(XMLStreamWriter xmlWriter, String namespace)
                throws XMLStreamException {
            String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = BeanUtil.getUniquePrefix();
                }

                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            return prefix;
        }

        public XMLStreamReader getPullParser(QName qName)
                throws ADBException {
            ArrayList elementList = new ArrayList();
            ArrayList attribList = new ArrayList();

            elementList.add(new QName("", "STATUS"));

            if (this.localSTATUS != null) {
                elementList.add(ConverterUtil.convertToString(this.localSTATUS));
            } else {
                throw new ADBException("STATUS cannot be null!!");
            }

            elementList.add(new QName("", "SID"));

            if (this.localSID != null) {
                elementList.add(ConverterUtil.convertToString(this.localSID));
            } else {
                throw new ADBException("SID cannot be null!!");
            }

            return new ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        public static class Factory {

            public static HybridwsdlStub.HybridRSP parse(XMLStreamReader reader)
                    throws Exception {
                HybridwsdlStub.HybridRSP object = new HybridwsdlStub.HybridRSP();

                String nillableValue = null;
                String prefix = "";
                String namespaceuri = "";
                try {
                    while ((!reader.isStartElement()) && (!reader.isEndElement())) {
                        reader.next();
                    }

                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");

                        if (fullTypeName != null) {
                            String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;

                            String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                            if (!"hybridRSP".equals(type)) {
                                String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (HybridwsdlStub.HybridRSP) HybridwsdlStub.ExtensionMapper.getTypeObject(nsUri, type, reader);
                            }

                        }

                    }

                    Vector handledAttributes = new Vector();

                    reader.next();

                    while (!reader.isEndElement()) {
                        if (reader.isStartElement()) {
                            if ((reader.isStartElement()) && (new QName("", "STATUS").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setSTATUS(ConverterUtil.convertToString(content));

                                reader.next();
                            } else if ((reader.isStartElement()) && (new QName("", "SID").equals(reader.getName()))) {
                                String content = reader.getElementText();

                                object.setSID(ConverterUtil.convertToString(content));

                                reader.next();
                            } else {
                                throw new ADBException("Unexpected subelement " + reader.getLocalName());
                            }
                        } else {
                            reader.next();
                        }

                    }

                } catch (XMLStreamException e) {
                    throw new Exception(e);
                }

                return object;
            }
        }
    }
}