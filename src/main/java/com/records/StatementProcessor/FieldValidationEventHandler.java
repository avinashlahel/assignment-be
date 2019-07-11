package com.records.StatementProcessor;

import com.records.StatementProcessor.exception.EmptyFieldException;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 * This class is an event handler for Jaxb2Marshaller
 * which is used in BatchConfig.java. This callback is
 * called when there is an exception thrown while unmarshalling
 * an xml. The EmptyFieldException is thrown from the various
 * XmlAdapter classes
 *
 * @author avi
 */
public class FieldValidationEventHandler implements ValidationEventHandler {

    @Override
    public boolean handleEvent(ValidationEvent event) {
        if (event.getMessage().contains("com.records.StatementProcessor.exception.EmptyFieldException"))
            throw new EmptyFieldException("XML Record found with an empty field at line number :" +
                    event.getLocator().getLineNumber());
        else
            return true;
    }
}
