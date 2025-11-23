# Energy Management System - Assignment 2 Update

This document details the architectural evolution and new components introduced in Assignment 2. The system has transitioned from a synchronous REST architecture to a distributed, event-driven system powered by RabbitMQ.

## Architectural Changes

### 1. Asynchronous Communication (RabbitMQ)
We introduced **RabbitMQ** as the message broker to decouple services and enable asynchronous data processing.
* **`sensor.data.queue`**: Handles high-frequency energy measurements sent by the simulator.
* **`sync.queue`**: Handles synchronization events (device creation/deletion) broadcast by the core services to the monitoring service.

### 2. New Microservices

#### **Device Simulator (Producer)**
A standalone Java Spring Boot application that acts as a smart meter.
* **Functionality:** Reads historical data from a CSV file (`sensor.csv`) containing timestamps, device IDs, and energy values.
* **Behavior:** Publishes JSON messages to `sensor.data.queue` at a configurable rate (e.g., simulating 1 hour of data every second).
* **Configuration:** Uses the `DEVICE_ID` environment variable to map data to a specific registered device.

#### **Monitoring Microservice (Consumer)**
A new service dedicated to data ingestion and analytics.
* **Data Ingestion:** Consumes messages from `sensor.data.queue`.
* **Buffering Logic:** Implements a sliding window buffer. It aggregates 6 consecutive measurements (representing 1 hour) before saving a single `HourlyConsumption` record to its dedicated `monitoring_db`.
* **Synchronization:** Listens to `sync.queue` to maintain a local cache of valid Devices. This allows it to validate incoming sensor data without making synchronous HTTP calls to the Device Service.
* **API:** Exposes endpoints for the frontend to fetch historical consumption data.

### 3. Legacy Service Refactoring
The existing services were updated to support the new event-driven flow:
* **`device-service`**: Now publishes `create_device` and `delete_device` events to `sync.queue` whenever an admin manages devices.
* **`user-service`**: Wired up to RabbitMQ to support future user-related synchronization events.

### 4. Frontend Visualization
* **Chart.js Integration:** The client dashboard now includes a "View Consumption" feature.
* **Interactive Graph:** Users can select a specific date to view a bar chart of their device's hourly energy consumption, fetched from the Monitoring Service.

### 5. Tracking RabbitMQ messages
* RabbitMQ messages can be tracked accessing **http://localhost:15672**
* Username: guest
* Password: guest
