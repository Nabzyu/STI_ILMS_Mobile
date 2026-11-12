# Student portal P0 — Design

**Date:** 2026-10-14  
**Status:** Approved (Approach 1; Printing B; Clearance A; Profile C)  
**Audience:** Student / Faculty only  

## Goal

Close the highest-impact user-side gaps vs web: real printing jobs and official clearance status. Document Profile as blocked on backend deploy; no Profile feature work until mounts exist on production `main`.

## Decisions

| Topic | Choice |
|-------|--------|
| Overall approach | Build Printing + Clearance now; Profile checklist only |
| Printing depth | MVP: pick file, quote, submit, cancel, history — no receipts |
| Clearance depth | Status from `GET /clearance/me` only — no lost-book report |
| Profile | Deploy checklist; verify later with `api-route-deploy-gap` |
| Staff | Out of scope |

## Architecture

Same layers as the rest of the app:

1. **DTOs** in `network/dto` with snake_case + camelCase alternates where the JWT API uses camelCase.
2. **LibraryApi** Retrofit methods for printing + clearance.
3. **LibraryRepository** owns list/state, submit/cancel/load, maps errors with `userFacingMessage`.
4. **Screens** `PrintingServiceScreen` / `PrintingHistoryScreen` / `ClearanceScreen` use Portal/Elms components.

### Printing flow

```
Pick document → (optional) GET service-status
  → POST quote (multipart) → show page_count + calculated_cost
  → POST requests (multipart with quote fields)
  → refresh GET requests
Cancel: PUT .../requests/{id}/cancel when Pending + Unpaid
```

### Clearance flow

```
Open Clearance → GET /clearance/me → render status, reason, summary, lists
```

## UI notes

- Replace fake local cost estimate with server quote.
- Print type/paper use API enums (`Monochrome`/`Colored`, `Short`/`A4`/`Long`).
- Clearance removes loan/fine heuristic; show API blockers.
- Service closed: disable submit, show `unavailable_reason` when present.

## Profile deploy checklist (no code this pass)

1. Confirm feature branch mounts: `codex/library-revision-updates` has `app.use('/api/v1/profile/avatar'…)` and `app.use('/api/v1/profile'…)`.
2. Merge to `main` and wait for Vercel production deploy.
3. Probe with `api-route-deploy-gap` skill (authenticated Student token → not Route not found).
4. Then: smoke My Profile load/edit/avatar on mobile; fix only regressions.

## Error handling

All user-visible failures via `userFacingMessage`. Preserve existing banner/toast patterns.

## Testing

- Unit: print/clearance DTO parsing; optional mapper helpers.
- Manual/emulator: quote → submit → appears in history → cancel; clearance Cleared vs Not Cleared.
- Visual verification after Compose changes.

## Non-goals

Staff desk, receipts PDF, lost-book report, floor plan, notification actions, Profile UI rewrite pre-deploy.
