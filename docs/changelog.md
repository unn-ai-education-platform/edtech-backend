# changelog.md

## 2026-03-06

Initial documentation baseline aligned to the repository foundation:

- Student-visible status is generic: `UNDER_REVIEW` → `FINAL_READY` (no AI exposure).
- `TeacherDecision` has no draft state in MVP; creation finalizes and publishes immediately.
- `decidedAt` is the approval/publication timestamp in MVP (no `publishedAt`).
- DB invariants documented as enforced by `V1__init.sql`:
  - max 1 active evaluation job per submission,
  - max 1 AI result per submission (MVP),
  - max 1 teacher decision per submission (MVP),
  - `ACCEPT_AI` requires `aiResultId`,
  - `totalScoreNormalized` is constrained to `0..100`.
- Health endpoints are public (no user headers): `GET /api/v1/ping`, `/actuator/health*` (if exposed).

## 2026-03-11

Уточнено текущее поведение API для Rubrics:

- Зафиксирована текущая форма request/response для `POST /api/v1/rubrics` и `GET /api/v1/rubrics/{id}`.
- Rubrics сделаны immutable after creation в рамках MVP; `PATCH /api/v1/rubrics/{id}` больше не поддерживается.
- Зафиксированы rubric-specific error codes:
  - `INVALID_REQUEST`
  - `RUBRIC_NOT_FOUND`
- Временное ограничение:
  - поведение покрыто unit/web tests;
  - отдельные PostgreSQL integration tests для persistence mapping еще не добавлены и должны появиться позже вместе с полноценной test DB strategy.
