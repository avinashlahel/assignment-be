package com.records.StatementProcessor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.FileNotFoundException;

public class FileVerificationSkipper implements SkipPolicy {

    private static Logger log = LoggerFactory.getLogger(FileVerificationSkipper.class);

    @Override
    public boolean shouldSkip(Throwable exception, int skipCount) throws SkipLimitExceededException {
        if(exception instanceof FileNotFoundException)
            return false;
        else if(exception instanceof FlatFileParseException && skipCount <=5) {
            FlatFileParseException parseException = (FlatFileParseException) exception;
            StringBuffer buffer = new StringBuffer();
            buffer.append("Error occurred while processing line ").append(parseException.getLineNumber());
            buffer.append("\n Below is the faulty input line \n").append(parseException.getInput());
            log.error("{}",buffer.toString());
            return true;
        }
        return true;
    }
}
