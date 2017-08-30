package org.springframework.core.util.xml;

import org.apache.commons.logging.Log;
import org.xml.sax.ErrorHandler;

import javax.xml.transform.TransformerException;

public class SimpleTransformErrorListener implements ErrorHandler {

    private final Log logger;

    @Override
    public void warning(TransformerException ex) throws TransformerException {
        logger.warn("XSLT transformation warning", ex);
    }

    @Override
    public void error(TransformerException ex) throws TransformerException {
        logger.error("XSLT transformation error", ex);
    }

    @Override
    public void fatalError(TransformerException ex) throws TransformerException {
        throw ex;
    }
}
