package org.springframework.core.util.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.sax.SAXResult;

/**
 * Created by Administrator on 2017/9/1 0001.
 */
 class StaxResult extends SAXResult {

     private XMLEventWriter eventWriter;
     private XMLStreamWriter streamWriter;

     public StaxResult(XMLStreamWriter streamWriter){
         StaxStreamHandler handler = new StaxStreamHandler(streamWriter);
         super.setHandler(handler);
         super.setLexicalHandler(handler);
         this.streamWriter = streamWriter;
     }

     public StaxResult(XMLEventWriter eventWriter){
         StaxEventHandler handler = new StaxEventHandler(eventWriter);
         super.setHandler(handler);
         super.setLexicalHandler(handler);
         this.eventWriter = eventWriter;
     }

     public XMLEventWriter getXMLEventWriter(){return this.eventWriter;}

     public XMLStreamWriter getXMLStreamWriter(){return this.streamWriter;}

     public void setHandler(ContentHandler handler){
         throw new UnsupportedOperationException("setHandler is not supported");
     }

     public void setLexicalHandler(LexicalHandler handler){
         throw new UnsupportedOperationException("setLexicalHandler is not supported");
     }

 }
