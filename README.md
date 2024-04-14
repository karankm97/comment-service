# Quick Comment Service

This is a simple comment service that allows users to post comments and reactions.

## Tech Stack:
* Java SpringBoot
* MySQL
* Redis Cache
* JUnit and SpringBootTest for unit and integration tests
* Python for API automation tests

## How to run the service locally?
- Install the required packages using the command `mvn clean install`
- Make sure you have the DB and redis server running. Put the required configurations in the `application.properties` file.
- Run the application using the command `java -jar target/quick-comment-service-0.0.1-SNAPSHOT.jar`
- The service will be running on `http://localhost:8080`