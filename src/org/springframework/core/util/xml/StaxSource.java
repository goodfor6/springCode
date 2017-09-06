package org.springframework.core.util.xml;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

/**
 * Created by Administrator on 2017/9/5 0005.
 */
class StaxSource  extends SAXSource {

    private XMLEventReader eventReader;

    private XMLStreamReader streamReader;

    StaxSource(XMLStreamReader streamReader){
        super(new StaxStreamXMLReader(streamReader),new InputSource());
        this.streamReader = streamReader;
    }

    StaxSource(XMLEventReader eventReader){
        super(new StaxEventXMLReader(eventReader),new InputSource());
        this.eventReader = eventReader;
    }

    XMLEventReader getXMLEventReader(){return this.eventReader;}

    XMLStreamReader getXMLStreamReader(){return this.streamReader;}

    public void setInputSource(InputSource inputSource){
        throw new UnsupportedOperationException("setInputSource is not supported");
    }

    public void setXMLReader(XMLReader reader){
        throw new UnsupportedOperationException("setXMLReader is not supported");
    }
}
