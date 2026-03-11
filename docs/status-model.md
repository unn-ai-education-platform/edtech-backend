# status-model.md

## Purpose

Defines the canonical mapping between internal workflow state and user-visible state in the MVP.

## Core rule

In MVP:
- `TeacherDecision` has no draft state.
- Creating `TeacherDecision` finalizes the result and makes it visible to the student immediately.
- `decidedAt` is the timestamp of final teacher approval (and publication).

## Internal states

### EvaluationJob.status (internal)

- `QUEUED`
- `RUNNING`
- `DONE`
- `FAILED`

## Student-visible states

Students must not see internal workflow details.
Student-visible states are:

- `UNDER_REVIEW`
- `FINAL_READY`

### Mapping (student)

| EvaluationJob.status | TeacherDecision exists | Student state |
|---|---:|---|
| `QUEUED` | no | `UNDER_REVIEW` |
| `RUNNING` | no | `UNDER_REVIEW` |
| `DONE` | no | `UNDER_REVIEW` |
| `FAILED` | no | `UNDER_REVIEW` |
| `DONE` | yes | `FINAL_READY` |

Notes:
- Internal failures are not exposed to students in MVP; the student remains in `UNDER_REVIEW`.
- Students receive the final result only when `TeacherDecision` exists.

## Teacher-visible operational states (optional UI)

Teacher UI may derive:

- `AI_NOT_READY` (job `QUEUED` or `RUNNING`)
- `AI_READY` (job `DONE` and no decision yet)
- `AI_FAILED` (job `FAILED`)

Mapping:

| EvaluationJob.status | TeacherDecision exists | Teacher operational state |
|---|---:|---|
| `QUEUED` | no | `AI_NOT_READY` |
| `RUNNING` | no | `AI_NOT_READY` |
| `DONE` | no | `AI_READY` |
| `FAILED` | no | `AI_FAILED` |
| `DONE` | yes | `FINAL_DECIDED` |

## Canonical endpoints

- Student: `GET /api/v1/submissions/{id}` (aggregate: status + final decision if ready)
- Teacher: `GET /api/v1/submissions/{id}`, `GET /api/v1/jobs/{jobId}`, `GET /api/v1/submissions/{id}/ai-result`, `GET /api/v1/submissions/{id}/decision`
