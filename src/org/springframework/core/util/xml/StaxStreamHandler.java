package org.springframework.core.util.xml;

import jdk.internal.org.xml.sax.Locator;
import org.springframework.core.util.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/1 0001.
 */
class StaxStreamHandler extends AbstractStaxHandler {

    private final XMLStreamWriter streamWriter;

    public StaxStreamHandler(XMLStreamWriter streamWirter) {
        Assert.notNull(streamWirter, "XMLStreamWriter must not be null");
        this.streamWriter = streamWirter;
    }

    protected void startDocumentInternal() throws XMLStreamException {
        this.streamWriter.writeStartDocument();
    }

    protected void endDocumentInternal() throws XMLStreamException{
        this.streamWriter.writeEndDocument();
    }

    protected void startElementInternal(QName name, Attributes attributes,Map<String, String> namespaceMapping)throws XMLStreamException{
        this.streamWriter.writeStartElement(name.getPrefix(),name.getLocalPart(),name.getNamespaceURI());
        for(Map.Entry<String,String> entry : namespaceMapping.entrySet()){
            String prefix = entry.getKey();
            String namespaceUri = entry.getValue();
            this.streamWriter.writeNamespace(prefix, namespaceUri);
            if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)){
                this.streamWriter.setDefaultNamespace(namespaceUri);
            }
            else{
                this.streamWriter.setPrefix(prefix, namespaceUri);
            }
        }
        for(int i = 0; i < attributes.getLength(); i++){
            QName attrName = toQName(attributes.getURI(i), attributes.getQName(i));
            if(!isNamespaceDeclaration(attrName)){
                this.streamWriter.writeAttribute(attrName.getPrefix(),attrName.getNamespaceURI(), attrName.getLocalPart(),attributes.getValue(i));
            }
        }
    }

    protected void endElementInternal(QName name,Map<String, String>namespaceMapping)throws XMLStreamException{
        this.streamWriter.writeEndElement();
    }

    protected void charactersInternal(String data)throws XMLStreamException{
        this.streamWriter.writeCharacters(data);
    }

    protected void cDataInternal(String data)throws XMLStreamException{
        this.streamWriter.writeCData(data);
    }

    protected void ignorableWhitespaceInternal(String data)throws XMLStreamException{
        this.streamWriter.writeCharacters(data);
    }



    protected void processingInstructionInternal(String target, String data)throws XMLStreamException{
        this.streamWriter.writeProcessingInstruction(target,data);
    }

    protected void dtdInternal(String dtd)throws XMLStreamException{
        this.streamWriter.writeDTD(dtd);
    }

    protected void commentInternal(String comment)throws XMLStreamException{
        this.streamWriter.writeComment(comment);
    }

    public void setDocumentLocator(Locator locator){
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {

    }

    public void startEntity(String name) throws SAXException {}

    public void endEntity(String name)throws SAXException{}

    @Override
    public void endCDATA() throws SAXException {

    }

    protected void skippedEntityInternal(String name)throws XMLStreamException{}

    @Override
    public void setDocumentLocator(org.xml.sax.Locator locator) {

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

    }
}
