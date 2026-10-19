# WinWin.travel — Backend Engineer Test Task

Implementation of two Spring Boot microservices (`auth-api` and `data-api`) with a PostgreSQL database and orchestration via Docker Compose.

---

## 🏗 Architecture

* **Service A (`auth-api`, port 8080)**:
  * Tech stack: Spring Boot (Web, Security, Data JPA).
  * Database: PostgreSQL (`winwindb`).
  * Features:
    * User registration and authentication with password hashing (BCrypt) and JWT issuance.
    * Protected endpoint: `POST /api/process` (requires Bearer JWT).
    * Inter-service call to Service B (`POST http://data-api:8081/api/transform`) with shared secret header `X-Internal-Token`.
    * Stores processing audit logs into the `processing_log` table.
* **Service B (`data-api`, port 8081)**:
  * Tech stack: Spring Boot (Web).
  * Features:
    * Endpoint `POST /api/transform` for text transformation.
    * Validates the incoming `X-Internal-Token` header. Returns `403 Forbidden` if missing or invalid.
* **PostgreSQL (port 5432)**:
  * Stores user credentials (`users`) and request processing history (`processing_log`).

---

## ⚙️ Environment Variables

| Variable | Description | Default Value |
|---|---|---|
| `POSTGRES_DB` | Database name | `winwindb` |
| `POSTGRES_USER` | Database username | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | Secret key used to sign and verify JWT tokens | `my-default-secret-key-that-is-long-enough-32-bytes` |
| `INTERNAL_TOKEN` | Shared secret token between Service A and Service B | `my-secret-internal-token` |
| `TRANSFORM_SERVICE_URL` | Service B base URL used by Service A | `http://data-api:8081` |

---

## 🚀 Build & Run

### Option 1: Run directly with Docker Compose (Recommended)
Thanks to multi-stage builds in the `Dockerfile`s, Docker compiles and packages the applications inside isolated containers:

```bash
docker compose up -d --build
```

### Option 2: Build locally with Maven first
```bash
# 1. Package JAR files
mvn -f authservice/auth/pom.xml clean package -DskipTests
mvn -f transformservice/transform/pom.xml clean package -DskipTests

# 2. Start containers
docker compose up -d --build
```

To stop all running services:
```bash
docker compose down
```

---

## 🧪 Testing Guide (cURL)

### 1. Register a new user
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@winwin.travel","password":"password123"}'
```
> **Expected Status**: `HTTP/1.1 201 Created`

---

### 2. Login & retrieve JWT token
```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@winwin.travel","password":"password123"}'
```
> **Expected Response** (`200 OK`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Save the received token to an environment variable for convenience:
```bash
TOKEN="<paste_jwt_token_here>"
```

---

### 3. Call protected endpoint `/api/process` (Service A -> Service B -> PostgreSQL)

```bash
curl -i -X POST http://localhost:8080/api/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text":"hello world"}'
```
> **Expected Response** (`200 OK`):
```json
{
  "result": "dlrow olleh"
}
```

*Request without authorization header or with an invalid token:*
```bash
curl -i -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{"text":"hello world"}'
```
> **Expected Status**: `HTTP/1.1 401 Unauthorized`

---

### 4. Verify Service B (`data-api`) security directly

#### Request without the internal token:
```bash
curl -i -X POST http://localhost:8081/api/transform \
  -H "Content-Type: application/json" \
  -d '{"text":"direct call"}'
```
> **Expected Status**: `HTTP/1.1 403 Forbidden`

#### Request with valid `X-Internal-Token`:
```bash
curl -i -X POST http://localhost:8081/api/transform \
  -H "Content-Type: application/json" \
  -H "X-Internal-Token: my-secret-internal-token" \
  -d '{"text":"direct call"}'
```
> **Expected Response** (`200 OK`):
```json
{
  "result": "llac tcerid"
}
```

---

### 5. Verify database records in PostgreSQL

Connect to the PostgreSQL container and inspect the audit log table:
```baseyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlM2E0MzM2Ni1hODg3LTQ1ZWQtYjU1NS01MzczNzIyMzljZmEiLCJlbWFpbCI6InRlc3RAd2lud2luLnRyYXZlbCIsImlhdCI6MTc5MjQyOTI4NSwiZXhwIjoxNzkzMTQ5Mjg1fQ.bdgUH-eGDnjC6LEvA4akkl_DR9wqUM9mMHDQ9LKjrvEh
docker exec -it postgres psql -U postgres -d winwindb -c "SELECT * FROM processing_log;"
```

---

## 🗄 Data Model (PostgreSQL)

### `users` table:
* `id` — UUID (Primary Key).
* `email` — VARCHAR, UNIQUE.
* `password` — VARCHAR (BCrypt hash).
* `created_at` — TIMESTAMP.

### `processing_log` table:
* `id` — UUID (Primary Key).
* `user_id` — UUID (User reference).
* `input_text` — TEXT (Input text payload).
* `output_text` — TEXT (Transformed result).
* `created_at` — TIMESTAMP (Processing timestamp).
