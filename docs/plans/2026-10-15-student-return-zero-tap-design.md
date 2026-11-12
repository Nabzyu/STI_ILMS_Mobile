# Student return — zero-tap (design)

**Date:** 2026-10-15  
**Status:** Approved (Approach 1 — mobile cleanup only)  
**Audience:** Student / Faculty  

## Goal

Stop asking students to press anything to return books. Returning is a **library counter event** completed by admin web (`PUT /api/v1/admin/borrowing/:transactionId/return`). The mobile app should only show loan status and clear counter guidance.

## Context

- Admin web already completes returns (loan row → Return). Checkout is barcode-driven; return is staff click today.
- Mobile `ReturnBookScreen` “Initiate return” is **local-only** — no API, false “submitted” success.
- Desired feel: bring the book; account updates after staff return.

## Decisions

| Topic | Choice |
|-------|--------|
| Approach | Remove fake student return actions (Approach 1) |
| Keep loan visibility | Yes — Overview active loans + Activity (Borrowing history) |
| Counter guidance | Short static copy where loans are shown; no fake submit |
| Student return API | None — do not invent |
| Admin scan-to-return | Out of scope this pass (Approach 2 later, web repo) |
| Staff Android | Out of scope |

## UX after change

```
Student sees active loan (Overview / Activity)
  → Bring book to counter
  → Staff completes return on admin
  → Student refreshes / reopens → status Returned (+ fines if overdue)
```

No “Initiate return”, no confirm-as-submit dialog, no dedicated Return route required.

## Non-goals

- Admin barcode scan-to-return
- Push notification on return (nice later)
- Changing clearance / lost-book report flows
- Backend changes in `StiOrmocLibrary`

## Success criteria

- No navigation path to a fake return submit flow
- Active loans still discoverable
- Copy makes counter return clear without implying an app action
- Compile + visual check on overview / activity
