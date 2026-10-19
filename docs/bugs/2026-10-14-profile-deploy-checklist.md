# Profile deploy checklist (mobile unblocked after this)

**Status:** Waiting on backend — mobile Profile UI already calls these routes.  
**Related:** `docs/bugs/2026-10-13-profile-reservation-route-not-found.md`, skill `api-route-deploy-gap`

## Why mobile shows “This feature is not available right now”

Production `main` does not mount `/api/v1/profile` or `/api/v1/profile/avatar`. After JWT succeeds, Express returns **Route not found**, which the app maps to the friendly banner.

## Deploy steps (backend repo `JohnBazty/StiOrmocLibrary`)

1. Ensure branch with mounts (e.g. `codex/library-revision-updates`) includes:
   - `app.use('/api/v1/profile/avatar', profileAvatarRouter)`
   - `app.use('/api/v1/profile', selfProfileRouter)`
2. Merge to `main` and confirm Vercel production deploy for `sti-ormoc-smart-library.vercel.app`.
3. Probe (PowerShell — use `curl.exe`):
   - Unauthenticated `GET /api/v1/profile/me` → `401 JWT_REQUIRED` (expected).
   - Authenticated Student/Faculty → **not** `Route not found` (200 or business error).
4. Mobile smoke: open My Profile → load fields; save; avatar upload pending state.
5. Only then treat Profile as done on mobile; fix camelCase/snake_case DTO gaps if any.

## Do not

- Rewrite Profile Compose to “fix” Route not found while mounts are missing.
- Mark Profile complete based on feature-branch code alone.
