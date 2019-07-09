package com.records.StatementProcessor.config;
import com.records.StatementProcessor.model.TransactionRecord;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;



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

    @Value("${outputReportLocation}")
    private String outputReportLocation;

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory,Step stepOne,Step StepTwo) {
        return jobBuilderFactory.get("statement-processor")
                .incrementer(new RunIdIncrementer())
                .flow(stepOne)
//                .next(stepOne)
                .end()
                .build();
    }

    @Bean
    @Primary
    public Step stepOne(StepBuilderFactory stepBuilderFactory, ItemReader<TransactionRecord> itemReader,
                        ItemProcessor<TransactionRecord, TransactionRecord> itemProcessor,
                        ItemWriter<TransactionRecord> itemWriter) {

        return stepBuilderFactory.get("csv-reader")
                .<TransactionRecord, TransactionRecord>chunk(10)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Step stepTwo(StepBuilderFactory stepBuilderFactory, ItemReader<TransactionRecord> itemReader,
                        ItemProcessor<TransactionRecord, TransactionRecord> itemProcessor,
                        ItemWriter<TransactionRecord> itemWriter) {

        return stepBuilderFactory.get("xml-reader")
                .<TransactionRecord, TransactionRecord>chunk(10)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }


    @Bean
    public FlatFileItemReader<TransactionRecord> csvItemReader(@Value("${input}") Resource resource) {
        FlatFileItemReader<TransactionRecord> fileItemReader = new FlatFileItemReader<>();
        fileItemReader.setName("csv-reader");
        fileItemReader.setResource(resource);
        fileItemReader.setLinesToSkip(1);
        fileItemReader.setLineMapper(csvLineMapper());

        return fileItemReader;
    }

    @Bean
    public LineMapper<TransactionRecord> csvLineMapper() {
        DefaultLineMapper<TransactionRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(true);
        tokenizer.setNames(new String[]{"reference", "accountNumber", "description", "startBalance", "mutation", "endBalance"});

        // fieldsetmapper to map parsed field
        BeanWrapperFieldSetMapper<TransactionRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(TransactionRecord.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }


    /**
     * FileWriter bean to be passed into each of the steps
     * @return
     */
    @Bean
    public FlatFileItemWriter<TransactionRecord> writer(){
        {

            FlatFileItemWriter<TransactionRecord> itemWriter = new FlatFileItemWriter<>();
            itemWriter.setResource(new FileSystemResource(outputReportLocation));
            itemWriter.setAppendAllowed(true);
            itemWriter.setEncoding("UTF-8");
            itemWriter.setHeaderCallback(new HeaderWriter(HEADER));

            DelimitedLineAggregator<TransactionRecord> lineAggregator = new DelimitedLineAggregator<>();
            lineAggregator.setDelimiter(",");

            BeanWrapperFieldExtractor<TransactionRecord> fieldExtractor = new BeanWrapperFieldExtractor<>();
            fieldExtractor.setNames(new String[] {"reference","accountNumber","description","processingResult"});

            lineAggregator.setFieldExtractor(fieldExtractor);

            itemWriter.setLineAggregator(lineAggregator);
            log.info(">>>>>>>  Output file to be written at : {} ",outputReportLocation);

            return itemWriter;
        }
    }

}


