# Product anchors — student portal P0

## Audience

- **In:** Student and Faculty signed into the mobile app
- **Out:** Admin / Librarian desk workflows (placeholder StaffHome stays)

## User outcomes

1. **Printing** — Submit a real library print job from the phone and cancel it while still Pending + Unpaid; see live history and cost from the server.
2. **Clearance** — See official Cleared / Not Cleared standing (same source of truth as web), not a home-grown guess from cached loans/fines.
3. **Profile** — Unblocked only after backend deploy; until then users may still see feature-unavailable (honest), checklist documents the merge.

## Copy / UX anchors

- Cash payment reminder: pay at the library counter (match web).
- Print types/labels: `Monochrome` / `Colored`; paper: `Short` / `A4` / `Long` (API enums — do not invent “Black & white”).
- Clearance banner: Cleared vs Not Cleared; show `reason` from API when blocked.
- All failures through `userFacingMessage`.

## Backend source of truth

- Repo: `JohnBazty/StiOrmocLibrary`
- Deployed: `main` → `https://sti-ormoc-smart-library.vercel.app/`
- Web reference: `apps/web/src/features/printing/StudentPrintingPage.tsx`, `apps/web/src/features/clearance/StudentClearancePage.tsx`
- Profile mounts (not on main yet): branch `codex/library-revision-updates` — `app.use('/api/v1/profile'…)`

## Non-goals this pass

- Print receipts UI / PDF download
- Lost-book reporting
- Floor plan
- Notification mark-read
- Staff circulation / attendance scan
