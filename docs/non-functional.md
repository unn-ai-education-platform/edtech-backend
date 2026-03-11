# non-functional.md

## Privacy

- Submission text is sensitive:
  - do not log it
  - do not include it in error payloads
- Students must not see raw AI draft data (`AIResult`) or internal workflow details.

## Explainability

- Draft and final results are rubric/criterion-based.
- Per-criterion rationale may be stored internally in `criteriaResults`.

## Reliability

- Evaluation is asynchronous.
- The system must enforce “max 1 active evaluation job per submission” to avoid duplicates and races.

## Auditability and traceability

- Preserve request tracing via `X-Request-Id` / `traceId`.
- All error responses include `traceId`.
- Internal entities may store `traceId` for correlation (`evaluation_jobs.trace_id`, `ai_results.trace_id`).

## API stability

- Avoid silent breaking API changes.
- Maintain backward compatibility unless explicitly agreed.

## Operational health

- `GET /api/v1/ping` is a lightweight liveness endpoint.
- `/actuator/health*` may be used for readiness/liveness checks (if exposed).
- Health endpoints should not require user headers.
