# URL Shortener

A Java REST API project built with Spring Boot. The application currently exposes a basic health-check endpoint; URL creation and redirection features have not been implemented yet.

## Requirements

- Java 21 or later
- MySQL

The project uses the Maven Wrapper, so a separate Maven installation is not required.

## Configure the database

Start MySQL and create the database configured by the application:

```sql
CREATE DATABASE url_shortener;
```

The default JDBC URL is `jdbc:mysql://localhost:3306/url_shortener`, and the configured username is `root`. Set the password in `src/main/resources/application.properties`, or override it through the `SPRING_DATASOURCE_PASSWORD` environment variable. Do not commit real database credentials.

In PowerShell, set the password for the current terminal session with:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "your-local-password"
```

## Run the application

From the directory containing `pom.xml`:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default.

## API

### Health check

```http
GET http://localhost:8080/api/health
```

Successful response:

```text
URL Shortener API is running!
```

## Build and tests

Build the project:

```powershell
.\mvnw.cmd package
```

Run the tests:

```powershell
.\mvnw.cmd test
```

The Spring application context test connects to the configured datasource, so MySQL must be running and accessible for the tests to pass.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- MySQL Connector/J
- Maven Wrapper
