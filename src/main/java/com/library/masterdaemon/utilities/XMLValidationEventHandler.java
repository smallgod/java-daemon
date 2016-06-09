package com.library.masterdaemon.utilities;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import org.slf4j.LoggerFactory;

public class XMLValidationEventHandler implements ValidationEventHandler {

    private final org.slf4j.Logger logger;

    public XMLValidationEventHandler() {
        System.out.println("XMLValidationEventHandler called - system.out.println()");
        logger = LoggerFactory.getLogger(XMLValidationEventHandler.class);
    }

    @Override
    public boolean handleEvent(ValidationEvent event) throws RuntimeException {

        System.out.println("handle event called - system.out.println()");
        logger.debug("handle event called");
        if (event == null) {
            throw new NullPointerException("Error inside handleEvent method, event is NULL");
        }

        int evenSeverity = event.getSeverity();
        ValidationEventLocator vel = event.getLocator();

        System.out.println("EVENT:             " + event.getClass());
        System.out.println("SEVERITY:          " + evenSeverity);
        System.out.println("MESSAGE:           " + event.getMessage());
        System.out.println("LINKED EXCEPTION:  " + event.getLinkedException());
        System.out.println("LOCATOR");
        System.out.println("    LINE NUMBER:   " + vel.getLineNumber());
        System.out.println("    COLUMN NUMBER: " + vel.getColumnNumber());
        System.out.println("    OFFSET:        " + vel.getOffset());
        System.out.println("    OBJECT:        " + vel.getObject());
        System.out.println("    NODE:          " + vel.getNode());
        System.out.println("    URL:           " + vel.getURL());

        if (evenSeverity == ValidationEvent.ERROR || evenSeverity == ValidationEvent.FATAL_ERROR) {
            String error = "XML Validation error:  " + event.getMessage() + " at row: " + vel.getLineNumber() + " and column: " + vel.getColumnNumber();
            logger.error(error);
            return false;
        } else {
            return true;
        }
    }
}
