# ConnectHub — Backend

> Real-Time Chat Platform · Spring Boot · WebSocket · STOMP · Java

---

## Overview

ConnectHub is a full-stack real-time chat platform built on **Java Spring Boot** with **Spring WebSocket + STOMP + SockJS** — a production-grade Socket.io alternative for the JVM. It supports direct messages, group channels, file sharing, emoji reactions, presence tracking, and push notifications, all delivered over persistent WebSocket connections with sub-100ms latency.

---

## Technology Stack

| Layer | Technology                                                        |
|---|-------------------------------------------------------------------|
| Framework | Java Spring Boot (MVC, Security, Data JPA, WebSocket)             |
| Real-time | STOMP over WebSocket · SockJS fallback                            |
| Auth | JWT via STOMP CONNECT header · Google/GitHub OAuth2               |
| Database | MySQL (persistence) · Redis (presence, session registry, Pub/Sub) |
| File Storage | AWS S3 · CloudFront CDN · Thumbnailator (image processing)        |
| Containerisation | Docker · Docker Compose                                           |
| Docs | Swagger/OpenAPI 3.0 · AsyncAPI 2.6, Eureka Server                 |

---

## Microservices

| Service                | Package                       | Responsibility                                                |
|------------------------|-------------------------------|---------------------------------------------------------------|
| `api-gateway`          | `com.connecthub.api`          | Centralized way to pass requests                              |
| `auth-service`         | `com.connecthub.auth`         | User accounts, JWT, OAuth2, status, last-seen                 |
| `room-service`         | `com.connecthub.room`         | Room CRUD, membership, roles, mute, unread count              |
| `message-service`      | `com.connecthub.message`      | Persistence, pagination, edit/delete, delivery status, search |
| `media-service`        | `com.connecthub.media`        | S3 upload, thumbnail generation, media gallery                |
| `presence-service`     | `com.connecthub.presence`     | Live status, session tracking, bulk reads, stale cleanup      |
| `notification-service` | `com.connecthub.notification` | In-app alerts, mentions                                       |
| `eureka-service`       | `com.connecthub.eureka`       | Service Monitoring Up/Down                                    |

---

## WebSocket Architecture

### STOMP Subscription Topics
| Destination | Purpose |
|---|---|
| `/topic/room/{roomId}` | All messages and events for a room |
| `/topic/user/{userId}` | Personal alerts and DM notifications |
| `/topic/presence` | Platform-wide online/offline broadcasts |

### Inbound Endpoints
| Endpoint | Event |
|---|---|
| `/app/chat.send` | Send a chat message |
| `/app/chat.typing` | Broadcast typing indicator |
| `/app/chat.read` | Send read receipt |

### STOMP Event Types
`CHAT_MESSAGE` · `TYPING_INDICATOR` · `READ_RECEIPT` · `REACTION` · `PRESENCE_UPDATE` · `MESSAGE_EDIT` · `MESSAGE_DELETE`

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- Redis 7+
- AWS account (S3, SES)
- Rabbit MQ

### Configuration

Copy and populate the environment file:

```bash
cp src/main/resources/application.example.yml src/main/resources/application.yml
```

### Run Locally

```bash
# Start dependencies
docker-compose up -d mysql redis

# Build and run
mvn clean install
mvn spring-boot:run
```

### Run with Docker Compose

```bash
docker-compose up --build
```

---

## REST API

Interactive API docs available at:

```
http://localhost:8080/swagger-ui.html
```

Base path: `/api/v1`

| Service | Base Route |
|---|---|
| Auth | `/auth` |
| Rooms | `/rooms` |
| Messages | `/messages` |
| Media | `/media` |
| Presence | `/presence` |
| Notifications | `/notifications` |

---

## Non-Functional Requirements

| Category | Target |
|---|---|
| Latency | < 100ms message delivery (same instance) |
| Availability | 99.9% uptime SLA · SockJS fallback |
| Scalability | Sticky-session load balancing · Redis Pub/Sub fan-out |
| Security | TLS · JWT on every WS upgrade · XSS sanitisation |
| Session Cleanup | Stale sessions purged after 60s via `@Scheduled` job |
| File Limits | Max 25MB · JPEG/PNG/GIF/WebP/PDF/DOCX/ZIP |

---

## Project Structure

```
connecthub-backend/
├── api-gateway/
├── auth-service/
├── room-service/
├── message-service/
├── media-service/
├── presence-service/
├── notification-service/
├── eureka-service/
├── docker-compose.yml
└── pom.xml
```

---

## Roles

| Role | Permissions |
|---|---|
| User | Send messages, manage profile, join/leave rooms |
| Room Admin | Moderate members, pin/delete messages, clear history |
| Platform Admin | Full user/room management, analytics, broadcast |

---

*ConnectHub Platform · Version 1.0 · 2026 · Confidential — Internal Use Only*