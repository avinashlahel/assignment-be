package com.records.StatementProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class StatementProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatementProcessorApplication.class, args);
	}

}
