# How to run the API Automation tests?

- Install the required packages using the command `pip install -r requirements.txt`
- Generate the latest JAR using `mvn clean install -DskipTests`
- Make sure you have the DB and redis server running. Put the required configurations in the `application-apitest.properties` file.
- Run the application using the command `java -jar target/employee-0.0.1-SNAPSHOT.jar --spring.profiles.active=apitest`
- Run the tests using the command `pytest -v`