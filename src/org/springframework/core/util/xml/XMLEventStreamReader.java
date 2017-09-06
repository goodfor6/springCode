package org.springframework.core.util.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/9/6 0006.
 */
class XMLEventStreamReader extends AbstractXMLStreamReader {

    private XMLEvent event;

    private final XMLEventReader eventReader;

    public XMLEventStreamReader(XMLEventReader eventReader)throws XMLStreamException {
        this.eventReader = eventReader;
        this.event = eventReader.nextEvent();
    }

    public QName getName(){
        if(this.event.isStartElement()){
            return this.event.asStartElement().getName();
        }
        else if (this.event.isEndElement()){
            return this.event.asEndElement().getName();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public Location getLocation(){return this.event.getLocation();}

    public int getEventType(){return this.event.getEventType();}

    public String getVersion(){
        if(this.event.isStartDocument()){
            return ((StartDocument)this.event).getVersion();
        }
        else{
            return null;
        }
    }

    public Object getProperty(String name)throws IllegalArgumentException{
        return this.eventReader.getProperty(name);
    }

    public boolean isStandalone(){
        if(this.event.isStartDocument()){
            return ((StartDocument)event).isStandalone();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public boolean standaloneSet(){
        if(this.event.isStartDocument()){
            return ((StartDocument)this.event).standaloneSet();
        }
        else{
            throw new IllegalStateException();
        }
    }

    @Override
    public String getCharacterEncodingScheme() {
        return null;
    }

    public String getEncoding(){return null;}

    public String getPITarget(){
        if(this.event.isProcessingInstruction()){
            return ((ProcessingInstruction)this.event).getTarget();
        }
        else {
            throw new IllegalStateException();
        }
    }

    public String getPIData(){
        if(this.event.isProcessingInstruction()){
            return ((ProcessingInstruction)this.event).getData();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public int getTextStart(){return 0;}

    public String getText(){
        if(this.event.isCharacters()){
            return event.asCharacters().getData();
        }
        else if(this.event.getEventType() == XMLEvent.COMMENT){
            return ((Comment)this.event).getText();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public int getAttributeCount(){
        if(!this.event.isStartElement()){
            throw new IllegalStateException();
        }
        Iterator  attributes = this.event.asStartElement().getAttributes();
        return countIterator(attributes);
    }

    public boolean isAttributeSpecified(int index){return getAttribute(index).isSpecified();}

    public QName getAttributeName(int index){return getAttribute(index).getName();}

    public String getAttributeType(int index){return getAttribute(index).getDTDType();}

    public String getAttributeValue(int index){return getAttribute(index).getValue();}

    private Attribute getAttribute(int index){
        if(!this.event.isStartElement()){
            throw new IllegalStateException();
        }
        int count = 0;
        Iterator attributes = this.event.asStartElement().getAttributes();
        while(attributes.hasNext()){
            Attribute attribute = (Attribute)attributes.next();
            if(count == index){
                return attribute;
            }
            else{
                count++;
            }
        }
        throw new IllegalArgumentException();
    }

    public NamespaceContext getNamespaceContext(){
        if(this.event.isStartElement()){
            return this.event.asStartElement().getNamespaceContext();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public int getNamespaceCount(){
        Iterator namespaces;
        if(this.event.isStartElement()){
            namespaces = this.event.asStartElement().getNamespaces();
        }
        else if(this.event.isEndElement()){
            namespaces = this.event.asEndElement().getNamespaces();
        }
        else{
            throw new IllegalStateException();
        }
        return countIterator(namespaces);
    }

    public String getNamespacePrefix(int index){return getNamespace(index).getPrefix();}

    public String getNamespaceURI(int index){return getNamespace(index).getNamespaceURI();}


    private Namespace getNamespace(int index){
        Iterator namespaces;
        if(this.event.isStartElement()){
            namespaces = this.event.asStartElement().getNamespaces();
        }
        else if(this.event.isEndElement()){
            namespaces = this.event.asEndElement().getNamespaces();
        }
        else{
            throw new IllegalStateException();
        }

        int count = 0;
        while(namespaces.hasNext()){
            Namespace namespace = (Namespace)namespaces.next();
            if(count == index){
                return namespace;
            }
            else {
                count++;
            }
        }
       throw new IllegalArgumentException();
    }

    public int next() throws XMLStreamException{
        this.event = this.eventReader.nextEvent();
        return this.event.getEventType();
    }

    public void close() throws XMLStreamException{
        this.eventReader.close();
    }
    private static int countIterator(Iterator iterator){
        int count = 0;
        while(iterator.hasNext()){
            iterator.next();
            count++;
        }
        return count;
    }

}
