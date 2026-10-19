---
name: student-portal-p1
description: Implement STI ILMS Student/Faculty P1 portal gaps — Profile post-deploy smoke, notification mark/delete, cancel pending borrow, print receipts/PDF, report lost book, shelf location text. Use when planning or building remaining user-side gaps after P0 printing/clearance.
---

# Student portal P1

Staff/admin **out of scope**. Follow design + plan:

- `docs/plans/2026-10-14-student-portal-p1-design.md`
- `docs/plans/2026-10-14-student-portal-p1.md`

## Stack

Reuse `.claude/skills/student-portal-p0/skill-stack.md` plus:

| Skill | Why |
|-------|-----|
| `api-route-deploy-gap` | Profile mounts on production |
| `student-portal-p0` | Printing/clearance patterns already shipped |

## Order (do not scramble)

0. Profile deploy/smoke  
1. Notifications actions  
2. Cancel borrow  
3. Print receipts  
4. Report lost book  
5. Shelf location text  

## Hard rules

- Mirror web JWT routes; no invented paths.
- `userFacingMessage` on all failures.
- Floor plan = **text only** this pass (no `/floor-plan` map UI).
- Confirm dialogs for delete-all, cancel borrow, report lost.
