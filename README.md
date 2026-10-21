# URL Shortener

A REST API for creating short URLs and redirecting to their saved destinations.
It uses Java 21, Spring Boot, Spring Data JPA, and MySQL.

## Requirements

- Java 21 or later
- MySQL

The project includes the Maven Wrapper, so a separate Maven installation is
not required.

## Database configuration

Start MySQL and create the database:

```sql
CREATE DATABASE url_shortener;
```

By default, the application connects to
`jdbc:mysql://localhost:3306/url_shortener` as user `root`. Configure the
password with the `SPRING_DATASOURCE_PASSWORD` environment variable. You can
also override the JDBC URL and username with `DB_URL` and `DB_USERNAME`.

For example, in PowerShell:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "your-local-password"
```

Do not commit real database credentials.

## Run the application

From the directory containing `pom.xml`:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. The short links it returns
use `http://localhost:8080` as their base URL by default. To configure a
different base URL, set `APP_BASE_URL` before starting the application:

```powershell
$env:APP_BASE_URL = "https://short.example.com"
```

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

Request body:

```json
{
  "originalUrl": "https://www.example.com/some/very/long/path"
}
```

On success, the API returns `201 Created`. The short code is seven
URL-safe characters, and its uniqueness is enforced by the database.

```json
{
  "originalUrl": "https://www.example.com/some/very/long/path",
  "shortCode": "aB3xY7k",
  "shortUrl": "http://localhost:8080/aB3xY7k"
}
```

Only valid HTTP and HTTPS destination URLs are accepted. Invalid input returns
`400 Bad Request` with a JSON error message.

PowerShell example:

```powershell
curl.exe -i -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.example.com/some/very/long/path"}'
```

### Redirect to the saved destination

Request the short code returned by the create endpoint:

```http
GET http://localhost:8080/aB3xY7k
```

An existing, unexpired short code returns `302 Found` with a `Location` header
pointing to the destination saved in the database. The redirect destination is
looked up by short code; it is not accepted from the request.

An unknown short code returns `404 Not Found`. If a stored URL has an `expiresAt`
timestamp that has passed, the endpoint returns `410 Gone`. Expiration is not
currently set through the URL creation API.

Use `curl.exe -i` to inspect the redirect status and `Location` header without
following the redirect:

```powershell
curl.exe -i http://localhost:8080/aB3xY7k
```

### Test with Postman

1. Start the application and make sure MySQL is available.
2. To create a short URL, select **POST** and enter
   `http://localhost:8080/api/urls`.
3. Select **Body** → **raw** → **JSON**, then send:

   ```json
   {
     "originalUrl": "https://www.example.com/some/very/long/path"
   }
   ```

4. Confirm the response is `201 Created` and copy the returned `shortCode`.
5. Create a **GET** request to `http://localhost:8080/<shortCode>` and send it.
   Postman should show `302 Found` and a `Location` response header containing
   the saved destination. Disable automatic redirect following in Postman if
   you want to inspect the 302 response directly.
6. A GET request with an unknown code should return `404 Not Found`.

## Build and tests

Compile the application:

```powershell
.\mvnw.cmd -DskipTests compile
```

Run all tests:

```powershell
.\mvnw.cmd test
```

The application context test uses the configured datasource, so MySQL must be
running and accessible for the full test suite.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- MySQL Connector/J
- Maven Wrapper
