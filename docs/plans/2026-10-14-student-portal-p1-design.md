# Student portal P1 — Design

**Date:** 2026-10-14  
**Status:** Approved (Approach 1; Profile = deploy/smoke only; Floor plan = light shelf text)  
**Audience:** Student / Faculty only  

## Goal

Close remaining **P0 + P1** user-side gaps vs web: unblock Profile after backend deploy, then ship notification actions, cancel pending borrow, print receipts, report lost book, and shelf location text on details.

## Decisions

| Topic | Choice |
|-------|--------|
| Overall approach | Single ordered plan (Approach 1) |
| Profile | Deploy checklist + post-deploy smoke/fixes only |
| Floor plan | Light — show catalog shelf/location text; no interactive map |
| Notifications | Mark read / mark all / delete one / clear all; skip schedule UI |
| Print receipts | List + detail + open/share PDF |
| Lost book | Student report only (no admin decide/settle) |
| P2 items | Deferred (fine receipts, dashboard API, attendance history) |
| Staff | Out of scope |

## Build order

0. Profile deploy gate + smoke (blocked until production mounts `/api/v1/profile`)  
1. Notification actions  
2. Cancel pending borrow request  
3. Print receipts (+ PDF)  
4. Report lost book  
5. Shelf location on book/research detail  

## Architecture

Same layers as P0:

```
LibraryApi → LibraryRepository (+ userFacingMessage) → Compose (Portal*/Elms*)
```

### 0. Profile (gate)

- No feature rewrite.
- Follow `docs/bugs/2026-10-14-profile-deploy-checklist.md` + skill `api-route-deploy-gap`.
- After deploy: login → My Profile load/edit → avatar upload; fix only camelCase/DTO bugs if any.

### 1. Notifications

| Action | API |
|--------|-----|
| Mark one read | `PATCH /api/v1/notifications/{id}/read` |
| Mark all read | `PATCH /api/v1/notifications/read-all` |
| Delete one | `DELETE /api/v1/notifications/{id}` |
| Clear all | `DELETE /api/v1/notifications` |

UI: actions on `NotificationsScreen` (row tap / overflow / toolbar). Refresh list and unread badge after each action.

### 2. Cancel pending borrow

| Action | API |
|--------|-----|
| Cancel request | `PUT /api/v1/circulation/requests/{transactionId}/cancel` body optional `{ reason }` |

UI: on `BorrowingHistoryScreen` (and/or borrow process) show Cancel when `BorrowStatus.PENDING`. Confirm dialog; optional short reason; reload history.

### 3. Print receipts

| Action | API |
|--------|-----|
| List | `GET /api/v1/printing/receipts` |
| Detail | `GET /api/v1/printing/receipts/{id}` |
| PDF | `GET /api/v1/printing/receipts/{id}/pdf` |

UI: section on printing history or dedicated receipts list; View opens in-app detail; Download/Share uses Android share/view Intent for PDF bytes. Out of scope: admin receipts.

### 4. Report lost book

| Action | API |
|--------|-----|
| Report | `POST /api/v1/clearance/lost-books/{transactionId}/report` |

UI: from Clearance (active loan row) and/or Borrowing history for active loans. Confirm dialog → POST → `loadClearance()` (+ history refresh). Show success/error via banner.

### 5. Shelf location (light)

- Data already mapped: `Book.shelfLocation`, `Research.shelfLocation` from catalog DTOs.
- **Already shown** on `BookDetailScreen` (`InfoLine("Shelf", …)`) and `ResearchDetailScreen`.
- This task is verify/polish only: ensure blank/`—` empty states are clear; add **call number** on book detail when catalog provides it; no `/floor-plan` map.

## Error handling

All failures through `userFacingMessage`. Prefer `ElmsBanner` / confirm dialogs over raw toasts for destructive actions.

## Testing

- Unit tests for new DTOs (notification action envelopes if needed, receipt DTO, cancel response).
- Emulator: each flow once signed in as Student.
- Visual verification after Compose changes.
- Profile: only “done” after live probe proves mounts exist.

## Non-goals

Interactive floor map, fine receipts/terms, `/dashboard` aggregate, attendance visit history, staff/admin, notification schedule UI.
