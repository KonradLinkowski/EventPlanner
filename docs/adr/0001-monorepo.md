# ADR 0001: Monorepo for all services and the frontend

## Status
Accepted

## Context
EventHub is split into several Spring Boot microservices (API gateway, event,
booking, payment, notification) plus an Angular frontend and supporting infra
(Keycloak realm, Docker Compose, Kubernetes manifests, Prometheus/Grafana
config). As a solo, educational project, we need to optimize for ease of
local development, atomic cross-cutting changes (e.g. updating a shared
Kafka contract and its consumer in the same commit), and simple CI, rather
than for independent release cadences or large multi-team ownership.

## Decision
Use a single monorepo (`eventhub/`) containing all services under
`services/`, the Angular app under `frontend/`, and shared infra/docs under
`infra/` and `docs/`. Each backend service keeps its own Maven module and
`pom.xml`, managed by a parent POM at the repository root. Services remain
independently deployable (own Dockerfile, own CI job via path filters) even
though they live in one repository.

## Consequences
- Pros: one `git clone`, atomic commits across service boundaries, shared
  tooling (parent POM, CI workflows, editorconfig), simpler local
  onboarding.
- Cons: coarser git history, requires path-based CI filters to avoid
  rebuilding everything on every change, no per-service access control.
- We explicitly avoid a shared JPA entity/library between services (see
  section 5 of `plan.md`) to keep services loosely coupled despite sharing a
  repository.
- Revisit if the project ever needs independent team ownership or release
  trains per service (unlikely for this educational project).
