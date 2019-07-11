# Assignment-be

The application is written to process Multi-Source data records and generate a report
marking the invalid records as well as records identified as duplicates.

# Technologies used

- Spring Batch
- Spring boot
- Java
- Maven

# Running locally

git cloning the repository locally and running mvn install should be all that is required
to get started with the application.
Once the maven dependencies are installed we need to run the class <b>StatementProcessorApplication</b>
<br>

The application is pre-configured with test data in the resources folder. transactions.csv for csv input file and
xmlRecords.xml for xml input file. The output will also be generated in the same folder. This can be changed by
making the necessary changes in the application.properties.

git clone url : https://github.com/avinashlahel/assignment-be.git

# Tests

Tests for the application are written in <b>StatementProcessorApplicationTests</b>.All The tests can be
run by running this class.

# External Configuration

To change the location of the input/output files, use the application.properties file. The properties have been 
named accordingly.

 

