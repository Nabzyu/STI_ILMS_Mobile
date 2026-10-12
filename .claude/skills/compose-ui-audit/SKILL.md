---
name: compose-ui-audit
description: Structured UI/UX review of the STI ILMS Compose screens that produces prioritised, evidence-backed findings and an implementation plan. Use when asked to improve, audit, review, polish, or find problems in the app's UI/UX, or before starting a batch of visual changes.
---

# Compose UI/UX Audit

Produces a prioritised findings report and a staged plan. This skill **reviews and plans — it does not implement.** Only start editing after the user picks what to act on.

Read `sti-design-system` first for the app's tokens and components. Use `android-visual-verification` to capture the screens you are auditing.

## Workflow

```
- [ ] 1. Agree the scope
- [ ] 2. Capture evidence
- [ ] 3. Review against the dimensions
- [ ] 4. Rate and prioritise
- [ ] 5. Write the report
```

### 1. Agree the scope

Auditing 23 screens at once produces a report nobody acts on. Pick a slice and say which one you picked:

- a user journey (auth → dashboard → catalog → borrow)
- a role (`UserType.STUDENT` / `FACULTY` / `ADMIN`)
- a theme concern across all screens (dark mode, empty states, touch targets)

Screens live under `app/src/main/java/{auth,dashboard,ui}/`. `ui/preview/ScreenPreviews.kt` lists every screen with its preview entry point — use it as the index.

### 2. Capture evidence

Every finding must point at either a file and line or a screenshot. Do not report impressions.

Capture each in-scope screen in light and dark mode per `android-visual-verification`, and read the corresponding source file. Findings with no evidence get dropped, not downgraded.

### 3. Review dimensions

**Visual hierarchy** — Is the primary action obvious within two seconds? Does type scale signal importance, or is everything the same weight? Is the eyebrow/title/body rhythm (`EyebrowStyle` → headline → body) applied consistently?

**Layout and rhythm** — Spacing on the 4dp grid and consistent with neighbouring screens. Consistent 16dp screen padding. No crowding, no orphaned whitespace. Content not colliding with the status bar or gesture bar.

**State coverage** — Every data-backed screen needs loading, empty, error, and success. Missing empty and error states are the most common gap in this codebase. Check that shared components (`ElmsLoadingBlock`, `ElmsEmptyState`, `ElmsBanner`) are used rather than ad-hoc text.

**Consistency** — Same concept rendered the same way everywhere: cards, list rows, badges, buttons. Watch for the duplicate `StatCard` / `SectionCard` definitions producing two looks for one idea. One icon set per screen.

**Dark mode** — Captured and compared, not assumed. Hunt for hardcoded light-only colours and the deprecated `Brand*` / `PrimaryBlue*` aliases.

**Accessibility** — Touch targets ≥48dp, meaningful `contentDescription` on interactive elements, `null` on decorative ones, text contrast ≥4.5:1, layout survives 1.3x font scale. See the `android` skill for the full ruleset.

**Feedback and affordance** — Destructive actions confirmed, async actions show progress, errors say what to do next (`ui/feedback/UserFacingErrors.kt`), disabled states look disabled.

**Copy** — Clear, consistent terminology, sentence case, no developer jargon leaking into labels. Note that nearly all copy is hardcoded rather than in string resources.

### 4. Severity

| Level | Meaning | Examples |
|---|---|---|
| 🔴 Critical | Blocks or misleads the user | Invisible text in dark mode, unreachable action, no error state on a failing call |
| 🟠 High | Noticeably degrades the experience | Touch target under 48dp, missing empty state, text clipped at 1.3x font scale |
| 🟡 Medium | Inconsistency or friction | Off-grid spacing, two card styles for one concept, weak hierarchy |
| 🟢 Low | Polish | Minor alignment, copy tightening, redundant divider |

Rank by severity first, then by effort within a level. Call out the cheap high-severity fixes explicitly — those are what the user should do first.

### 5. Report template

```markdown
## Scope
[Screens audited, roles and themes covered, how they were captured]

## Summary
[2–4 sentences: the overall state, and the single biggest problem]

## Findings

### 🔴 [Short title]
**Where:** `path/to/Screen.kt:123` — [screenshot name if relevant]
**Problem:** [what the user experiences, not what the code does]
**Fix:** [specific change, naming the token or component to use]
**Effort:** S / M / L

### 🟠 …

## Recommended order
1. [Finding] — [why first]
2. …

## Out of scope
[Things noticed but deliberately not covered]
```

## Rules

- Report what a user would notice. "Uses `Column` instead of `LazyColumn`" is only a finding if it causes visible jank — otherwise it belongs in a code review, not a UX audit.
- One finding per issue. Do not bundle five spacing problems into "spacing is inconsistent".
- Every fix names the existing token or component to use. "Use better colours" is not actionable; "replace `PrimaryBlue45` with `MaterialTheme.colorScheme.primary`" is.
- Do not invent new design direction mid-audit. If a screen needs a rethink rather than a fix, say so and stop — that is a separate conversation.
- Note the things you could not check (no device, screen behind a login, backend unavailable) rather than quietly skipping them.
