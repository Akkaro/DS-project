# Docker Deployment for the Energy Management System

This document provides instructions for building and running the entire multi-container application stack for the Energy Management System, including all microservices, databases, and the reverse proxy.

## Overview of Services

This `docker-compose.yml` file will create and manage 8 containers:

  * **`traefik`**: The reverse proxy (API Gateway) that acts as the single entry point for all traffic. It routes requests to the correct service.
  * **`frontend`**: An Nginx container that serves the static `index.html` frontend application.
  * **`auth-service`**: The Spring Boot microservice responsible for user registration, login, and token management.
  * **`user-service`**: The Spring Boot microservice responsible for all CRUD operations on users.
  * **`device-service`**: The Spring Boot microservice responsible for all CRUD operations on devices.
  * **`auth-db`**: A PostgreSQL database for the `auth-service` (stores refresh tokens).
  * **`user-db`**: A PostgreSQL database for the `user-service`.
  * **`device-db`**: A PostgreSQL database for the `device-service`.

## Docker Configuration Files

The deployment is managed by several key configuration files:

1.  [`docker-compose.yml`](https://www.google.com/search?q=docker-compose.yml) – Defines all 8 services (containers), environment variables, ports, volumes, and the private network. It orchestrates the entire application build and startup.
2.  **`Dockerfile`s** – Each microservice has its own Dockerfile that specifies how to build its production image.
      * `auth_microservice/demo/Dockerfile`
      * `user_microservice/demo/Dockerfile`
      * `device_microservice/demo/Dockerfile`
3.  **Traefik Configuration**
      * `reverse_proxy/traefik.yml`: The static configuration for Traefik, defining entry points (ports) and the dashboard.
      * `reverse_proxy/dynamic/path.yml`: The dynamic configuration, defining all the routing rules that map paths like `/api/users` to the correct service.

## How to Run

### Prerequisites

  * Docker
  * Docker Compose

### 1\. Create and Start All Containers

Open a terminal **in the root folder** of the project (where `docker-compose.yml` is located).

The `docker-compose.yml` file is configured to automatically create the `ems-network` Docker network, so you do not need to create it manually.

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