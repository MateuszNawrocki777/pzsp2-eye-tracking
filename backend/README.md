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
- **Response:** returns the generated `userId`, normalized `email`, assigned `role`, `createdAt` timestamp, plus a freshly issued `token` and its `expiresAt` timestamp so the client can treat registration as an automatic login.

### Login

- **Endpoint:** `POST /api/auth/login`
- **Body:**
	```json
	{
		"email": "researcher@example.com",
		"password": "StrongPass123"
	}
	```
- **Response:** returns the `userId`, `role`, a signed `token`, and its `expiresAt` timestamp. Include the token in subsequent requests via the `Authorization: Bearer <token>` header (protected endpoints will use it once security rules are tightened).

## API documentation

- Install dependencies with `./mvnw dependency:resolve` (first run will do this automatically).
- Start the backend (`./mvnw spring-boot:run`).
- Open Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to explore and try all endpoints. The generated OpenAPI JSON is also available at `/v3/api-docs` for tooling integration.
- JWT tokens use the HS512 algorithm, which **requires at least a 64-byte secret**. Export `JWT_SECRET` before starting the app; otherwise the built-in 64-character default will be used for local development.
- Generate a strong secret (>= 64 chars) for your environment, for example:

	```bash
	openssl rand -hex 64
	# fish shell
	set -x JWT_SECRET (openssl rand -hex 64)
	```

## Testing

Run the backend test suite (uses an H2 database) with `mvn test`

## TODO
- Remove database password from `application.yaml`