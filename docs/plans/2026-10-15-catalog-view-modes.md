# Catalog view modes Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add list vs 2-column grid toggles on Book and Research catalogs, with separate persisted preferences per catalog.

**Architecture:** Pure `CatalogViewMode` parse/serialize + thin `CatalogViewPrefs` over plain `SharedPreferences`. Shared `CatalogViewToggle` and grid tile composables. Each catalog screen keeps filters; only the results region switches `LazyColumn` ↔ `LazyVerticalGrid` (2 cols). Invalid/missing prefs → `LIST`.

**Tech Stack:** Jetpack Compose, Material 3, Lucide icons, JUnit JVM tests, STI design tokens (`ElmsSectionCard`, `BookCoverPlaceholder`, `Shape2Xl`, `ElmsCyan`).

**Design:** `docs/plans/2026-10-15-catalog-view-modes-design.md`  
**Skills:** `@sti-design-system`, `@unit-testing-test-generate`, `@android-visual-verification`

---

### Task 1: CatalogViewMode + failing parse tests

**Files:**
- Create: `app/src/main/java/dashboard/components/CatalogViewMode.kt`
- Create: `app/src/test/java/com/example/stismartlib/CatalogViewModeTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.stismartlib

import com.example.stismartlib.ui.dashboard.components.CatalogViewMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogViewModeTest {
    @Test
    fun fromStorage_list() {
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage("list"))
    }

    @Test
    fun fromStorage_grid2() {
        assertEquals(CatalogViewMode.GRID_2, CatalogViewMode.fromStorage("grid_2"))
    }

    @Test
    fun fromStorage_nullOrBlankOrGarbage_defaultsToList() {
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage(null))
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage(""))
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage("grid_3"))
    }

    @Test
    fun toStorage_roundTrip() {
        assertEquals("list", CatalogViewMode.LIST.toStorage())
        assertEquals("grid_2", CatalogViewMode.GRID_2.toStorage())
        assertEquals(
            CatalogViewMode.GRID_2,
            CatalogViewMode.fromStorage(CatalogViewMode.GRID_2.toStorage())
        )
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests com.example.stismartlib.CatalogViewModeTest`

Expected: FAIL (class/symbol not found)

**Step 3: Write minimal implementation**

```kotlin
package com.example.stismartlib.ui.dashboard.components

enum class CatalogViewMode {
    LIST,
    GRID_2;

    fun toStorage(): String = when (this) {
        LIST -> "list"
        GRID_2 -> "grid_2"
    }

    companion object {
        fun fromStorage(raw: String?): CatalogViewMode = when (raw) {
            "grid_2" -> GRID_2
            else -> LIST
        }
    }
}
```

Note: package follows existing dashboard components (`ui.dashboard.components`); file lives under `app/src/main/java/dashboard/components/`.

**Step 4: Run tests — expect PASS**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests com.example.stismartlib.CatalogViewModeTest`

**Step 5: Commit**

```bash
git add app/src/main/java/dashboard/components/CatalogViewMode.kt app/src/test/java/com/example/stismartlib/CatalogViewModeTest.kt
git commit -m "feat(catalog): add CatalogViewMode storage mapping"
```

---

### Task 2: CatalogViewPrefs

**Files:**
- Create: `app/src/main/java/dashboard/components/CatalogViewPrefs.kt`
- Modify: `app/src/test/java/com/example/stismartlib/CatalogViewModeTest.kt` (optional — prefer keep prefs untested on JVM; no Robolectric). Document that prefs is a thin wrapper only.

**Step 1: Implement prefs**

```kotlin
package com.example.stismartlib.ui.dashboard.components

import android.content.Context

class CatalogViewPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(catalogKey: String): CatalogViewMode =
        CatalogViewMode.fromStorage(prefs.getString(storageKey(catalogKey), null))

    fun set(catalogKey: String, mode: CatalogViewMode) {
        prefs.edit().putString(storageKey(catalogKey), mode.toStorage()).apply()
    }

    companion object {
        const val PREFS_NAME = "smartlib_catalog_view"
        const val KEY_BOOKS = "books"
        const val KEY_RESEARCH = "research"

        private fun storageKey(catalogKey: String) = "catalog_view_$catalogKey"
    }
}
```

**Step 2: Compile check**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: SUCCESS

**Step 3: Commit**

```bash
git add app/src/main/java/dashboard/components/CatalogViewPrefs.kt
git commit -m "feat(catalog): persist view mode per catalog key"
```

---

### Task 3: CatalogViewToggle + grid tiles

**Files:**
- Create: `app/src/main/java/dashboard/components/CatalogViewToggle.kt`
- Modify: `app/src/main/java/dashboard/components/DashboardComponents.kt` only if reusing cover helpers; prefer new file for tiles to keep diff small.

**Step 1: Toggle composable**

`CatalogViewToggle(mode, onModeChange)` — Row of two 48dp icon buttons:
- List: `Lucide.List` (or `LayoutList` if present in the Lucide set used by the project)
- Grid: `Lucide.LayoutGrid` (or `Grid2x2` / `Columns2` if `LayoutGrid` missing)

Selected: tint/`background` with `ElmsCyan` / `colorScheme.primary` soft fill. Unselected: `onSurfaceVariant`. Use `clickable` + semantics `contentDescription` ("List view" / "Grid view"). Follow `@sti-design-system` (no raw colours; min 48dp).

**Step 2: Grid tiles**

```kotlin
@Composable
fun BookCatalogGridTile(book: Book, onClick: () -> Unit)

@Composable
fun ResearchCatalogGridTile(item: Research, onClick: () -> Unit)
```

- Wrap in `ElmsSectionCard` or clipped `Shape2Xl` surface
- Book: `BookCoverPlaceholder` full width aspect ~3:4, title 2 lines `maxLines=2` + `StatusBadge`
- Research: `Lucide.FileText` in soft brand fill box, title 2 lines, subtitle `year · category`
- Entire tile `clickable(onClick)`

**Step 3: Compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

**Step 4: Commit**

```bash
git add app/src/main/java/dashboard/components/CatalogViewToggle.kt
git commit -m "feat(catalog): add view toggle and grid tiles"
```

---

### Task 4: Wire BookCatalogScreen

**Files:**
- Modify: `app/src/main/java/dashboard/catalog/BookCatalogScreen.kt`

**Step 1: State + prefs**

```kotlin
val context = LocalContext.current
val viewPrefs = remember { CatalogViewPrefs(context) }
var viewMode by remember {
    mutableStateOf(viewPrefs.get(CatalogViewPrefs.KEY_BOOKS))
}
fun setViewMode(mode: CatalogViewMode) {
    viewMode = mode
    viewPrefs.set(CatalogViewPrefs.KEY_BOOKS, mode)
}
```

**Step 2: Results header**

Beside `"${visible.size} resources"` add `CatalogViewToggle(viewMode, ::setViewMode)` (Cart button stays).

**Step 3: Results layout**

When results are ready (not loading/empty/error-only):

- If `viewMode == LIST`: keep current `LazyColumn` + `items { ElmsListRow… }` structure (or keep one outer `LazyColumn` for chrome and list items).
- If `viewMode == GRID_2`: use `LazyVerticalGrid(columns = GridCells.Fixed(2), …)` with:
  - Header/filter blocks as `item(span = { GridItemSpan(maxLineSpan) }) { … }`
  - Books as `items(visible, key = { it.id }) { BookCatalogGridTile(…) }`

Avoid nesting a vertical grid inside a vertical lazy list without a fixed height.

**Step 4: Compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

**Step 5: Commit**

```bash
git add app/src/main/java/dashboard/catalog/BookCatalogScreen.kt
git commit -m "feat(catalog): list and grid modes for books"
```

---

### Task 5: Wire ResearchCatalogScreen

**Files:**
- Modify: `app/src/main/java/dashboard/research/ResearchCatalogScreen.kt`

Same pattern as Task 4 with `CatalogViewPrefs.KEY_RESEARCH`, count row + toggle, `ResearchCatalogGridTile` in grid mode. Keep existing list item composable for list mode.

**Commit:**

```bash
git add app/src/main/java/dashboard/research/ResearchCatalogScreen.kt
git commit -m "feat(catalog): list and grid modes for research"
```

---

### Task 6: Verify

**Step 1: Unit tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests com.example.stismartlib.CatalogViewModeTest`

Expected: all PASS

**Step 2: Visual (optional but preferred)**

Follow `@android-visual-verification`: build, install, open Book Catalog → toggle grid → open Research → confirm list still default → set research to grid → force-stop → reopen → books and research remember separately. Check light and dark.

**Step 3: Final commit only if leftover polish**

---

## Done when

- [ ] Books: list + 2-col grid toggle works
- [ ] Research: list + 2-col grid toggle works
- [ ] Prefs separate and persist across restart
- [ ] Invalid pref → list
- [ ] `CatalogViewModeTest` green
- [ ] No nested-scroll crash; filters still work in both modes
