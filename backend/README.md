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
		"role": "RESEARCHER"
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

## Testing

Run the backend test suite (uses an H2 database) with `mvn test`

## TODO
- Remove database password from `application.yaml`