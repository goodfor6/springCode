package org.springframework.core.util.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

/**
 * Created by Administrator on 2017/9/5 0005.
 */
public abstract  class createStaxSource {

    public static Source createStaxSource(XMLStreamReader streamReader){return new StAXSource(streamReader);}

    public static Source createStaxSource(XMLEventReader eventReader)throws XMLStreamException {return new StAXSource(eventReader);}

    public static Source createCustomStaxSource(XMLStreamReader streamReader){return new StaxSource(streamReader);}

    public static Source createCustomStaxSource(XMLEventReader eventReader){return new StaxSource(eventReader);}

    public static boolean isStaxSource(Source source){return (source instanceof StAXSource || source instanceof StaxSource);}

    public static XMLStreamReader getXMLStreamReader(Source source){
        if(source instanceof StAXSource){
            return ((StAXSource)source).getXMLStreamReader();
        }
        else if(source instanceof StaxSource){
            return ((StaxSource)source).getXMLStreamReader();
        }
        else{
            throw new IllegalArgumentException("Source '" +source+"'is neither StaxSource nor StAXSource");
        }
    }

    public static XMLEventReader getXMLEventReader(Source source){
        if(source instanceof StAXSource){
            return ((StAXSource)source).getXMLEventReader();
        }
        else if(source instanceof StaxSource){
            return ((StaxSource)source).getXMLEventReader();
        }
        else{
            throw new IllegalArgumentException("Source'" + source+" ' is neither StaxSource nor StAXSource");
        }
    }

    public static Result createStaxResult(XMLStreamWriter streamWriter){return new StAXResult(streamWriter);}

}
