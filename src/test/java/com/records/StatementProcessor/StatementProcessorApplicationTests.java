package com.records.StatementProcessor;

import com.records.StatementProcessor.batch.TransactionRecordProcessor;
import com.records.StatementProcessor.model.TransactionRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@ContextConfiguration(classes = TestConfiguration.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatementProcessorApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(StatementProcessorApplicationTests.class);

    @Autowired
    TransactionRecordProcessor recordProcessor;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Value("${csvOutputPath}")
    private String csvOutput;

    @Value("${xmlOutputPath}")
    private String xmlOutput;

    @Before
    public void setUp() throws Exception {
        recordProcessor.reset();
        jobLauncherTestUtils.launchJob();
    }

    /**
     * This test will cover the entire batch run and generate
     * the report in the resources folder
     *
     * @throws Exception
     */
    @Test
    public void completeJobTest() throws Exception {
        log.info("*********  Running test : {} *********","completeJobTest");
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

    /**
     * Test existence of csv report generated
     * after running the complete job
     */
    @Test
    public void testCSVReportGenerated() {
        log.info("*********  Running test : {} *********","testCSVReportGenerated");
        File file = new File(csvOutput);
        assertTrue(file.exists());
    }

    /**
     * Test existence of xml report generated
     * after running the complete job
     */
    @Test
    public void testXMLReportGenerated() {
        log.info("*********  Running test : {} *********","testXMLReportGenerated");
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
        log.info("*********  Running test : {} *********","testDuplicate");
        TransactionRecord record = new TransactionRecord();
        record.setReference(109762);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("Flowers from Rik de Vries");
        record.setStartBalance(new BigDecimal(47.45).setScale(2, RoundingMode.HALF_EVEN));
        record.setMutation(new BigDecimal(17.82).setScale(2, RoundingMode.HALF_EVEN));
        record.setEndBalance(new BigDecimal(65.27).setScale(2, RoundingMode.HALF_EVEN));
        TransactionRecord response = recordProcessor.process(record);
        assertEquals(record, response);
    }


    /**
     * Test isNotDuplicate: A unique record should return
     * null so that the record in not written to the report
     *
     * @throws Exception
     */
    @Test
    public void testIsNotDuplicate() throws Exception {
        log.info("*********  Running test : {} *********","testIsNotDuplicate");
        TransactionRecord record = new TransactionRecord();
        record.setReference(999999);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("Flowers from Rik de Vries");
        record.setStartBalance(new BigDecimal(47.45).setScale(2, RoundingMode.HALF_EVEN));
        record.setMutation(new BigDecimal(17.82).setScale(2, RoundingMode.HALF_EVEN));
        record.setEndBalance(new BigDecimal(65.27).setScale(2, RoundingMode.HALF_EVEN));
        TransactionRecord response = recordProcessor.process(record);
        assertNull(response);
    }

    /**
     * Test inValid End balance: Test the scenario where end
     * balance is not equal to the sum of startBalance and
     * mutation. In such a case the processor should
     * return the record to be written to the ItemWriter
     *
     * @throws Exception
     */
    @Test
    public void testInValidEndBalance() throws Exception {
        log.info("*********  Running test : {} *********","testInValidEndBalance");
        TransactionRecord record = new TransactionRecord();
        record.setReference(111111);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("This is a test desc");
        record.setStartBalance(new BigDecimal(47.45).setScale(2, RoundingMode.HALF_EVEN));
        record.setMutation(new BigDecimal(17.82).setScale(2, RoundingMode.HALF_EVEN));
        record.setEndBalance(new BigDecimal(12.27).setScale(2, RoundingMode.HALF_EVEN));
        TransactionRecord response = recordProcessor.process(record);
        assertEquals(record, response);
    }

    /**
     * Test Valid End balance: Test the scenario where end
     * balance is EQUAL to the sum of startBalance and
     * mutation. In such a case the processor should
     * return the record to be written to the ItemWriter
     *
     * @throws Exception
     */
    @Test
    public void testValidEndBalance() throws Exception {
        log.info("*********  Running test : {} *********","testValidEndBalance");
        TransactionRecord record = new TransactionRecord();
        record.setReference(113331);
        record.setAccountNumber("NL93ABNA0585619023");
        record.setDescription("This is a test desc");
        record.setStartBalance(new BigDecimal(47.45).setScale(2, RoundingMode.HALF_EVEN));
        record.setMutation(new BigDecimal(17.82).setScale(2, RoundingMode.HALF_EVEN));
        record.setEndBalance(new BigDecimal(65.27).setScale(2, RoundingMode.HALF_EVEN));
        TransactionRecord response = recordProcessor.process(record);
        assertNull(response);
    }


}
