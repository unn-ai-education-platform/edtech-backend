# scope.md

## In scope (MVP)

- TEXT submissions only.
- Rubric CRUD sufficient to run grading:
  - create rubric
  - read rubric
  - update rubric (minimal)
- Student flow:
  - create submission
  - view submission status and final result (generic status only)
- Teacher flow:
  - view submission list and details
  - run evaluation (creates internal job)
  - view AI draft result (teacher-only)
  - create final teacher decision (accept AI or override)
- Asynchronous evaluation job lifecycle: `QUEUED`, `RUNNING`, `DONE`, `FAILED`.
- Header-based request context (`X-User-Id`, `X-User-Role`) as MVP access model.
- Structured JSON errors with `traceId`.

## Out of scope (MVP)

- Non-text assignments (files, quizzes, matching, etc.).
- Full authentication/authorization (JWT, OAuth, Spring Security rollout).
- Admin role and admin UI.
- Notifications (email/push).
- Multi-tenant organizations, classes/courses, LMS integrations.
- Student visibility of AI drafts or AI workflow states.

## Explicit non-goals

- Students must not be informed that an AI process is used during review.
- No separate publish step for teacher decisions in MVP (creation = publication).
