# docs/README.md

## Purpose

This directory is the **current source of truth** for MVP behavior, API contracts, and the data model.

- Repository/agent rules live in `../AGENTS.md`.
- Product and technical contracts live in this `docs/` directory.

If implementation and docs conflict on **product behavior** (visibility rules, scoring semantics, lifecycle semantics), reconcile explicitly—do not silently guess.

## Reading order

1. `prd.md` — product goal, personas, end-to-end flow, invariants
2. `scope.md` — in/out of MVP
3. `user-stories.md` — user flows
4. `acceptance-criteria.md` — testable requirements
5. `screens.md` — UI states and copy (student must not see AI details)
6. `status-model.md` — canonical mapping from internal workflow to visible states
7. `api-contract.md` — endpoints and request/response semantics
8. `data-model.md` — entities, relationships, and DB-level invariants
9. `non-functional.md` — privacy, auditability, reliability
10. `changelog.md` — changes to requirements

## Update rules

- Any requirement change must update `changelog.md` and the affected doc(s).
- Do not duplicate the same requirement text across multiple files.
- Keep `screens.md`, `status-model.md`, `api-contract.md`, and `data-model.md` consistent.

## Quick links

- Student-visible status must be generic: `UNDER_REVIEW` → `FINAL_READY`.
- `TeacherDecision` has no draft state in MVP: creating it finalizes and makes it visible to students.
- `decidedAt` is the timestamp of final teacher approval (and publication).
