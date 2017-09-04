package org.springframework.core.util.xml;

import org.springframework.core.util.Assert;
import org.springframework.core.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Created by Administrator on 2017/9/4 0004.
 */
public class StaxStreamXMLReader extends AbstractStaxXMLReader{

    private static final String DEFAULT_XML_VERSION = "1.0";
    private final XMLStreamReader reader ;
    private String xmlVersion = DEFAULT_XML_VERSION;
    private String encoding;

    StaxStreamXMLReader(XMLStreamReader reader){
        Assert.notNull(reader,"'reader' must no be null");
        int event = reader.getEventType();
        if(!(event == XMLStreamConstants.START_DOCUMENT || event == XMLStreamConstants.START_ELEMENT)){
            throw new IllegalStateException("XMLEventReader not at start of document or element");
        }
        this.reader = reader;
    }

    protected void parseInternal() throws SAXException, XMLStreamException {
        boolean documentStarted = false;
        boolean documentEnded = false;
        int elementDepth = 0;
        int eventType = this.reader.getEventType();
        while(true){
            if(eventType != XMLStreamConstants.START_DOCUMENT && eventType != XMLStreamConstants.END_DOCUMENT && !documentStarted){
                handleStartDocument();
                documentStarted = true;
            }
            switch(eventType){
                case XMLStreamConstants.START_ELEMENT:
                elementDepth++;
                handleStartElement();
                break;

                case XMLStreamConstants.END_ELEMENT:
                    elementDepth--;
                    if(elementDepth >= 0){
                        handleEndElement();
                    }
                    break;

                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    handleProcessingInstruction();
                    break;

                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                    handleCharacters();
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    handleStartDocument();
                    documentStarted = true;
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    handleEndDocument();
                    documentEnded = true;
                    break;
                case XMLStreamConstants.COMMENT:
                    handleComment();
                    break;
                case XMLStreamConstants.DTD:
                    handleDtd();
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    handleEntityReference();
                    break;
            }
            if(this.reader.hasNext() && elementDepth >= 0){
                eventType = this.reader.next();
            }
            else{
                break;
            }
        }
        if(!documentEnded){
            handleEndDocument();
        }
    }

    private void handleStartDocument() throws SAXException{
        if(XMLStreamConstants.START_DOCUMENT == this.reader.getEventType()){
            String xmlVersion = this.reader.getVersion();
            if(StringUtils.hasLength(xmlVersion)){
                this.xmlVersion = xmlVersion;
            }
            this.encoding = this.reader.getCharacterEncodingScheme();
        }
        if(getContentHandler() != null){
            final Location location = this.reader.getLocation();
            getContentHandler().setDocumentLocator(new Locator2(){

                @Override
                public String getPublicId() {
                    return (location != null ? location.getPublicId():null);
                }

                @Override
                public String getSystemId() {
                    return (location != null ? location.getSystemId(): null);
                }

                @Override
                public int getLineNumber() {
                    return (location != null ? location.getLineNumber(): -1);
                }

                @Override
                public int getColumnNumber() {
                    return (location != null ? location.getColumnNumber(): -1);
                }

                @Override
                public String getXMLVersion() {
                    return xmlVersion;
                }

                @Override
                public String getEncoding() {
                    return encoding;
                }
            });
            getContentHandler().startDocument();
            if(this.reader.standaloneSet()){
                setStandalone(this.reader.isStandalone());
            }
        }
    }

    private void handleStartElement() throws SAXException{
        if(getContentHandler()!= null){
            QName qName = this.reader.getName();
            if(hasNamespaceFeature()){
                for(int i = 0; i < this.reader.getNamespaceCount(); i++){
                    startPrefixMapping(this.reader.getNamespacePrefix(i),this.reader.getNamespaceURI(i));
                }
                for(int i =0; i < this.reader.getAttributeCount(); i++){
                    String prefix = this.reader.getAttributePrefix(i);
                    String namespace = this.reader.getAttributeNamespace(i);
                    if(StringUtils.hasLength(namespace)){
                        startPrefixMapping(prefix,namespace);
                    }
                }
                getContentHandler().startElement(qName.getNamespaceURI(),qName.getLocalPart(),toQualifiedname(qName),getAttributes());
            }
            else{
                getContentHandler().startElement("","",toQualifiedname(qName),getAttributes());
            }
        }
    }

    private void handleEndElement() throws SAXException{
        if(getContentHandler() != null){
            QName qName = this.reader.getName();
            if(hasNamespacesFeature()){
                getContentHandler().endElement(qName.getNamespaceURI(),qName.getLocalPart(),toQualifiedname(qName));
                for(int i = 0; i < this.reader.getNamespaceCount() ; i++){
                    String prefix = this.reader.getNamespacePrefix(i);
                    if(prefix == null){
                        prefix = "";
                    }
                    endPrefixMapping(prefix);
                }
            }
            else{
                getContentHandler().endElement("","",toQualifiedname(qName));
            }
        }
    }

    private  void handleCharacters() throws SAXException{
        if(XMLStreamConstants.CDATA == this.reader.getEventType() && getLexicalhandler() != null){
            getLexicalhandler().startCDATA();
        }
        if(getContentHandler()!=null){
            getContentHandler().characters(this.reader.getTextCharacters(),this.reader.getTextStart(),this.reader.getTextLength());
        }
        if(XMLStreamConstants.CDATA == this.reader.getEventType() && getLexicalhandler()!= null){
            getLexicalhandler().endCDATA();
        }
    }

    private void handleComment() throws SAXException{
        if(getLexicalhandler() != null){
            getLexicalhandler().comment(this.reader.getTextCharacters(),this.reader.getTextStart(),this.reader.getTextLength());
        }
    }

    private void handleDtd() throws SAXException{
        if(getLexicalhandler() != null){
            javax.xml.stream.Location location = this.reader.getLocation();
            getLexicalhandler().startDTD(null,location.getPublicId(),location.getSystemId());
        }
        if(getLexicalhandler() != null){
            getLexicalhandler().endDTD();
        }
    }

    private void handleEntityReference() throws SAXException{
        if(getLexicalhandler() != null){
            getLexicalhandler().startEntity(this.reader.getLocalName());
        }
        if(getLexicalhandler() != null){
            getLexicalhandler().endEntity(this.reader.getLocalName());
        }
    }

    private void handleEndDocument() throws SAXException{
        if(getContentHandler() != null){
            getContentHandler().endDocument();
        }
    }

    private void handleProcessingInstruction() throws SAXException{
        if(getContentHandler() != null){
            getContentHandler().processingInstruction(this.reader.getPITarget(),this.reader.getPIData());
        }
    }

    private Attributes getAttributes(){
        AttributesImpl attributes = new AttributesImpl();
        for(int i = 0; i < this.reader.getAttributeCount(); i++){
            String namespace = this.reader.getAttributeNamespace(i);
            if(namespace == null || !hasNamespaceFeature()){
                namespace = "";
            }
            String type = this.reader.getAttributeType(i);
            if(type == null){
                type = "CDATA";
            }
            attributes.addAttribute(namespace,this.reader.getAttributeLocalName(i),
                    toQualifiedname(this.reader.getAttributeName(i)),type,this.reader.getAttributeValue(i));
        }
        if(hasNamespaceFeature()){
            for(int i = 0; i < this.reader.getNamespaceCount();i++ ){
                String prefix = this.reader.getNamespacePrefix(i);
                String namespaceUri = this.reader.getNamespaceURI(i);
                String qName;
                if(StringUtils.hasLength(prefix)){
                    qName = "xmlns:" + prefix;
                }
                else{
                    qName = "xmlns";
                }
                attributes.addAttribute("","",qName,"CDATA",namespaceUri);
            }
        }
       return attributes;
    }


    @Override
    public void setDTDHandler(DTDHandler handler) {

    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }
}
