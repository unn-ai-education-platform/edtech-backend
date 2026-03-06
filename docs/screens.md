# screens.md

This document describes UI states and user-facing copy for the MVP.
**Student UI must not mention AI.**

## Student screens

### S1 — Submit assignment

Inputs:
- Rubric selection
- Text input

Action:
- Submit

Success:
- Navigate to S2 (status).

### S2 — Submission status & result

**State: UNDER_REVIEW**
- Title: “Работа на проверке”
- Body: “Результат появится после завершения проверки.”
- Не упоминать внутренние шаги проверки.

**State: FINAL_READY**
- Title: “Результат готов”
- Show final teacher-approved result:
  - per-criterion results
  - overall score (`totalScoreNormalized`)
  - final grade
  - teacher comment (optional)
  - `decidedAt` timestamp

If the system is delayed internally, the student still sees `UNDER_REVIEW` in MVP.

## Teacher screens

### T1 — Rubric management

- Create rubric (name, criteria with weights, grade scheme)
- Edit rubric

### T2 — Submissions list

Shows submissions with operational state for the teacher:
- AI draft not ready
- AI draft ready
- evaluation failed
- final decision exists

Teacher-only: list and filter capabilities.

### T3 — Submission review

Sections:
- Student submission text (teacher-only; do not log)
- AI draft (teacher-only; if available)
- Teacher decision editor:
  - Accept AI (mode `ACCEPT_AI`)
  - Override (mode `OVERRIDE`)

Action button:
- “Подтвердить финал” (creates final decision; in MVP this also publishes to the student)

After confirmation:
- final result is visible to the student immediately.
