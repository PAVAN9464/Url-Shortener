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

### Create a short URL

```http
POST http://localhost:8080/api/urls
Content-Type: application/json
```

Request:

```json
{"originalUrl":"https://www.example.com/some/very/long/path"}
```

The endpoint returns `201 Created` with the original URL, generated short code,
and short URL. Configure the short URL host with `APP_BASE_URL`; it defaults to
`http://localhost:8080`.

Example response:

```json
{"originalUrl":"https://www.example.com/some/very/long/path","shortCode":"aB3xY7k","shortUrl":"http://localhost:8080/aB3xY7k"}
```

PowerShell:

```powershell
curl.exe -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.example.com/some/very/long/path"}'
```

Invalid or non-HTTP(S) URLs return `400 Bad Request`.

### Redirect using a short code

Open the generated short URL in a browser, or request it directly:

```powershell
curl.exe -i http://localhost:8080/aB3xY7k
```

An existing short code returns `302 Found` with a `Location` header pointing to
the stored destination. An unknown code returns `404 Not Found`; an expired URL
returns `410 Gone`.

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
