# User Microservice — Energy Management System

A Spring Boot REST API microservice for managing users in the Energy Management System. Includes user CRUD operations, role-based user management, and secure password storage using BCrypt encryption.

## Overview

This microservice is part of the Energy Management System and handles all user-related operations. It provides RESTful endpoints for creating, reading, updating, and deleting users with role-based access (ADMIN and CLIENT).

## Project Structure

```
demo/
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── demo
│   │   │               ├── config
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controllers
│   │   │               │   └── UserController.java
│   │   │               ├── dtos
│   │   │               │   ├── builders
│   │   │               │   │   └── UserBuilder.java
│   │   │               │   ├── UserDTO.java
│   │   │               │   └── UserDetailsDTO.java
│   │   │               ├── entities
│   │   │               │   ├── User.java
│   │   │               │   └── Role.java
│   │   │               ├── handlers
│   │   │               │   ├── exceptions
│   │   │               │   └── RestExceptionHandler.java
│   │   │               ├── repositories
│   │   │               │   └── UserRepository.java
│   │   │               ├── services
│   │   │               │   └── UserService.java
│   │   │               └── DemoApplication.java
│   │   └── resources
│   │       └── application.properties
├── pom.xml
└── postman_collection.json
```

## Features

- **Full CRUD Operations**: Create, Read, Update, Delete users
- **Role Management**: Support for ADMIN and CLIENT roles
- **Password Security**: BCrypt password encryption
- **Validation**: Input validation for username, password, email, and other fields
- **Duplicate Prevention**: Username uniqueness validation
- **Error Handling**: Comprehensive exception handling with detailed error messages

## User Entity

A user has the following attributes:
- `id` (UUID) - Automatically generated unique identifier
- `username` (String) - Unique username (min 3 characters)
- `password` (String) - Encrypted password (min 6 characters)
- `email` (String) - Email address (optional, validated format)
- `name` (String) - Full name of the user
- `role` (Enum) - Either ADMIN or CLIENT

## Prerequisites

- **Java JDK 25**
- **PostgreSQL** server accessible from the app
- **Postman** (optional) for testing the API

## Database Setup (PostgreSQL)

Create the database before running the application:

```sql
CREATE DATABASE user_db;
```

Default connection values:
```
DB_IP=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=postgres
DB_DBNAME=user_db
```

> Note: Hibernate is set to `spring.jpa.hibernate.ddl-auto=update`, so the `users` table will be created automatically on first run.

## Configuration

All settings are in `src/main/resources/application.properties`. Override them via environment variables:

| Purpose | Property | Env var | Default |
|---|---|---|---|
| DB host | `database.ip` | `DB_IP` | `localhost` |
| DB port | `database.port` | `DB_PORT` | `5432` |
| DB user | `database.user` | `DB_USER` | `postgres` |
| DB password | `database.password` | `DB_PASSWORD` | `postgres` |
| DB name | `database.name` | `DB_DBNAME` | `user_db` |
| HTTP port | `server.port` | `PORT` | `8080` |

## How to Run (Local)

From the project root (`demo/`):

```bash
# 1) Set environment variables (if needed)
export DB_IP=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=postgres
export DB_DBNAME=user_db
export PORT=8080

# 2) Run the application
./mvnw spring-boot:run
```

The application will start on **http://localhost:8080**

## API Endpoints

### Users Resource (`/users`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/users` | List all users | - | 200 OK, List of UserDTO |
| GET | `/users/{id}` | Get user by ID | - | 200 OK, UserDetailsDTO |
| GET | `/users/role/{role}` | Get users by role (ADMIN/CLIENT) | - | 200 OK, List of UserDTO |
| POST | `/users` | Create new user | UserDetailsDTO | 201 Created, Location header |
| PUT | `/users/{id}` | Update user | UserDetailsDTO | 204 No Content |
| DELETE | `/users/{id}` | Delete user | - | 204 No Content |

### Request/Response Examples

**Create User (POST /users)**
```json
{
  "username": "johndoe",
  "password": "password123",
  "email": "john.doe@example.com",
  "name": "John Doe",
  "role": "CLIENT"
}
```

**User Response (GET /users/{id})**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "password": null,
  "email": "john.doe@example.com",
  "name": "John Doe",
  "role": "CLIENT"
}
```

**Note**: Password is never returned in GET responses for security reasons.

## Testing with Postman

1. Import the collection file: `postman_collection.json`
2. Verify collection variables:
   - `baseUrl` → `http://localhost:8080`
   - `resource` → `users`
3. Run requests in order (the collection includes tests that store `userId` after creation)

## Validation Rules

- **Username**: Required, minimum 3 characters, must be unique
- **Password**: Required, minimum 6 characters (encrypted with BCrypt)
- **Email**: Must be valid email format (if provided)
- **Name**: Required, cannot be blank
- **Role**: Required, must be either ADMIN or CLIENT

## Error Responses

The API returns detailed error responses in the following format:

```json
{
  "timestamp": "2025-10-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "MethodArgumentNotValidException",
  "resource": "Validation failed",
  "details": [
    "username: Username must be at least 3 characters"
  ],
  "path": "uri=/users"
}
```

## Security Notes

- Passwords are encrypted using BCrypt before storage
- Passwords are never returned in API responses
- Username uniqueness is enforced at the database level
- All validation errors are returned with clear messages

## Integration with Other Microservices

This User Microservice is designed to work with:
- **Authentication Microservice**: Handles login/logout and token generation
- **Device Microservice**: Manages device-to-user associations
- **API Gateway**: Routes requests and performs authorization

## Next Steps

To complete the Energy Management System:
1. Implement Device Microservice
2. Implement Authentication Microservice
3. Set up API Gateway (Traefik or similar)
4. Dockerize all services
5. Create deployment diagram

---

**Course**: Distributed Systems  
**Assignment**: Request-Reply Communication  
**Institution**: UTCN - Faculty of Automation and Computer Science