package org.springframework.core.util.xml;

import org.apache.commons.logging.Log;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;

public class SimpleTransformErrorListener implements ErrorHandler {

    private final Log logger;


    public SimpleTransformErrorListener(Log logger) {
        this.logger = logger;
    }

    public void warning(TransformerException ex) throws TransformerException {
        logger.warn("XSLT transformation warning", ex);
    }

    public void error(TransformerException ex) throws TransformerException {
        logger.error("XSLT transformation error", ex);
    }

    public void fatalError(TransformerException ex) throws TransformerException {
        throw ex;
    }

    public void warning(SAXParseException exception) throws SAXException {

    }

    @Override
    public void error(SAXParseException exception) throws SAXException {

    }


    public void fatalError(SAXParseException exception) throws SAXException {

    }
}
