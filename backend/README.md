# Eye Tracking Backend

## Setup
`docker-compose up -d --build`

## Tests & Study Management API

### Create a new Study (Test)

- **Endpoint:** `POST /api/tests`
- **Type:** `multipart/form-data`
- **Body:**
    - `files` (Key): Select one or more files (images/PDFs).
    - `settings` (Key): JSON object containing study configuration.
      > **Important:** When testing in Postman, you must manually set the `Content-Type` for the `settings` part to `application/json`.
      ```json
      {
          "title": "Eye Tracking Test 1",
          "description": "Initial experiment",
          "dispGazeTracking": true,
          "dispTimeLeft": false,
          "timePerImageMs": 5000,
          "randomizeOrder": true
      }
      ```
- **Response:** Returns the `UUID` of the created study.

### Get All Studies

- **Endpoint:** `GET /api/tests`
- **Response:** Returns a list of summaries for all studies created by the user (currently using a test user).
    ```json
    [
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "title": "Eye Tracking Study 1",
            "firstImageLink": "http://localhost:8080/api/tests/files/c871578f-..."
        }
    ]
    ```

### Get Study Details

- **Endpoint:** `GET /api/tests/{testId}`
- **Response:** Returns full study settings and a list of links to all associated files.
    ```json
    {
        "id": "...",
        "title": "Eye Tracking Study 1",
        "description": "Initial experiment",
        "dispGazeTracking": true,
        "dispTimeLeft": false,
        "timePerImageMs": 5000,
        "randomizeOrder": true,
        "fileLinks": [
            "http://localhost:8080/api/tests/files/...",
            "http://localhost:8080/api/tests/files/..."
        ]
    }
    ```

### Update Study Settings

- **Endpoint:** `PUT /api/tests/{testId}`
- **Body:** JSON object (Raw). Note: This endpoint updates metadata only, not files.
    ```json
    {
        "title": "Updated Title",
        "description": "Updated description",
        "dispGazeTracking": false,
        "dispTimeLeft": true,
        "timePerImageMs": 3000,
        "randomizeOrder": false
    }
    ```
- **Response:** `200 OK`

### Delete Study

- **Endpoint:** `DELETE /api/tests/{testId}`
- **Effect:** Removes the study entry from the database and physically deletes all associated files from the server storage (`uploads_data` volume).
- **Response:** `204 No Content`

### Manage Individual Files

#### Add file to existing study
- **Endpoint:** `POST /api/tests/{testId}/files`
- **Type:** `multipart/form-data`
- **Body:**
    - `file`: Single file to append to the study.

#### Delete specific file
- **Endpoint:** `DELETE /api/tests/files/{fileId}`
- **Response:** `204 No Content`

#### View/Download file
- **Endpoint:** `GET /api/tests/files/{fileId}`
- **Response:** Binary stream of the file (image or PDF).

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
- **Notes:** Only `USER` accounts can be created through this endpoint; first `ADMIN` account is managed via configuration. `ADMIN` user then can change other user roles to `ADMIN` via endpoint ``

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
- Generate a strong secret (>= 64 chars) for your environment

## Default admin account

- On startup the backend ensures administrator account is created using the credentials from `app.security.admin` in `application.yaml` (or environment variables `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`).
- The initializer will create the admin user if it doesn't exist yet or rotate its password when the configured secret changes.

## Testing

Run the backend test suite (uses an H2 database) with `mvn test`

## TODO
- Remove database password from `application.yaml`