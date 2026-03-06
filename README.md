# EdTech Backend (MVP)

Spring Boot backend for **AI-assisted grading of text submissions** with a **teacher-in-the-loop** workflow.

- Students submit **TEXT** assignments.
- The system generates an internal AI draft evaluation.
- Teachers review/override and create the final **TeacherDecision**.
- Students see only a generic “under review” status until the final teacher-approved result is ready.

## Repo guide

- Project rules for Codex/agents: `AGENTS.md`
- Product & API documentation: `docs/README.md`

## Requirements

- JDK **25** (see `pom.xml`)
- PostgreSQL (local dev)

## Run locally

1) Create a local PostgreSQL database and user (or adjust `application-local.properties`):

- DB: `ai-edtech`
- Host: `localhost:5432`

2) Run the app with the `local` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Flyway migrations will run on startup (`spring.jpa.hibernate.ddl-auto=validate`).

## Health endpoints

These are intended to be accessible without user headers (operational health checks):

- `GET /api/v1/ping` → `ok`
- `/actuator/health*` (if exposed)

Business endpoints use request headers `X-User-Id` and `X-User-Role`.

## Test

```bash
./mvnw test
```
