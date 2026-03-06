# acceptance-criteria.md

## Global rules

- All business endpoints require request headers:
  - `X-User-Id`
  - `X-User-Role` ∈ `{STUDENT, TEACHER}`
  - optional `X-Request-Id` for tracing
- Health endpoints are public (no user headers):
  - `GET /api/v1/ping` returns `ok`
  - `/actuator/health*` (if exposed)
- Errors are JSON with:
  - `code`
  - `message`
  - `traceId`
  - `details` is optional

## Student-facing visibility

- Student-facing responses must not include:
  - raw `AIResult`
  - any “AI” workflow status
  - internal evaluation job details
- Student-visible status values are only:
  - `UNDER_REVIEW`
  - `FINAL_READY`

## Rubrics

1. **Create rubric**
   - Given a teacher request
   - When `POST /api/v1/rubrics` is called with valid rubric payload
   - Then a rubric is stored and the response contains its id and timestamps.

2. **Read rubric**
   - When `GET /api/v1/rubrics/{id}` is called
   - Then rubric details are returned.

## Submissions

1. **Create submission**
   - Given a student request
   - When `POST /api/v1/submissions` is called with `rubricId` and `text`
   - Then a submission is stored and the response returns `submissionId` and `createdAt`.
   - And the submission text is not logged.

2. **Student reads status**
   - When `GET /api/v1/submissions/{id}` is called by the owning student
   - Then response status is `UNDER_REVIEW` until the final teacher decision exists.
   - Then response status becomes `FINAL_READY` and includes the final decision.

## Evaluation jobs

1. **Start evaluation**
   - Given a teacher request
   - When `POST /api/v1/submissions/{id}/evaluate` is called
   - Then an evaluation job is created with status `QUEUED`.

2. **Active job constraint**
   - The system must prevent more than one active job (`QUEUED` or `RUNNING`) per submission.

3. **View job**
   - Given a teacher request
   - When `GET /api/v1/jobs/{jobId}` is called
   - Then the job status is returned (`QUEUED`, `RUNNING`, `DONE`, `FAILED`).

## AI draft

- Given a teacher request
- When `GET /api/v1/submissions/{id}/ai-result` is called
- Then the AI draft evaluation is returned (teacher-only).

## Teacher decision (final)

1. **Finalize decision**
   - Given a teacher request
   - When `POST /api/v1/submissions/{id}/decision` is called
   - Then a `TeacherDecision` is created with `decidedAt` set.
   - And the final result becomes immediately visible to the student (MVP).

2. **Decision constraints**
   - Only one `TeacherDecision` can exist per submission.
   - If decision mode is `ACCEPT_AI`, an `aiResultId` must be provided.

3. **Student reads final decision**
   - When `GET /api/v1/submissions/{id}` is called by the student and a decision exists
   - Then the final decision payload is returned and the status is `FINAL_READY`.
