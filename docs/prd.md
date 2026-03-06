# prd.md

## Problem

Teachers spend significant time grading text assignments. We want to reduce teacher workload while keeping grading transparent and under teacher control.

## MVP solution

The MVP provides **AI-assisted grading** for **TEXT** submissions using a rubric, with a strict **teacher-in-the-loop** workflow:

1. A student submits a text assignment.
2. The system runs an internal evaluation job to produce an **AI draft**.
3. A teacher reviews the draft and creates the final **TeacherDecision**.
4. The student sees only a generic “under review” status until the final teacher-approved result is ready.

**Important:** Students must not be told that AI is involved in the review process in the UI or student-facing API.

## Personas

- **Student**: submits text and waits for results.
- **Teacher**: creates rubrics, reviews submissions, finalizes results.

## Key invariants

- TEXT submissions only.
- AI output is a draft recommendation; the teacher is the final decision-maker.
- Students never see raw AI output (`AIResult`) or internal workflow details.
- Student-visible progress is generic: `UNDER_REVIEW` → `FINAL_READY`.
- The only student-visible final artifact is `TeacherDecision`.
- `TeacherDecision` has no draft state in MVP: creating it finalizes and makes it visible to the student.
- `decidedAt` is the timestamp of final teacher approval (and publication).

## Primary success criteria (MVP)

- A teacher can define a rubric and evaluate student submissions against it.
- The system produces an AI draft and the teacher can accept or override it.
- Students receive a final result only after teacher approval.
- The system is auditable: request tracing is preserved and errors are structured.
