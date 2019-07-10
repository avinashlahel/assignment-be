package com.records.StatementProcessor.batch;

import com.records.StatementProcessor.model.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;

/**
 * @author avi
 * Implements itemProcessor, used in Batch Config
 */
@Component
public class TransactionRecordProcessor implements ItemProcessor<TransactionRecord, TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionRecord.class);

    public final HashSet<TransactionRecord> uniqueRecords = new HashSet<>();

    /**
     * Entrypoint for the processor to go through all records
     * @param transactionRecord
     * @return
     * @throws Exception
     */
    @Override
    public TransactionRecord process(TransactionRecord transactionRecord) throws Exception {
        log.info("Processing Record {}", transactionRecord.getReference());

        if (isDuplicate(transactionRecord) || !isEndBalanceValid(transactionRecord)) {
            uniqueRecords.add(transactionRecord);
            return transactionRecord;
        }

        uniqueRecords.add(transactionRecord);
        return null;
    }


    /**
     * Updates the record with processing result for a duplicate
     *
     * @param record
     * @return
     */
    private boolean isDuplicate(TransactionRecord record) {
        if (uniqueRecords.contains(record)) {
            log.error("[DUPLICATE] Duplicate record found for reference id: {}", record.getReference());
            record.setProcessingResult("[DUPLICATE] Record with reference id:" + record.getReference() + " already exists");
            return true;
        }
        return false;
    }

    /**
     * Accounting for non-negative end balance, this methods marks
     * the record as invalid
     *
     * @param record
     * @return
     */
    private boolean isEndBalanceValid(TransactionRecord record) {
        if (record.getEndBalance().compareTo(new BigDecimal(0)) < 0) {
            log.error("[INVALID] Negative balance found for id: {}", record.getReference());
            record.setProcessingResult("[INVALID] End Balance value cannot be in the negative");
            return false;
        }
        return true;
    }
}
