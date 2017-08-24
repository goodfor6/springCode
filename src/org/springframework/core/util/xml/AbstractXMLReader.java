package org.springframework.core.util.xml;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

/**
 * Created by Administrator on 2017/8/24 0024.
 */
abstract class AbstractXMLReader implements XMLReader {
    private DTDHandler dtdHandler;
    private ContentHandler contentHandler;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private LexicalHandler lexicalhandler;

    AbstractXMLReader(){}

    public DTDHandler getDtdHandler() {
        return dtdHandler;
    }

    public void setDtdHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    @Override
    public ContentHandler getContentHandler( ) {
        return contentHandler;
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public LexicalHandler getLexicalhandler() {
        return lexicalhandler;
    }

    public void setLexicalhandler(LexicalHandler lexicalhandler) {
        this.lexicalhandler = lexicalhandler;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,SAXNotSupportedException{
        throw new SAXNotRecognizedException(name);
    }

    public void setFeature(String name,boolean value) throws SAXNotRecognizedException, SAXNotSupportedException{
        throw new SAXNotRecognizedException(name);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException{
        if("http://xml.org/sax/properties/lexical-handler".equals(name)){
            return this.lexicalhandler;
        }else{
            throw new SAXNotRecognizedException(name);
        }
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException{
        if("http://xml.org/sax/properties/lexical-handler".equals(name)){
            this.lexicalhandler = (LexicalHandler)value;
        }
        else{
            throw new SAXNotRecognizedException(name);
        }
    }

}
