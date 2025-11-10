# Frontend Web Application — Energy Management System

A single-page application (SPA) dashboard for interacting with the Energy Management System. It provides a role-based user interface for clients and administrators to manage users and devices by consuming the backend REST APIs.

## Overview

This microservice is the main user interface for the entire system. It is a static HTML/CSS/JavaScript file served by an **Nginx** web server. It handles user login, parses JWTs to determine roles, and displays different views for `ADMIN` and `CLIENT` users.

## Project Structure

```
frontend/
└── index.html    # The complete single-page application (HTML, CSS, JS)
```

## Features

  - **User Login**: Authenticates users against the `/api/auth/login` endpoint.
  - **Role-Based Views**: Decodes the received JWT to show a specific dashboard for `ADMIN` or `CLIENT` roles.
  - **Admin Dashboard**:
      - Full CRUD (Create, Read, Update, Delete) operations on **users**.
      - Full CRUD operations on **devices**.
      - Assign and unassign devices from users.
  - **Client Dashboard**:
      - View a read-only list of all devices assigned to their own account.
  - **Dynamic UI**: Uses modals for creating/updating entities and showing success/error notifications.

## Core Technologies

  - **HTML5**: Provides the structure for the three main views (Login, Admin, Client).
  - **Tailwind CSS**: Used for all styling, loaded via a CDN.
  - **Vanilla JavaScript (ES6+)**: Handles all application logic, API `fetch` calls, DOM manipulation, and state management.
  - **Nginx**: A high-performance web server used to serve the static `index.html` file inside a Docker container.

## Prerequisites

  - A modern web browser.
  - **Docker and Docker Compose** to run the application stack.

## Configuration

The primary configuration is inside the `<script>` block of `index.html`:

  - `API_BASE = '/api'`: This constant tells the frontend to send all API requests to the `/api` path, which is then handled by the Traefik reverse proxy.

## How to Run

This service is not intended to be run standalone. It is started as part of the entire application stack using the main `docker-compose.yml` in the project root.

```bash
# 1) From the project root (where docker-compose.yml is)
docker compose up -d

# 2) Access the application in your browser
```

The application will be available at **http://localhost:8000** (via the Traefik reverse proxy).

## API Endpoints Consumed

This frontend does not provide any endpoints. It consumes the following endpoints from the API gateway:

| Method | Endpoint | Description | Role |
|---|---|---|---|
| `POST` | `/api/auth/login` | Log in and get JWT | `CLIENT`, `ADMIN` |
| `GET` | `/api/users` | Get all users | `ADMIN` |
| `POST` | `/api/users` | Create a new user | `ADMIN` |
| `PUT` | `/api/users/{id}` | Update an existing user | `ADMIN` |
| `DELETE` | `/api/users/{id}` | Delete a user | `ADMIN` |
| `GET` | `/api/devices` | Get all devices | `ADMIN` |
| `GET` | `/api/devices/user/{userId}` | Get devices for a specific user | `CLIENT` |
| `POST` | `/api/devices` | Create a new device | `ADMIN` |
| `PUT` | `/api/devices/{id}` | Update an existing device | `ADMIN` |
| `DELETE` | `/api/devices/{id}` | Delete a device | `ADMIN` |
| `PUT` | `/api/devices/{deviceId}/assign/{userId}` | Assign a device to a user | `ADMIN` |
| `PUT` | `/api/devices/{deviceId}/unassign` | Unassign a device | `ADMIN` |

## Security and Application Flow

1.  The user is always shown the **Login View** (`#login-view`) on first load.
2.  On successful login, the `accessToken` (JWT) is stored in a JavaScript variable.
3.  The JWT is decoded to extract the `role` and `userId` claims.
4.  Based on the `role`, either the **Admin View** (`#admin-view`) or **Client View** (`#client-view`) is shown.
5.  Every subsequent API request to `/api/*` includes the `Authorization: Bearer <token>` header.
6.  If any API call returns a `401 Unauthorized`, the application automatically logs the user out and returns them to the login view.

## Integration with Other Microservices

This frontend is the primary consumer of the entire backend stack. It sends all API requests to the **Traefik Reverse Proxy**, which then routes them to the correct microservice (`auth-service`, `user-service`, or `device-service`).