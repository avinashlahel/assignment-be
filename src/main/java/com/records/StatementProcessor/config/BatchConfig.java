package com.records.StatementProcessor.config;

import com.records.StatementProcessor.FieldValidationEventHandler;
import com.records.StatementProcessor.RecordFieldSetMapper;
import com.records.StatementProcessor.model.TransactionRecord;
import com.records.StatementProcessor.utils.FileVerificationSkipper;
import com.records.StatementProcessor.utils.HeaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * Configuration class for batch processing
 * @author avi
 */
@EnableBatchProcessing
@Configuration
@Primary
public class BatchConfig extends DefaultBatchConfigurer {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    private static final String HEADER = "REFERENCE,ACCOUNT NUMBER,DESCRIPTION,ERROR";
    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Value("${csvOutputPath}")
    private String csvOutput;

    @Value("${xmlOutputPath}")
    private String xmlOutput;

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, @Qualifier("stepOne") Step stepOne, @Qualifier("stepTwo") Step stepTwo) {
        return jobBuilderFactory.get("statement-processor")
                .incrementer(new RunIdIncrementer())
                .flow(stepOne)
                .next(stepTwo)
                .end()
                .build();
    }

    @Bean
    public Step stepOne(StepBuilderFactory stepBuilderFactory, ItemReader<TransactionRecord> csvItemReader,
                        ItemProcessor<TransactionRecord, TransactionRecord> itemProcessor,
                        ItemWriter<TransactionRecord> csvWriter) {

        return stepBuilderFactory.get("csv-reader")
                .<TransactionRecord, TransactionRecord>chunk(10)
                .reader(csvItemReader)
                .faultTolerant()
                .skipPolicy(fileVerificationSkipper())
                .processor(itemProcessor)
                .writer(csvWriter)
                .build();
    }

    @Bean
    public Step stepTwo(StepBuilderFactory stepBuilderFactory, ItemReader<TransactionRecord> xmlItemReader,
                        ItemProcessor<TransactionRecord, TransactionRecord> itemProcessor,
                        ItemWriter<TransactionRecord> xmlWriter) {

        return stepBuilderFactory.get("xml-reader")
                .<TransactionRecord, TransactionRecord>chunk(10)
                .reader(xmlItemReader)
                .faultTolerant()
                .skipPolicy(fileVerificationSkipper())
                .processor(itemProcessor)
                .writer(xmlWriter)
                .build();
    }


    /**
     * csv reader for csv processing step
     * @param resource
     * @return
     */
    @Bean
    public ItemReader<TransactionRecord> csvItemReader(@Value("${csvInput}") Resource resource) {
        FlatFileItemReader<TransactionRecord> fileItemReader = new FlatFileItemReader<>();
        fileItemReader.setName("csv-reader");
        fileItemReader.setResource(resource);
        fileItemReader.setLinesToSkip(1);
        fileItemReader.setLineMapper(csvLineMapper());

        return fileItemReader;
    }

    /**
     * xml reader for xml processing step
     * @param resource
     * @return
     */
    @Bean
    public ItemReader<TransactionRecord> xmlItemReader(@Value("${xmlInput}") Resource resource) {
        StaxEventItemReader<TransactionRecord> xmlFileReader = new StaxEventItemReader<>();
        xmlFileReader.setName("xml-reader");
        xmlFileReader.setResource(resource);
        xmlFileReader.setFragmentRootElementName("record");

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setValidationEventHandler(new FieldValidationEventHandler());
        marshaller.setClassesToBeBound(TransactionRecord.class);
        xmlFileReader.setUnmarshaller(marshaller);

        return xmlFileReader;
    }

    @Bean
    public LineMapper<TransactionRecord> csvLineMapper() {
        DefaultLineMapper<TransactionRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(true);
        tokenizer.setNames(new String[]{"reference", "accountNumber", "description", "startBalance", "mutation", "endBalance"});

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new RecordFieldSetMapper());

        return lineMapper;
    }


    /**
     * FileWriter bean to be passed into csv step
     * @return
     */
    @Bean
    public FlatFileItemWriter<TransactionRecord> csvWriter(){
        {
            FlatFileItemWriter<TransactionRecord> itemWriter = new FlatFileItemWriter<>();
            itemWriter.setResource(new FileSystemResource(csvOutput));
            log.info(">>>>>>>  Output file to be written at : {} ",csvOutput);
            return configureFlatFileItemWriter(itemWriter);
        }
    }


    /**
     * FileWriter bean to be passed into xml step
     * @return
     */
    @Bean
    public FlatFileItemWriter<TransactionRecord> xmlWriter(){
        {
            FlatFileItemWriter<TransactionRecord> itemWriter = new FlatFileItemWriter<>();
            itemWriter.setResource(new FileSystemResource(xmlOutput));
            log.info(">>>>>>>  Output file to be written at : {} ",xmlOutput);
            return configureFlatFileItemWriter(itemWriter);
        }
    }

    /**
     * Generic code for the two writers
     * @param itemWriter
     * @return
     */
    private FlatFileItemWriter<TransactionRecord> configureFlatFileItemWriter(FlatFileItemWriter<TransactionRecord> itemWriter){
        itemWriter.setEncoding("UTF-8");
        itemWriter.setHeaderCallback(new HeaderWriter(HEADER));

        DelimitedLineAggregator<TransactionRecord> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<TransactionRecord> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"reference","accountNumber","description","processingResult"});

        lineAggregator.setFieldExtractor(fieldExtractor);

        itemWriter.setLineAggregator(lineAggregator);
        return itemWriter;
    }

    @Bean
    public SkipPolicy fileVerificationSkipper() {
        return new FileVerificationSkipper();
    }

}




