# Eye Tracking Backend

## Setup
1. Run Docker Desktop
2. `docker-compose up -d`
3. `mvn spring-boot:run`

## Authentication API

### Register a user

- **Endpoint:** `POST /api/auth/register`
- **Body:**
	```json
	{
		"email": "researcher@example.com",
		"password": "StrongPass123",
		"role": "USER"
	}
	```
- **Response:** returns the generated `userId`, normalized `email`, assigned `role`, and `createdAt` timestamp.

### Login

- **Endpoint:** `POST /api/auth/login`
- **Body:**
	```json
	{
		"email": "researcher@example.com",
		"password": "StrongPass123"
	}
	```
- **Response:** returns the `userId`, `role`, `loggedInAt` timestamp, and a short success message.

## API documentation

- Install dependencies with `./mvnw dependency:resolve` (first run will do this automatically).
- Start the backend (`./mvnw spring-boot:run`).
- Open Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to explore and try all endpoints. The generated OpenAPI JSON is also available at `/v3/api-docs` for tooling integration.

## Testing

Run the backend test suite (uses an H2 database) with `mvn test`

## TODO
- Remove database password from `application.yaml`