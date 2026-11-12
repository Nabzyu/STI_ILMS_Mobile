# Global Search Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Wire the drawer search icon to a dedicated Search screen that shows book and research predictions as the user types (local first, then debounced API).

**Architecture:** New stacked `DashboardRoute.Search` composable. Filter `LibraryRepository.books` / `research` on each keystroke; after 300ms idle, call existing `loadBooks` / `loadResearch` and refresh suggestions. Tap navigates to existing book/research detail routes.

**Tech Stack:** Jetpack Compose, Navigation Compose, existing `LibraryRepository` / Portal UI components. No new dependencies.

**Design:** [`2026-10-13-global-search-design.md`](./2026-10-13-global-search-design.md)

---

### Task 1: Add Search route

**Files:**
- Modify: `app/src/main/java/dashboard/DashboardRoute.kt`
- Modify: `app/src/main/java/dashboard/DashboardScreen.kt` (stacksOverTabs set + drawer Search onClick + composable)

**Step 1: Add route object**

In `DashboardRoute.kt`, add:

```kotlin
object Search : DashboardRoute("search")
```

**Step 2: Wire drawer + NavHost**

- Include `DashboardRoute.Search.route` in `stacksOverTabs`.
- Change drawer Search `HeaderIconButton` from no-op to:

```kotlin
onClick = {
    dashboardNavController.openStackedRoute(DashboardRoute.Search.route)
    scope.launch { drawerState.close() }
}
```

- Add composable (screen stub OK until Task 2):

```kotlin
composable(DashboardRoute.Search.route) {
    GlobalSearchScreen(
        onBack = { dashboardNavController.popBackStack() },
        onBookClick = { id ->
            dashboardNavController.navigate(DashboardRoute.BookDetail.create(id))
        },
        onResearchClick = { id ->
            dashboardNavController.navigate(DashboardRoute.ResearchDetail.create(id))
        },
    )
}
```

**Step 3: Commit** (when user asks to commit)

```bash
git add app/src/main/java/dashboard/DashboardRoute.kt app/src/main/java/dashboard/DashboardScreen.kt
git commit -m "feat(search): add stacked search route and drawer entry"
```

---

### Task 2: Prediction model + local filter helper

**Files:**
- Create: `app/src/main/java/dashboard/search/SearchModels.kt` (or top of `GlobalSearchScreen.kt` if keeping one file — prefer small models file or private types in screen)
- Optional test: `app/src/test/java/com/example/stismartlib/SearchPredictionsTest.kt`

**Step 1: Define suggestion type**

```kotlin
enum class SearchResultKind { Book, Research }

data class SearchPrediction(
    val id: String,
    val kind: SearchResultKind,
    val title: String,
    val subtitle: String,
)
```

**Step 2: Pure filter function**

```kotlin
fun buildSearchPredictions(
    query: String,
    books: List<Book>,
    research: List<Research>,
    limit: Int = 8,
): List<SearchPrediction> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val bookHits = books.asSequence()
        .filter { "${it.title} ${it.author} ${it.isbn}".contains(q, ignoreCase = true) }
        .map {
            SearchPrediction(it.id, SearchResultKind.Book, it.title, it.author.ifBlank { "Book" })
        }
    val researchHits = research.asSequence()
        .filter { "${it.title} ${it.author} ${it.category}".contains(q, ignoreCase = true) }
        .map {
            SearchPrediction(it.id, SearchResultKind.Research, it.title, it.author.ifBlank { "Research" })
        }
    return (bookHits + researchHits).take(limit).toList()
}
```

**Step 3: Unit test** (optional but preferred)

- Blank query → empty
- Title match returns book before research when both match
- Limit respected

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*SearchPredictions*"`

---

### Task 3: GlobalSearchScreen UI + debounce

**Files:**
- Create: `app/src/main/java/dashboard/search/GlobalSearchScreen.kt`
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt` (add preview)
- Modify: imports in `DashboardScreen.kt`

**Step 1: Screen structure**

- `rememberLibraryRepository()`, `var query by rememberSaveable { mutableStateOf("") }`
- `var apiError by remember { mutableStateOf<String?>(null) }`
- `var refreshing by remember { mutableStateOf(false) }`
- `val predictions = remember(query, repository.books.toList(), repository.research.toList()) { buildSearchPredictions(...) }`
- `LaunchedEffect(query)`: if blank clear error; else `delay(300)` then `refreshing=true`, `runCatching { loadBooks(q); loadResearch(q) }`, set `apiError` on failure without clearing lists, `refreshing=false`
- Request focus on field with `FocusRequester` + `LaunchedEffect(Unit)`

**Step 2: Layout**

`LazyColumn` / `Column` + list:

1. Title “Search” + subtitle
2. `PortalField` search (leading `Lucide.Search`)
3. Optional `ElmsBanner` for `apiError`
4. If query blank → hint text
5. Else if predictions empty && !refreshing → `ElmsEmptyState` (“No matches”, action “Browse catalog” → callback to book catalog optional; or omit action and only Back)
6. Else prediction rows (`ElmsListRow` or custom Row ≥48dp) with Book/Research chip
7. Ghost `PortalButton` Back

**Step 3: Wire clicks**

- Book → `onBookClick(id)`
- Research → `onResearchClick(id)`

**Step 4: Preview**

```kotlin
@Preview(name = "Global search")
private fun GlobalSearchScreenPreview() {
    PreviewApp {
        NestedPreview {
            GlobalSearchScreen(onBack = NoOp, onBookClick = {}, onResearchClick = {})
        }
    }
}
```

**Step 5: Build**

Run: `.\gradlew.bat :app:compileDebugKotlin`  
Expected: BUILD SUCCESSFUL

---

### Task 4: Device verification

**Files:** none (manual / adb)

**Step 1: Install**

`.\gradlew.bat :app:installDebug`

**Step 2: Drive**

- Open `DashboardScreenPreview` → More/drawer → Search
- Type part of a seeded book title → predictions appear
- Wait briefly → list may refresh from API (or keep local if offline)
- Tap a book prediction → book detail
- Back → Search still has query
- Back again → leave Search
- Repeat for research title if sample data present

**Step 3: Screenshot**

`build/uishots/global-search.png`

---

### Task 5: Polish

- Ensure touch targets ≥48dp on rows
- Dark mode: no hardcoded light-only colors
- Do not add Search to bottom nav
- Confirm drawer Search closes drawer before navigate

---

## Done when

- [ ] Drawer Search opens Search screen
- [ ] Typing shows local book/research predictions
- [ ] Debounced API refresh updates suggestions
- [ ] Tap opens correct detail; Back preserves query on Search
- [ ] Light + dark look consistent with portal UI
