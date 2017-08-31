package org.springframework.core.util.xml;

import com.sun.xml.internal.stream.events.AttributeImpl;
import org.springframework.core.util.Assert;
import org.springframework.core.util.StringUtils;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;

import javax.tools.DocumentationTool;
import javax.xml.stream.events.StartElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import javax.xml.stream.Location;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/8/30 0030.
 */
public class StaxEventXMLReader extends AbstractStaxXMLReader{

    private static final String DEFAULT_XML_VERSION = "1.0";

    private final XMLEventReader reader;

    private String xmlVersion = DEFAULT_XML_VERSION;

    private String encoding;

    StaxEventXMLReader(XMLEventReader reader){
        Assert.notNull(reader,"reader must not be null");
        try{
            XMLEvent event = reader.peek();
           if( event != null && !(event.isStartDocument() || event.isStartElement())){
               throw new IllegalStateException("XMLEventReader not at start of document or element");
            }
        }
        catch(XMLStreamException ex){
            throw new IllegalStateException("XMLEventReader not at start of document or element");
        }
        this.reader = reader;
    }

    protected void parseInternal() throws SAXException, XMLStreamException{
        boolean documentStarted = false;
        boolean documentEnded = false;
        int elementDepth = 0;
        while(this.reader.hasNext() && elementDepth >= 0){
            XMLEvent event = this.reader.nextEvent();
            if(!event.isStartDocument() && !event.isEndDocument() && !documentStarted){
                handleStartDocument(event);
                documentStarted = true;
            }
            switch(event.getEventType()){
                case XMLStreamConstants.START_DOCUMENT:
                handleStartDocument(event);
                documentStarted = true;
                break;

                case XMLStreamConstants.START_ELEMENT:
                    elementDepth++;
                    handleEndElement(event.asEndElement());
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    elementDepth--;
                    if(elementDepth >= 0){
                        handleEndElement(event.asEndElement());
                    }
                    break;

                case   XMLStreamConstants.PROCESSING_INSTRUCTION:
                    handleProcessingInstruction((ProcessingInstruction)event);
                    break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                     handleCharacters(event.asCharacters());
                     break;
                case XMLStreamConstants.END_DOCUMENT:
                    handleEndDocument();
                    documentEnded = true;
                    break;
                case XMLStreamConstants.NOTATION_DECLARATION:
                    handleNotationDeclaration((NotationDeclaration)event);
                    break;
                case XMLStreamConstants.ENTITY_DECLARATION:
                    handleEntityDeclaration((EntityDeclaration)event);
                    break;
                case XMLStreamConstants.COMMENT:
                    handleComment((Comment)event);
                    break;
                case XMLStreamConstants.DTD:
                    handleDtd((DTD) event);
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    handleEntityReference((EntityReference)event);
                    break;
            }
        }
        if(documentStarted && !documentEnded){
            handleEndDocument();
        }
    }

    private void handleStartDocument(final XMLEvent event)throws SAXException{
        if(event.isStartDocument()){
            StartDocument startDocument = (StartDocument) event;
            String xmlVersion = startDocument.getVersion();
            if(StringUtils.hasLength(xmlVersion)){
                this.xmlVersion = xmlVersion;
            }
            if(startDocument.encodingSet()){
                this.encoding = startDocument.getCharacterEncodingScheme();
            }
        }
        if(getContentHandler()!= null){
            final Location location = event.getLocation();
            getContentHandler().setDocumentLocator(new Locator2(){

                @Override
                public String getPublicId() {
                    return (location != null ? location.getPublicId() : null);
                }

                @Override
                public String getSystemId() {
                    return (location != null ? location.getSystemId() : null);
                }

                @Override
                public int getLineNumber() {
                    return (location != null ? location.getLineNumber() : -1);
                }

                @Override
                public int getColumnNumber() {
                    return (location != null? location.getColumnNumber() : -1);
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
            getContentHandler();
        }
    }

    private void handleStartElement(StartElement startElement)throws SAXException{
        if(getContentHandler() != null){
            QName qName = startElement.getName();
            if(hasNamespaceFeature()){
                for(Iterator i =  startElement.getNamespaces();i.hasNext();){
                    Namespace namespace = (Namespace)i.next();
                    startPrefixMapping(namespace.getPrefix(),namespace.getNamespaceURI());
                }
                for(Iterator i = startElement.getAttributes();i.hasNext();){
                    Attribute attribute = (Attribute)i.next();
                    QName attributeName = attribute.getName();
                    startPrefixMapping(attributeName.getPrefix(),attributeName.getNamespaceURI());
                }

                getContentHandler().startElement(qName.getNamespaceURI(),qName.getLocalPart(),toQualifiedname(qName),getAttributes(startElement));
            }
            else{
                getContentHandler().startElement("","",toQualifiedname(qName),getAttributes(startElement));
            }
        }
    }

    private void handleCharacters(Characters characters)throws SAXException{
        char[] data = characters.getData().toCharArray();
        if(getContentHandler() != null && characters.isIgnorableWhiteSpace()){
            getContentHandler().ignorableWhitespace(data,0,data.length);
            return ;
        }
        if(characters.isCData() && getLexicalhandler() != null){
            getLexicalhandler().startCDATA();
        }
        if(getContentHandler() != null){
            getContentHandler().characters(data,0,data.length);
        }
        if(characters.isCData() && getLexicalhandler() != null){
            getLexicalhandler().endCDATA();
        }
    }

    private void handleEndElement(EndElement endElement)throws SAXException{
        if(getContentHandler() != null){
            QName qName = endElement.getName();
            if(hasNamespaceFeature()){
                getContentHandler().endElement(qName.getNamespaceURI(),qName.getLocalPart(),toQualifiedname(qName));
                for(Iterator i = endElement.getNamespaces();i.hasNext();){
                    Namespace namespace = (Namespace)i.next();
                    endPrefixMapping(namespace.getPrefix());
                }
            }
            else{
                getContentHandler().endElement("","",toQualifiedname(qName));
            }
        }
    }

    private void handleEndDocument() throws SAXException{
        if(getContentHandler() != null){
            getContentHandler().endDocument();
        }
    }

    private void handleNotationDeclaration(NotationDeclaration declaration)throws SAXException{
        if(getDTDHandler() != null){
            getDTDHandler().notationDecl(declaration.getName(),declaration.getPublicId(),declaration.getSystemId());
        }
    }

    private void handleEntityDeclaration(EntityDeclaration entityDeclaration)throws SAXException{
        if(getDTDHandler() != null){
            getDTDHandler().unparsedEntityDecl(entityDeclaration.getName(),entityDeclaration.getPublicId(),
                    entityDeclaration.getSystemId(),entityDeclaration.getNotationName());
        }
    }

    private void handleProcessingInstruction (ProcessingInstruction pi)throws SAXException{
        if(getContentHandler() != null){
            getContentHandler().processingInstruction(pi.getTarget(),pi.getData());
        }
    }

    private void handleComment(Comment comment)throws SAXException{
        if(getLexicalhandler() != null){
            char[] ch = comment.getText().toCharArray();
            getLexicalhandler().comment(ch,0,ch.length);
        }
    }

    private void handleDtd(DTD dtd)throws SAXException{
        if(getLexicalhandler() != null){
            javax.xml.stream.Location location = dtd.getLocation();
            getLexicalhandler().startDTD(null,location.getPublicId(),location.getSystemId());
        }
        if(getLexicalhandler() != null){
            getLexicalhandler().endDTD();
        }
    }

    private void handleEntityReference(EntityReference reference) throws SAXException{
        if(getLexicalhandler() != null){
            getLexicalhandler().startEntity(reference.getName());
        }
        if(getLexicalhandler() != null){
            getLexicalhandler().endEntity(reference.getName());
        }
    }

    private Attributes getAttributes(StartElement event){
        AttributesImpl attributes = new AttributesImpl();
        for(Iterator i = event.getAttributes(); i.hasNext();){
            Attribute attribute = (Attribute)i.next();
            QName qName = attribute.getName();
            String namespace = qName.getNamespaceURI();
            if(namespace == null || !hasNamespaceFeature()){
                namespace = "";
            }
            String type = attribute.getDTDType();
            if(type == null){
                type = "CDATA";
            }
            attributes.addAttribute(namespace,qName.getLocalPart(),toQualifiedname(qName),type,attribute.getValue());
        }
        if(hasNamespacePrefixesFeature()){
            for(Iterator i = event.getNamespaces();i.hasNext();){
                Namespace namespace = (Namespace)i.next();
                String prefix = namespace.getPrefix();
                String namespaceUri = namespace.getNamespaceURI();
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
