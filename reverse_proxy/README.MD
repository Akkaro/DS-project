# Traefik Reverse Proxy Setup

An example of a complete reverse proxy implementation using Traefik, Nginx, and a Flask backend with path-based routing and authentication.

## Project Structure

```
traefik-reverse-proxy/
├── docker-compose.yml         # Container orchestration - defines all services
├── traefik.yml                # Traefik static configuration
├── dynamic/
│   └── path.yml               # Traefik routing rules and middlewares
├── nginx/
│   └── html/
│       └── index.html         # Frontend static page
└── backend/
    └── app.py                 # Flask API endpoints
```

### Component Descriptions

**docker-compose.yml**
- Defines three services: Nginx, Flask backend, and Traefik
- Creates the `proxy-network` Docker network for internal communication
- Exposes ports 80 (HTTP) and 8080 (Dashboard) to the host

**traefik.yml**

- Traefik static configuration (entry points, providers, API settings)
- Defines global Traefik behavior and features
- Loaded once at startup

**dynamic/path.yml**
- Contains Traefik routing rules
- Defines middlewares
- Maps services to backend containers

**nginx/html/index.html**
- Static HTML page with JavaScript to call the backend API

**backend/app.py**
- Flask application with REST API endpoints


## How It Works

### Request Flow for Frontend (`http://localhost/`)

1. Browser sends request to `localhost:80`
2. Traefik receives request on port 80
3. Traefik matches routing rule: `Host('localhost')` (no path prefix)
4. Traefik forwards request to `nginx:80` using Docker DNS
5. Nginx serves the HTML file
6. Response returns through Traefik to browser

### Request Flow for API (`http://localhost/api/hello`)

1. Browser sends request to `localhost:80/api/hello`
2. Traefik receives request on port 80
3. Traefik matches routing rule: `Host('localhost') && PathPrefix('/api')`
4. **Middleware 1**: `auth-basic` - Validates username/password
5. **Middleware 2**: `strip-api-prefix` - Removes `/api` → becomes `/hello`
6. Traefik forwards modified request to `backend:5000/hello`
7. Flask processes `/hello` endpoint and returns JSON
8. Response returns through Traefik to browser

### Key Mechanisms

**Docker Networking**
- All containers share the `proxy-network`
- Docker's internal DNS resolves container names to IP addresses
- Containers communicate using names: `nginx`, `backend`, `traefik`
- Only Traefik exposes ports to the host machine

**Path-Based Routing**
- URL path determines destination service
- `/` → Nginx (static content)
- `/api/*` → Backend (dynamic API)

**Middleware Chain**
- Processes requests before reaching backend
- Applied in sequential order
- Can transform, validate, or reject requests

## Functionality

### Routing
- **Frontend Route**: `localhost/` → Nginx static files
- **API Route**: `localhost/api/*` → Flask backend (with prefix stripped)

### Authentication
- Basic Auth protection on API endpoints
- Username: `user` / Password: `test`
- Credentials validated by Traefik before reaching backend

### Monitoring
- Dashboard available at `http://localhost:8080/dashboard/`
- Shows active routes, services, and middlewares
- Access logs for debugging request flow

### Service Discovery
- Traefik auto-discovers services via Docker socket
- File-based configuration for routing rules
- Hot-reload when configuration files change (See comment in `docker-compose.yml`)

## Quick Start

```bash
# Start all services
docker-compose up -d

# Verify containers are running
docker-compose ps

# Enable hot-reloading of Traefik config - cannot be done in detached mode, must keep the terminal open
# Windows only
docker-compose watch

# Access the application
# Frontend: http://localhost/
# API: http://localhost/api/hello (requires auth: user/test)
# Dashboard: http://localhost:8080/dashboard/

# View logs
docker-compose logs -f traefik

# Stop all services
docker-compose down
```

