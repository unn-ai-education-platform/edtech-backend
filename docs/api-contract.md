# api-contract.md

## Purpose

Defines the target MVP API contract (and notes what is currently implemented).

Base path: `/api/v1`

## Access model

### Business endpoints (MVP)

Business endpoints use request headers:

- `X-User-Id`
- `X-User-Role` ∈ `{STUDENT, TEACHER}`
- optional `X-Request-Id` (trace id)

### Public health endpoints

These endpoints must be accessible without `X-User-Id` / `X-User-Role`:

- `GET /api/v1/ping` → `ok`
- `/actuator/health*` (and `/actuator/info` if exposed)

## Error format

Structured JSON errors include:

- `code`
- `message`
- `traceId`

`details` is optional and may be omitted in MVP.

## Implemented endpoints (current repo)

### `GET /api/v1/ping`  (public)

Response (text):
- `ok`

### `GET /api/v1/teacher/ping`  (teacher-only)

Response (JSON):
```json
{ "status": "ok" }
```

Authorization / errors:
- Requires `X-User-Id` and `X-User-Role: TEACHER`.
- Missing/invalid required headers → `400` with error `code: INVALID_REQUEST_HEADER`.
- Non-`TEACHER` role → `403` with error `code: FORBIDDEN`.

## Target MVP endpoints

### Rubrics (teacher)

- `POST /api/v1/rubrics`
- `GET /api/v1/rubrics/{id}`
- `PATCH /api/v1/rubrics/{id}`

### Submissions

#### `POST /api/v1/submissions` (student)

Creates a submission.

Request:
```json
{
  "rubricId": "uuid",
  "text": "string"
}
```

Response:
```json
{
  "submissionId": "uuid",
  "createdAt": "timestamp"
}
```

#### `GET /api/v1/submissions/{id}` (student + teacher)

Canonical aggregate read endpoint.

Student response rules:
- Must not expose AI workflow details.
- Must not expose raw `AIResult`.
- Status is only `UNDER_REVIEW` or `FINAL_READY`.

Student response shape:
```json
{
  "id": "uuid",
  "rubricId": "uuid",
  "status": "UNDER_REVIEW | FINAL_READY",
  "createdAt": "timestamp",
  "decision": null
}
```

When final ready:
```json
{
  "id": "uuid",
  "rubricId": "uuid",
  "status": "FINAL_READY",
  "createdAt": "timestamp",
  "decision": {
    "mode": "ACCEPT_AI | OVERRIDE",
    "criteriaResults": [],
    "comment": "string or null",
    "totalScoreNormalized": 84.50,
    "finalGrade": {},
    "decidedAt": "timestamp"
  }
}
```

#### `GET /api/v1/submissions?status=...` (teacher)

Teacher listing endpoint (workflow-oriented).

### Evaluation

#### `POST /api/v1/submissions/{id}/evaluate` (teacher)

Starts evaluation for a submission.

Response:
```json
{ "jobId": "uuid", "status": "QUEUED" }
```

Rules:
- At most one active job (`QUEUED` or `RUNNING`) may exist per submission.

#### `GET /api/v1/jobs/{jobId}` (teacher)

Returns internal job status:
- `QUEUED | RUNNING | DONE | FAILED`

### AI draft (teacher-only)

#### `GET /api/v1/submissions/{id}/ai-result`

Teacher-only draft view.

### Final decision (teacher)

#### `POST /api/v1/submissions/{id}/decision`

Creates the final teacher-approved result.

MVP semantics:
- `TeacherDecision` has no draft state.
- Creating it finalizes the result and makes it visible to the student immediately.
- `decidedAt` is the publish timestamp in MVP (no `publishedAt`).

Constraints:
- If mode is `ACCEPT_AI`, `aiResultId` must be provided.

#### `GET /api/v1/submissions/{id}/decision` (teacher)

Teacher-only view of final decision payload.


Note:
- Students obtain the final result only via `GET /api/v1/submissions/{id}` (aggregate endpoint). The `/decision` endpoint is teacher-only.
