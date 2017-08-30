package org.springframework.core.util.xml;

import org.springframework.core.util.Assert;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

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

    protected void paseInternal() throws SAXException, XMLStreamException{
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
                    elementDepth--;
                    if(elementDepth >= 0){
                        handleEndElement(event.asEndElement());
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:



            }
        }
    }

}
