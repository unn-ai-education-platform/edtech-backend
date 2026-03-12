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

#### `POST /api/v1/rubrics` (teacher)

Текущая реализованная форма запроса:

```json
{
  "name": "Essay rubric",
  "criteria": [
    {
      "name": "Content",
      "description": "optional",
      "weight": 60
    },
    {
      "name": "Style",
      "description": "optional",
      "weight": 40
    }
  ],
  "gradeScheme": {
    "bands": []
  }
}
```

Текущая реализованная форма ответа:

```json
{
  "id": "uuid",
  "name": "Essay rubric",
  "criteria": [],
  "gradeScheme": {},
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

Правила:
- Endpoint доступен только `TEACHER`.
- `criteria` не должен быть пустым.
- Вес каждого критерия должен быть в диапазоне `0..100`.
- Сумма весов критериев должна быть равна `100`.
- `gradeScheme` должен быть JSON object или JSON array.

Замечание по реализации:
- `criteria` и `gradeScheme` сохраняются как целые JSON-документы в существующие `jsonb`-поля схемы.
- Это соответствует текущему `V1__init.sql` и является осознанным решением для MVP.

#### `GET /api/v1/rubrics/{id}` (teacher)

Возвращает полное текущее состояние rubric в той же форме, что и `POST /api/v1/rubrics`.

MVP note:
- Rubrics are immutable after creation.

Ошибки:
- `404 RUBRIC_NOT_FOUND`, если rubric не существует.

Ошибки для Rubrics:
- `400 INVALID_REQUEST_HEADER` — отсутствуют или невалидны обязательные заголовки;
- `400 INVALID_REQUEST` — payload нарушает инварианты rubric;
- `403 FORBIDDEN` — роль пользователя не `TEACHER`;
- `404 RUBRIC_NOT_FOUND` — rubric не найден.

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

Rules:
- Endpoint is available only to `STUDENT`.
- `rubricId` must reference an existing rubric.

Errors:
- `404 RUBRIC_NOT_FOUND` if the rubric does not exist.

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

Access / errors:
- `STUDENT` can read only their own submission.
- `TEACHER` can read any submission.
- `404 SUBMISSION_NOT_FOUND` if the submission does not exist.
- `403 FORBIDDEN` if a student tries to access another student's submission.

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
- Teacher-only endpoint.
- At most one active job (`QUEUED` or `RUNNING`) may exist per submission.

Errors:
- `404 SUBMISSION_NOT_FOUND` if the submission does not exist.
- `400 INVALID_REQUEST` if an active job already exists for the submission.

#### `GET /api/v1/jobs/{jobId}` (teacher)

Returns internal job status:
- `QUEUED | RUNNING | DONE | FAILED`

Errors:
- `404 JOB_NOT_FOUND` if the job does not exist.

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
