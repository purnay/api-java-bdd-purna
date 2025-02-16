# Java and BDD sample framework for API test automation

This project has sample tests created to demonstrate API test automation with [Cucumber-JVM](https://cucumber.io/docs/installation/java/)


## Objective

Verification of the API endpoints


## High level test flow

* API user obtains authorisation token using login credentials
* Search books by author, borrow an available book, return it followed by viewing of borrowing history
* A negative journey that attempts borrowing without availability is also added to verify API error handling 
* All available endpoints and their responses are verified in the two tests developed


Tests have been developed in Java 21

Implemented **Spring Boot** with **Cucumber Spring** for effective dependency injection.

**AssertJ** - fluent assertions java library is used for assertions

Used **REST-assured**, a popular testing framework for RESTful APIs


## Getting Started

### Prerequisites

* JDK 21
* Maven 3.6.3 or higher


### Executing tests


Following are the command line arguments for test run.

- `-DlogRestCalls` - API request and responses are logged to a separate file created in `target` folder. The logging  is available by default. To stop the logging, this property can be set as `false`
- `-Dusername` and `-Dpassword` - Default values are provided in the properties file within the project, but these two can be used to supply different values or to avoid exposure of credentials in properties file

To run tests from IDE, run the Cucumber Runner Class `ExecuteApiFeatures` 

Tests can be executed from command line using the example commands below.

Examples

- `mvn clean test`
- `mvn clean test -Dusername=AnyUser -Dpassword=pass`
- `mvn test -Dcucumber.filter.tags="@error"` (To run specific scenario that has tag `@error`. Command line syntax for this Cucumber tag command might vary in Windows PowerShell)

## Reports

Cucumber html report with test results will be generated at `target/reports/library-api-test-reports.html`
