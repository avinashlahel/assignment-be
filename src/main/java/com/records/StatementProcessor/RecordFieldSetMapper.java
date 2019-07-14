package com.records.StatementProcessor;

import com.records.StatementProcessor.model.TransactionRecord;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.math.RoundingMode;

/**
 * This class has been written to account for any empty fields
 * encountered while reading the file. Incase of an empty field
 * a BindException is thrown which is then handled in
 * FileVerificationSkipper. The processing continues only to stop
 * if the skipCounts are more than 5
 * @author avi
 */
public class RecordFieldSetMapper implements FieldSetMapper<TransactionRecord> {

    @Override
    public TransactionRecord mapFieldSet(FieldSet fs) throws BindException {
        TransactionRecord record = new TransactionRecord();

        //reference number
        if(fs.readString("reference").isEmpty()) throw new BindException(fs,"reference");
        else record.setReference(fs.readInt("reference"));

        //account number
        if(fs.readString("accountNumber").isEmpty()) throw new BindException(fs,"accountNumber");
        else record.setAccountNumber(fs.readString("accountNumber"));

        //reference number
        if(fs.readString("description").isEmpty()) throw new BindException(fs,"description");
        else record.setDescription(fs.readString("description"));

        //start balance
        if(fs.readString("startBalance").isEmpty()) throw new BindException(fs,"startBalance");
        else record.setStartBalance(fs.readBigDecimal("startBalance").setScale(2, RoundingMode.HALF_EVEN));

        //mutation
        if(fs.readString("mutation").isEmpty()) throw new BindException(fs,"mutation");
        else record.setMutation(fs.readBigDecimal("mutation").setScale(2, RoundingMode.HALF_EVEN));

        //endBalance
        if(fs.readString("endBalance").isEmpty()) throw new BindException(fs,"endBalance");
        else record.setEndBalance(fs.readBigDecimal("endBalance").setScale(2, RoundingMode.HALF_EVEN));

        return record;
    }
}
