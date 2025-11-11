# Energy Management System

This is a full-stack, multi-container application demonstrating a microservices architecture for an Energy Management System. The backend is built with Spring Boot, secured with JWT, and persisted with PostgreSQL. The system is orchestrated with Docker Compose, and all traffic is managed through a Traefik reverse proxy.

## Architecture & Services

This `docker-compose.yml` file will create and manage 8 containers that form the complete application stack:

  * **`frontend`**: An Nginx container that serves the static `index.html` Single-Page Application (SPA) dashboard.
  * **`traefik`**: The reverse proxy (API Gateway) that acts as the single entry point for all traffic. It routes requests to the correct service and provides a monitoring dashboard.
  * **`auth-service`**: The Spring Boot microservice responsible for user registration, login, logout, and JWT management.
  * **`user-service`**: The Spring Boot microservice responsible for all CRUD operations on users, including role management.
  * **`device-service`**: The Spring Boot microservice responsible for all CRUD operations on devices and managing user-device associations.
  * **`auth-db`**: A PostgreSQL database for the `auth-service` (stores refresh tokens).
  * **`user-db`**: A PostgreSQL database for the `user-service`.
  * **`device-db`**: A PostgreSQL database for the `device-service`.

## How to Run (Recommended Method)

The entire application stack is designed to be run with a single Docker Compose command.

### Prerequisites

  * Docker
  * WSL (strongly recommended)

### 1\. Create and Start All Containers

Open a terminal in the root folder of the project (where `docker-compose.yml` is located).

**Recommended First-Time Run:**
This command builds all microservice images from their `Dockerfile`s and starts all 8 services in detached (background) mode.

```bash
docker compose up -d --build
```

**Standard Start (after images are built):**
To simply start the containers without rebuilding them:

```bash
docker compose up -d
```

**Start with Live Logs (for debugging):**
To start the application and see all logs from all containers in your terminal:

```bash
docker compose up
```

### 2\. Accessing the Application

  * **Frontend Application**: [http://localhost:8000](https://www.google.com/search?q=http://localhost:8000)
  * **Traefik Dashboard (Monitoring)**: [http://localhost:8081](https://www.google.com/search?q=http://localhost:8081)

### 3\. Check Docker Containers

  * **List all running containers:**
    ```bash
    docker ps
    ```
  * **View logs for a specific service (e.g., `user-service`):**
    ```bash
    docker compose logs -f user-service
    ```
    (You can also use `auth-service`, `device-service`, `traefik`, etc.)

### 4\. Stop All Containers

This command will stop and **remove** all containers associated with the project.

```bash
docker compose down
```

### 5\. Clean Up (Optional)

  * **To stop containers AND remove data volumes** (deletes all database data):
    ```bash
    docker compose down -v
    ```
  * **To remove unused Docker images** (clears old build versions):
    ```bash
    docker image prune
    ```

-----

## System & API Overview

### API Gateway & Routing Rules

The `traefik` reverse proxy is the single entry point for all requests. It routes traffic based on the URL path, as defined in `reverse_proxy/dynamic/path.yml`.

| External Path (`localhost:8000`) | Target Service | Internal URL | Middleware |
| :--- | :--- | :--- | :--- |
| `/` | `frontend` | `http://frontend:80` | None |
| `/api/auth/*` | `auth-service` | `http://auth-service:8082` | None |
| `/api/users/*` | `user-service` | `http://user-service:8080` | `strip-api-prefix` |
| `/api/devices/*` | `device-service` | `http://device-service:8080` | `strip-api-prefix` |

-----

## Microservice Details

### 1\. `frontend`

A single-page application (SPA) dashboard served by an Nginx container.

  * **Technology**: HTML5, Tailwind CSS (via CDN), Vanilla JavaScript (ES6+).
  * **Features**:
      * **User Login**: Authenticates users against the `/api/auth/login` endpoint.
      * **Role-Based Views**: Decodes the received JWT to show a specific dashboard for `ADMIN` or `CLIENT` roles.
      * **Admin Dashboard**:
          * Full CRUD (Create, Read, Update, Delete) on **users**.
          * Full CRUD on **devices**.
          * Assign and unassign devices from users.
      * **Client Dashboard**:
          * View a read-only list of all devices assigned to their own account.
      * **Dynamic UI**: Uses modals for creating/updating entities and showing notifications.
  * **Configuration**: The `API_BASE` variable in `index.html` is set to `/api`, directing all calls to the Traefik proxy.

### 2\. `reverse_proxy` (Traefik)

The central API Gateway that manages all web traffic.

  * **Technology**: Traefik v3.0
  * **Features**:
      * **Path-Based Routing**: Maps public paths like `/api/users` to the correct internal microservice.
      * **Middleware**: Uses `stripPrefix` to remove `/api` from paths before forwarding.
      * **Monitoring**: Provides a dashboard at `http://localhost:8081` to visualize routes and service health.
      * **Health Checks**: Actively monitors the `/actuator/health` endpoint of backend services.
  * **Configuration**:
      * `traefik.yml`: Static configuration (entry points, dashboard, logging).
      * `dynamic/path.yml`: Dynamic routing rules, middlewares, and service definitions.

### 3\. `auth-service`

Spring Boot microservice for user authentication and token management.

  * **Technology**: Spring Boot, Spring Security, JWT, PostgreSQL.
  * **Features**:
      * Handles user registration by proxying requests to the `user-service`.
      * Handles user login by validating credentials (from `user-service`) and generating JWTs.
      * Manages JWT access and refresh tokens.
      * Stores refresh tokens in the `auth-db` PostgreSQL database.
  * **API Endpoints (`/api/auth`)**:
    | Method | Endpoint | Description |
    | :--- | :--- | :--- |
    | `POST` | `/register` | Registers a new user (via `user-service`). |
    | `POST` | `/login` | Authenticates a user and returns an `accessToken` and `refreshToken`. |
    | `POST` | `/refresh` | Accepts a valid `refreshToken` and returns a new `accessToken`. |
    | `POST` | `/logout` | Deletes the user's `refreshToken` from the database. |

### 4\. `user-service`

Spring Boot microservice for managing all user data.

  * **Technology**: Spring Boot, Spring Security (BCrypt), Spring Data JPA, PostgreSQL.
  * **Features**:
      * Full CRUD operations (Create, Read, Update, Delete) for users.
      * Supports `ADMIN` and `CLIENT` roles.
      * Secures passwords using **BCrypt** encryption.
      * Enforces username uniqueness.
  * **User Entity**:
      * `id` (UUID)
      * `username` (String, unique)
      * `password` (String, encrypted)
      * `email` (String)
      * `name` (String)
      * `role` (Enum: `ADMIN`, `CLIENT`)
  * **API Endpoints (`/api/users`)**:
    | Method | Endpoint | Description |
    | :--- | :--- | :--- |
    | `GET` | `/users` | List all users (returns `UserDTO`). |
    | `POST` | `/users` | Create a new user (accepts `UserDetailsDTO`). |
    | `GET` | `/users/{id}` | Get a single user by ID (returns `UserDetailsDTO`, password is `null`). |
    | `PUT` | `/users/{id}` | Update an existing user (accepts `UserDetailsDTO`). |
    | `DELETE` | `/users/{id}` | Delete a user by ID. |
    | `GET` | `/users/role/{role}` | Get users by role (e.g., `ADMIN` or `CLIENT`). |
    | `GET` | `/users/username/{username}`| Get user by username (used internally by `auth-service`). |

### 5\. `device-service`

Spring Boot microservice for managing smart energy devices.

  * **Technology**: Spring Boot, Spring Data JPA, PostgreSQL.
  * **Features**:
      * Full CRUD operations for devices.
      * Manages device-to-user associations (`userId` field).
      * Supports device statuses (`ACTIVE`, `INACTIVE`, `MAINTENANCE`).
      * Filters devices by user, status, or unassigned.
  * **Device Entity**:
      * `id` (UUID)
      * `name` (String, unique)
      * `description` (String)
      * `address` (String)
      * `maxConsumption` (Double)
      * `userId` (UUID, nullable)
      * `status` (Enum: `ACTIVE`, `INACTIVE`, `MAINTENANCE`)
      * `createdAt`, `updatedAt` (LocalDateTime)
  * **API Endpoints (`/api/devices`)**:
    | Method | Endpoint | Description |
    | :--- | :--- | :--- |
    | `GET` | `/devices` | List all devices. |
    | `POST` | `/devices` | Create a new device. |
    | `GET` | `/devices/{id}` | Get a single device by ID. |
    | `PUT` | `/devices/{id}` | Update an existing device. |
    | `DELETE` | `/devices/{id}` | Delete a device by ID. |
    | `GET` | `/devices/user/{userId}` | Get all devices assigned to a specific user. |
    | `GET` | `/devices/status/{status}` | Get devices by status (e.g., `ACTIVE`). |
    | `GET` | `/devices/unassigned` | Get all devices with no assigned user. |
    | `GET` | `/devices/user/{userId}/count` | Count devices assigned to a specific user. |
    | `PUT` | `/devices/{deviceId}/assign/{userId}` | Assign a device to a user. |
    | `PUT` | `/devices/{deviceId}/unassign` | Unassign a device (sets `userId` to `null`). |

-----

## Local Development (Advanced)

This section is for advanced development and debugging. **Running with Docker Compose is the recommended method.**

If you need to run the Spring Boot microservices locally (e.g., to use an IDE's debugger), you must manually replicate the environment.

### Prerequisites

1.  **Java JDK 17**
2.  **Maven**
3.  **PostgreSQL Server**: You must have a PostgreSQL server running locally or accessible. You must also create the three separate databases:
      * `CREATE DATABASE user_db;`
      * `CREATE DATABASE device_db;`
      * `CREATE DATABASE auth_db;`

### Running a Service Locally (Example: `user-service`)

You must repeat this process for all three backend microservices.

1.  **Navigate to the service directory:**

    ```bash
    cd user_microservice/demo
    ```

2.  **Set Environment Variables:** The application reads settings from `application.properties`, which are configured for Docker. You must override them with environment variables for your local setup.

    ```bash
    # Example for user-service
    export DB_IP=localhost
    export DB_PORT=5432
    export DB_USER=your_local_postgres_user
    export DB_PASSWORD=your_local_postgres_password
    export DB_DBNAME=user_db
    export PORT=8080 

    # Example for device-service (in a separate terminal)
    export DB_IP=localhost
    export DB_PORT=5432
    export DB_USER=your_local_postgres_user
    export DB_PASSWORD=your_local_postgres_password
    export DB_DBNAME=device_db
    export PORT=8081 # Must use a different port from user-service

    # Example for auth-service (in a separate terminal)
    export DB_IP=localhost
    export DB_PORT=5432
    export DB_USER=your_local_postgres_user
    export DB_PASSWORD=your_local_postgres_password
    export DB_DBNAME=auth_db
    export PORT=8082 # Must use a different port
    export app_user-service_url=http://localhost:8080 # Tell auth-service where user-service is
    ```

3.  **Run the application:**

    ```bash
    ./mvnw spring-boot:run
    ```

**Note:** When running locally, you will also need to run the `frontend` and `reverse_proxy` (Traefik) via Docker to have a complete, functional system. You would need to edit `reverse_proxy/dynamic/path.yml` to point the services to `http://host.docker.internal:8080` (or your local IP) instead of the Docker service names. This complexity is why using Docker Compose for everything is strongly recommended.