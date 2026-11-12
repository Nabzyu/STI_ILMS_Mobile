# Bug Report: Profile (and related) APIs return “Route not found”

**Status:** Root cause confirmed — profile routes are not mounted on the hosted API (`main`). Mobile reserve path aligned to the live user reservation API.

## Description

Signed-in mobile calls to My Profile (`GET/PATCH /api/v1/profile/me`, avatar under `/api/v1/profile/avatar/me`) surface the API catch-all message **Route not found.** Reservation list/cancel paths exist on production; create is available as `POST /api/v1/reservations/request` (catalog create also exists). Profile endpoints only exist on an undeployed backend branch.

## Environment

- **App**: STI ILMS Mobile (`BuildConfig.API_BASE_URL` → `https://sti-ormoc-smart-library.vercel.app/`)
- **Backend repo**: `JohnBazty/StiOrmocLibrary`
- **Deployed branch**: GitHub `main` @ `befe9e5` (default branch)
- **Profile implementation branch** (local/feature): mounts exist, e.g. `codex/library-revision-updates`

## Severity

**High** — My Profile cannot load or save against production. Reservation create/list should work for Student/Faculty once the app hits the user reservation routes.

## Evidence

| Check | Result |
|-------|--------|
| Unauthenticated `GET /api/v1/profile/me` | `401 JWT_REQUIRED` (global `/api/v1` JWT gate runs before 404) |
| `origin/main` `apps/api/src/app.js` | **No** `selfProfileRouter` / `profileAvatarRouter` mounts |
| Feature branch `app.js` | `app.use('/api/v1/profile/avatar', …)` and `app.use('/api/v1/profile', …)` |
| `origin/main` reservations | `GET /api/v1/reservations`, `POST /api/v1/reservations/request`, `PUT /…/cancel` |
| `origin/main` catalog reserve | `POST /api/v1/catalog/books/:titleId/reservations` (Student/Faculty only) |
| Catch-all | `{ success: false, message: "Route not found." }` after JWT succeeds and no router matches |

## Expected Behavior

- Profile and avatar routes are registered on the deployed API.
- Authenticated Student/Faculty can load/edit profile and manage reservations.

## Actual Behavior

- After login, profile calls authenticate successfully, then hit the Express 404 catch-all → **Route not found.**
- Those routes are implemented in the backend monorepo but **not merged/deployed to `main`**.

## Possible Fix

1. **Backend (required for profile):** Merge and deploy the branch that mounts `/api/v1/profile` and `/api/v1/profile/avatar` to Vercel production (`main`).
2. **Mobile (done in this app):** Call `POST /api/v1/reservations/request` with `{ bookTitleId }` for create; map avatar `currentUrl` / pending / rejection; reload profile after PATCH (API returns `{ id, changed_fields }` only).

## Additional Notes

- Admin/Librarian tokens get `403 JWT_ROLE_FORBIDDEN` on user reservation routes (Student/Faculty only), not “Route not found.”
- Catalog reserve also rejects non–Student/Faculty with `BOOK_RESERVATION_ROLE_FORBIDDEN`.
