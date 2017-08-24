package org.springframework.core.util.xml;


import org.springframework.core.util.StringUtils;

import org.xml.sax.*;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/23 0023.
 */
abstract class AbstractStaxXMLReader extends AbstractXMLReader {

    private static final String NAMESPACES_FEATURE_NAME = "http://xml.org/sax/features/namespaces";
    private static final String NAMESPACE_PREFIEXS_FEATURE_NAME = "http://xml.org/sax/feature/namespace-prefixes";
    private static final String IS_STANDALONE_FEATURE_NAME = "htttp://xml.org/sax/features/is-standalone";
    private static boolean namespacesFeature = true;
    private static boolean namespacePrefixesFeature = false;
    private Boolean isStandalone;
    private final Map<String, String> namespaces = new LinkedHashMap();

    AbstractStaxXMLReader(){}

    public boolean getFeature(String name)throws SAXNotRecognizedException,SAXNotSupportedException {
        if("http://xml.org/sax/feature/namespaces".equals(name)){
            return this.namespacesFeature;
        }
        else if("http://xml.org/sax/feature/namespace-prefixes".equals(name)){
            return this.namespacePrefixesFeature;
        }
        else if("http://xml.org/sax/features/is-standalone".equals(name)){
            if(this.isStandalone != null){
                return this.isStandalone.booleanValue();
            }
            else{
                throw new SAXNotSupportedException("startDocument() callback not completed yet");
            }
        }
        else{
            return super.getFeature(name);
        }
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/features/namespaces".equals(name)) {
            this.namespacesFeature = value;
        } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
            this.namespacePrefixesFeature = value;
        }else{
            super.setFeature(name, value);
        }
    }

    protected void setStandalone(boolean standalone){ this.isStandalone = Boolean.valueOf(standalone);}

    protected boolean hasNamespaceFeature(){return this.namespacesFeature;}

    protected boolean hasNamespacePrefixesFeature(){return this.namespacePrefixesFeature;}

    protected String toQualifiedname(QName qName){
        String prefix = qName.getPrefix();
        return !StringUtils.hasLength(prefix)?qName.getLocalPart(): prefix +":"+qName.getLocalPart();
    }

    public final void parse(InputSource ignored) throws SAXException {
        this.parse();
    }

    public final void parse(String ignored )throws SAXException{
        this.parse();
    }

    private void parse() throws SAXException{
        try{
            this.parseInternal();
        }catch(XMLStreamException var4){
            Locator locator = null;
            if(var4.getLocation() != null){
                locator = new AbstractStaxXMLReader.StaxLocator(var4.getLocation());
            }

            SAXParseException saxException = new SAXParseException(var4.getMessage(), locator, var4);
            if(this.getErrorHandler() == null){
                throw saxException;
            }

            this.getErrorHandler().fatalError(saxException);
        }
    }

    protected abstract void parseInternal() throws SAXException, XMLStreamException;

    protected void startPrefixMapping(String prefix, String namespace) throws SAXException{
        if(this.getContentHandler() != null){
            if(prefix == null){
                prefix = "";
            }

            if(!StringUtils.hasLength(namespace)){
                return ;
            }

            if(!namespace.equals(this.namespaces.get(prefix))){
                this.getContentHandler().startPrefixMapping(prefix,namespace);
                this.namespaces.put(prefix, namespace);
            }
        }
    }

    protected void endPrefixMapping(String prefix)throws SAXException{
        if(this.getContentHandler() != null && this.namespaces.containsKey(prefix)){
            this.getContentHandler().endPrefixMapping(prefix);
            this.namespaces.remove(prefix);
        }
    }

    private static class StaxLocator implements Locator{
        private Location location;

        protected StaxLocator(Location location){ this.location = location;}
        public String getPublicId(){return this.location.getPublicId();}
        public String getSystemId(){ return this.location.getSystemId();}
        public int getLineNumber(){return this.location.getLineNumber();}
        public int getColumnNumber() { return this.location.getColumnNumber();}

    }

}
