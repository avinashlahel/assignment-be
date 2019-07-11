package com.records.StatementProcessor;

import com.records.StatementProcessor.batch.TransactionRecordProcessor;
import com.records.StatementProcessor.model.TransactionRecord;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.*;

/**
 * Test cases for batch processing, marked to run
 * in an ascending order to complete the report
 * generation before any assertions
 *
 * @author avi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = StatementProcessorApplication.class, properties = {"spring.batch.job.enabled=false"})
@ContextConfiguration(locations = {"classpath:test-context.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatementProcessorApplicationTests {

    @Autowired
    TransactionRecordProcessor recordProcessor;
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Value("${csvOutputPath}")
    private String csvOutput;
    @Value("${xmlOutputPath}")
    private String xmlOutput;

    /**
     * This test will cover the entire batch run and generate
     * the report in the resources folder
     *
     * @throws Exception
     */
    @Test
    public void completeJobTest() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

    /**
     * Test existence of csv report generated
     * after running the complete job
     */
    @Test
    public void testCSVReportGenerated() {
        File file = new File(csvOutput);
        assertTrue(file.exists());
    }

    /**
     * Test existence of xml report generated
     * after running the complete job
     */
    @Test
    public void testXMLReportGenerated() {
        File file = new File(xmlOutput);
        assertTrue(file.exists());
    }


    /**
     * Test Duplicate: A duplicate record should be returned
     * by the processor to be written back to the ItemWriter
     *
     * @throws Exception
     */
    @Test
    public void testDuplicate() throws Exception {
        TransactionRecord record = new TransactionRecord();
        record.setReference(109762);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("Flowers from Rik de Vries");
        record.setStartBalance(new BigDecimal(47.45));
        record.setMutation(new BigDecimal(17.82));
        record.setEndBalance(new BigDecimal(65.27));
        TransactionRecord response = recordProcessor.process(record);
        assertEquals(record, response);
    }

    /**
     * Test inValid End balance: Test the scenario where end
     * balance is not equal to the sum of startBalance and
     * mutation. In such a case the processor should
     * return the record to be written to the ItemWriter
     * @throws Exception
     */
    @Test
    public void testInValidEndBalance() throws Exception {
        TransactionRecord record = new TransactionRecord();
        record.setReference(111111);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("This is a test desc");
        record.setStartBalance(new BigDecimal(47.45).setScale(2, RoundingMode.CEILING));
        record.setMutation(new BigDecimal(17.82).setScale(2, RoundingMode.CEILING));
        record.setEndBalance(new BigDecimal(12.27).setScale(2, RoundingMode.CEILING));
        TransactionRecord response = recordProcessor.process(record);
        assertEquals(record, response);
    }

    /**
     * Test Valid End balance: Test the scenario where end
     * balance is EQUAL to the sum of startBalance and
     * mutation. In such a case the processor should
     * return the record to be written to the ItemWriter
     * @throws Exception
     */
    @Test
    public void testValidEndBalance() throws Exception {
        TransactionRecord record = new TransactionRecord();
        record.setReference(113331);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("This is a test desc");
        record.setStartBalance(new BigDecimal(47.45).setScale(2, RoundingMode.CEILING));
        record.setMutation(new BigDecimal(17.82).setScale(2, RoundingMode.CEILING));
        record.setEndBalance(new BigDecimal(65.28).setScale(2, RoundingMode.CEILING));
        TransactionRecord response = recordProcessor.process(record);
        assertNull(response);
    }



}
