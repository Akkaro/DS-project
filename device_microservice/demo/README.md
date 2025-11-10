# Device Microservice — Energy Management System

A Spring Boot REST API microservice for managing smart energy metering devices in the Energy Management System. Includes device CRUD operations, user-device associations, and status-based filtering.

## Overview

This microservice is part of the Energy Management System and handles all device-related operations. It provides RESTful endpoints for creating, reading, updating, and deleting devices, as well as assigning devices to users.

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
│   │   │               │   └── DeviceController.java
│   │   │               ├── dtos
│   │   │               │   ├── builders
│   │   │               │   │   └── DeviceBuilder.java
│   │   │               │   ├── DeviceDTO.java
│   │   │               │   └── DeviceDetailsDTO.java
│   │   │               ├── entities
│   │   │               │   ├── Device.java
│   │   │               │   └── DeviceStatus.java
│   │   │               ├── handlers
│   │   │               │   ├── exceptions
│   │   │               │   └── RestExceptionHandler.java
│   │   │               ├── repositories
│   │   │               │   └── DeviceRepository.java
│   │   │               ├── services
│   │   │               │   └── DeviceService.java
│   │   │               └── DemoApplication.java
│   │   └── resources
│   │       └── application.properties
├── pom.xml
└── postman_collection.json
```

## Features

  - **Full CRUD Operations**: Create, Read, Update, Delete devices.
  - **Status Management**: Support for ACTIVE, INACTIVE, and MAINTENANCE statuses.
  - **User-Device Association**: Assign/unassign devices to users.
  - **Filtering**: Filter devices by user, status, or unassigned devices.
  - **Validation**: Input validation for name, consumption, and other fields.
  - **Duplicate Prevention**: Device name uniqueness validation.
  - **Error Handling**: Comprehensive exception handling with detailed error messages.
  - **Timestamps**: Automatic creation and update timestamps.

## Device Entity

A device has the following attributes:

  - `id` (UUID) - Automatically generated unique identifier.
  - `name` (String) - Unique device name (min 3, max 100 characters).
  - `description` (String) - Device description (optional, max 500 characters).
  - `address` (String) - Physical location (optional, max 200 characters).
  - `maxConsumption` (Double) - Maximum energy consumption in watts (required, positive).
  - `userId` (UUID) - Associated user ID (optional, null if unassigned).
  - `status` (Enum) - ACTIVE, INACTIVE, or MAINTENANCE.
  - `createdAt` (LocalDateTime) - Creation timestamp.
  - `updatedAt` (LocalDateTime) - Last update timestamp.

## Prerequisites

  - **Java JDK 17**
  - **PostgreSQL** server accessible from the app
  - **Postman** (optional) for testing the API

## Database Setup (PostgreSQL)

Create the database before running the application:

```sql
CREATE DATABASE device_db;
```

Default connection values (for local development, from `device_microservice/README.md`):

```
DB_IP=localhost
DB_PORT=5433
DB_USER=postgres
DB_PASSWORD=postgres
DB_DBNAME=device_db
```

> Note: Hibernate is set to `spring.jpa.hibernate.ddl-auto=update`, so the `devices` table will be created automatically on first run.

## Configuration

All settings are in `src/main/resources/application.properties`. Override them via environment variables:

| Purpose | Property | Env var | Default |
|---|---|---|---|
| DB host | `database.ip` | `DB_IP` | `device-db` |
| DB port | `database.port` | `DB_PORT` | `5432` |
| DB user | `database.user` | `DB_USER` | `postgres` |
| DB password | `database.password` | `DB_PASSWORD` | `postgres` |
| DB name | `database.name` | `DB_DBNAME` | `device_db` |
| HTTP port | `server.port` | `PORT` | `8080` |

*(Note: Defaults listed are from `application.properties` for containerized deployment. See "How to Run (Local)" for local settings.)*

## How to Run (Local)

From the project root (`demo/`):

```bash
# 1) Set environment variables (if needed)
export DB_IP=localhost
export DB_PORT=5433
export DB_USER=postgres
export DB_PASSWORD=postgres
export DB_DBNAME=device_db
export PORT=8081

# 2) Run the application
./mvnw spring-boot:run
```

The application will start on **http://localhost:8081**

## API Endpoints

### Devices Resource (`/devices`)

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| GET | `/devices` | List all devices | - | 200 OK, List of DeviceDTO |
| GET | `/devices/{id}` | Get device by ID | - | 200 OK, DeviceDetailsDTO |
| GET | `/devices/user/{userId}` | Get all devices for a user | - | 200 OK, List of DeviceDTO |
| GET | `/devices/status/{status}` | Get devices by status (ACTIVE/INACTIVE/MAINTENANCE) | - | 200 OK, List of DeviceDTO |
| GET | `/devices/unassigned` | Get unassigned devices | - | 200 OK, List of DeviceDTO |
| GET | `/devices/user/{userId}/count` | Count devices for a user | - | 200 OK, Long |
| POST | `/devices` | Create new device | DeviceDetailsDTO | 201 Created, Location header |
| PUT | `/devices/{id}` | Update device | DeviceDetailsDTO | 204 No Content |
| PUT | `/devices/{deviceId}/assign/{userId}` | Assign device to user | - | 204 No Content |
| PUT | `/devices/{deviceId}/unassign` | Unassign device from user | - | 204 No Content |
| DELETE | `/devices/{id}` | Delete device | - | 204 No Content |

### Request/Response Examples

**Create Device (POST /devices)**

```json
{
  "name": "Smart Meter SM-001",
  "description": "Main building smart meter",
  "address": "Building A, Floor 1",
  "maxConsumption": 5000.0,
  "status": "ACTIVE"
}
```

**Device Response (GET /devices/{id})**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Smart Meter SM-001",
  "description": "Main building smart meter",
  "address": "Building A, Floor 1",
  "maxConsumption": 5000.0,
  "userId": "987e6543-e21b-12d3-a456-426614174000",
  "status": "ACTIVE",
  "createdAt": "2025-10-15T10:30:00",
  "updatedAt": "2025-10-15T10:30:00"
}
```

## Testing with Postman

1.  Import the collection file: `postman_collection.json`.
2.  Set collection variables:
      - `baseUrl` → `http://localhost:8081`
      - `resource` → `devices`
      - `userId` → (UUID of an existing user from User Microservice)
3.  Run requests in order (the collection includes tests that store `deviceId` after creation).

## Validation Rules

  - **Name**: Required, 3-100 characters, must be unique.
  - **Description**: Optional, max 500 characters.
  - **Address**: Optional, max 200 characters.
  - **Max Consumption**: Required, must be positive number.
  - **Status**: Required, must be ACTIVE, INACTIVE, or MAINTENANCE.

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
    "name: Device name must be between 3 and 100 characters"
  ],
  "path": "uri=/devices"
}
```

## Security Notes

  - API endpoints are secured and require a valid JWT, with the exception of the health check.
  - Device name uniqueness is enforced at the database level.
  - All validation errors are returned with clear messages.

## Integration with Other Microservices

This Device Microservice is designed to work with:

  - **User Microservice**: Provides user IDs for device assignment.
  - **Authentication Microservice**: Provides JWTs for securing the endpoints.
  - **API Gateway**: Routes requests and performs authorization.