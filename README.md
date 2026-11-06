# EventHub

Educational microservices project: an event management and ticket booking
platform, built to gain hands-on mastery of the modern Java ecosystem
(Spring Boot, Kafka, Keycloak, Docker/Kubernetes, CI/CD, observability).

See [plan.md](plan.md) for the full design: requirements, architecture,
API contracts, data model, roadmap and Definition of Done.

## Status

Stage 1 (repository and project foundations) is in progress. See the
roadmap in [plan.md](plan.md#21-implementation-roadmap) for what's next.

## Prerequisites

- JDK 21
- Docker (with Docker Compose) — used for local Postgres and for
  Testcontainers-based tests
- Node.js 20+ and npm (added once the Angular frontend is introduced)

No local Maven installation is required — the repo includes the Maven
Wrapper (`mvnw` / `mvnw.cmd`).

## Quick start

```bash
# 1. Start infrastructure (Postgres)
docker compose -f infra/docker/docker-compose.yml up -d

# 2. Build and test everything
./mvnw verify

# 3. Run the Event Service
./mvnw -pl services/event-service spring-boot:run
```

Check health: `curl http://localhost:8081/actuator/health` should return
`{"status":"UP"}`.

## Repository structure

```
eventhub/
├── pom.xml                 # parent POM (dependency & plugin management)
├── services/               # one Maven module per microservice
│   └── event-service/
├── frontend/                # Angular SPA (added in a later stage)
├── infra/
│   └── docker/              # docker-compose.yml, Postgres init scripts
├── docs/
│   └── adr/                 # Architecture Decision Records
└── plan.md                  # full project plan and roadmap
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branching and commit conventions.
