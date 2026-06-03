# Panini WC26 Tracker — Design

Single source of truth for product, UX, and architecture decisions.
Update this file when a decision changes — don't let chat be the authority.

---

## 1. Goal

A personal Android app to track progress on the Panini FIFA World Cup 2026
sticker album: own/missing/duplicates per sticker, browse by group/team,
search by player name, scan a sticker with the camera, and read clear progress
reports.

Single user, on-device, offline-first. No backend.

---

## 2. Scope (v1)

In:

- Local catalog of 994 stickers (48 teams × 20 + 34 specials)
- Per-sticker quantity tracking (0 = missing, 1 = owned, ≥2 = duplicates)
- Browse: Groups → Teams → Team sheet (4×5 grid)
- Search across player name, team name, sticker ID
- Camera scan with on-device OCR → fuzzy match → user confirms quantity
- Reports: Progress, Missing, Duplicated
- Material 3, light + dark, dynamic color where available

Out:

- Multi-user / multi-album
- Cloud sync, accounts
- Trading marketplace / friend matching
- Manually photographing each sticker (placeholders only)
- Tests (v1 ships without automated tests)
- Tablet-optimized layouts (phone only)

---

## 3. Data model

### 3.1 Catalog (read-only, bundled in `assets/stickers.json`)

Generated once from the source Google Sheet and shipped with the app.
Replaceable across versions via `catalog.version`.

```kotlin
data class StickerCatalog(
    val version: String,            // e.g. "2026.1"
    val source: String,
    val generatedAt: Instant,
    val meta: CatalogMeta,
    val teams: List<TeamDef>,       // 48 entries
    val stickers: List<StickerDef>, // 994 entries
)

data class TeamDef(
    val code: String,               // "MEX"
    val name: String,               // "Mexico"
    val group: String,              // "A" .. "L"
    val seed: Int,                  // 1..4 (FIFA seed within group)
)

data class StickerDef(
    val id: String,                 // "MEX02" | "FWC1" | "00"
    val teamCode: String?,          // null for specials
    val group: String?,             // null for specials
    val type: StickerType,          // BADGE | NORMAL | TEAM | SPECIAL
    val displayName: String,        // "Luis Malagón"
    val section: String?,           // null for team stickers; "Section 1" etc.
    val slotIndex: Int?,            // 1..20 for team stickers; null for specials
)

enum class StickerType { BADGE, NORMAL, TEAM, SPECIAL }
```

### 3.2 Ownership (mutable, Room)

```kotlin
@Entity(tableName = "ownership")
data class OwnershipEntity(
    @PrimaryKey val stickerId: String,
    val quantity: Int,
    val updatedAt: Long,            // epoch millis
)
```

Splitting catalog from ownership means:

- Reset progress = `DELETE FROM ownership`
- Catalog updates don't disturb user data
- Ownership rows only exist when `quantity > 0` — implicit "missing" for all
  stickers not present in the table

### 3.3 Derived (never stored)

```kotlin
fun isOwned(q: Int) = q >= 1
fun duplicatesOf(q: Int) = (q - 1).coerceAtLeast(0)
fun isDuplicated(q: Int) = q >= 2
```

---

## 4. Information architecture

```
Home (hub)
├── Browse
│   ├── Groups (A..L + Specials)
│   ├── Teams (within a group, seed-ordered)
│   └── Team Sheet (4×5 grid)
│       └── Sticker Detail (bottom sheet)
├── Search
│   └── Sticker Detail (bottom sheet)
├── Scan
│   ├── Camera preview + capture
│   ├── OCR match preview
│   └── Sticker Detail (bottom sheet)
├── Stats
│   ├── Progress
│   ├── Missing
│   └── Duplicated
└── Settings
```

**Sticker Detail is a bottom sheet, not a route.** Tapping/scanning/searching
all open the same sheet over whatever screen the user is on — back closes the
sheet, not the underlying screen.

---

## 5. Screens

### 5.1 Home

```
┌─────────────────────────────────────────┐
│ PANINI WC26 TRACKER             ⚙       │
│ ████████████░░░░░░  714 / 994  71.8%    │
│ ┌───────────┐ ┌───────────┐             │
│ │  Browse   │ │  Search   │             │
│ │    📂     │ │    🔍     │             │
│ └───────────┘ └───────────┘             │
│ ┌───────────┐ ┌───────────┐             │
│ │   Scan    │ │   Stats   │             │
│ │    📷     │ │    📊     │             │
│ └───────────┘ └───────────┘             │
│ Last added: César Montes ×2             │
└─────────────────────────────────────────┘
```

### 5.2 Browse → Groups

12 group rows (A..L) + "Specials" row. Each row shows progress
(`8 / 80`) and a thin progress bar.

### 5.3 Browse → Teams (within a group)

4 team rows in FIFA seed order (1..4). Each row: flag/badge placeholder,
team name, `n / 20` progress, dupe badge if any.

### 5.4 Team Sheet (the satisfying one)

```
┌─────────────────────────────────────────┐
│  ← Mexico (Group A)        14 / 20      │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐                │
│  │01✓│ │02✓│ │03✓│ │04·│                │
│  │BDG│ │Mal│ │Vás│ │Sán│                │
│  └───┘ └───┘ └───┘ └───┘                │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐                │
│  │05×2│ │06✓│ │07·│ │08✓│               │
│  └───┘ └───┘ └───┘ └───┘                │
│   ...                                   │
└─────────────────────────────────────────┘
```

- Empty slots: dashed outline (album-faithful)
- Owned: filled tile + inner shadow ("stuck-down")
- Duplicated: small `×N` corner badge in gold
- Sort options: by slot (default), alphabetical
- Tap → bottom sheet; long-press → quick +1; swipe right +1; swipe left −1

### 5.5 Sticker Detail (bottom sheet)

```
═══
MEX05 · Mexico · Group A
César Montes
Quantity:  [−]  2  [+]
You have 1 duplicate (1 to trade)
[Save]
```

Stepper for fast taps; tap the number to type a value directly.

### 5.6 Search

Single text field, live results grouped by team. Accent-insensitive.
Searches across `displayName`, team name (returns all team stickers), and
sticker ID.

### 5.7 Scan

```
[Camera preview] → capture
  ↓
"Reading..."
  ↓
┌──────────────────┐
│ [thumb]          │
│ Best match:      │
│ MEX02 · Luis     │
│ Malagón (Mexico) │
│ Quantity: [−1+]  │
│ [Not this one?]  │
│ [Save]           │
└──────────────────┘
```

Optional "rapid mode" toggle: auto-save +1 on high-confidence matches without
the confirm step — for ripping through a fresh pack.

Failure path: low/no confidence → show top 3 candidates + a "search manually"
fallback.

### 5.8 Stats

#### Progress

- Overall: `714 / 994 (71.8%)` + total duplicates count
- Per group: 12 horizontal bars (A..L)
- Per team: scrollable list of 48 with bar + dupe badge

#### Missing

- Flat list where `quantity == 0`
- Filters: by group, by team, by type
- Section headers per team
- "Copy as text" → multiline `MEX02, MEX07, ...`

#### Duplicated

- Flat list where `quantity ≥ 2`; shows `quantity − 1` as duplicate count
- Default sort: most duplicated first; alt: by team
- "Copy as text" → `MEX02 ×2 (1 dupe), ...`
- Footer: `Total duplicates: N across M stickers`

### 5.9 Settings

About / version / reset progress / open catalog source (link).

---

## 6. UX patterns

- **Sort within a group**: FIFA seed (1..4), fixed
- **Sort within a team**: slot index (default), alphabetical (toggle)
- **Sticker tile states**: empty (dashed), owned (filled), duplicated (badge `×N`)
- **Gestures on a tile**: tap = detail sheet, long-press = quick +1, swipe-right = +1, swipe-left = −1
- **Haptics**: single tick on +1, double tick on −1, celebration on team 100%
- **Empty states**: friendly + actionable (e.g., "No duplicates yet — go scan some stickers!")
- **Errors**: inline, never block; OCR failure → manual fallback

---

## 7. Visual style

Album-faithful × sporty/branded. Material 3 with dynamic color (Android 12+)
and a fallback palette tied to the 2026 host-nation flags.

### Palette (fallback, when dynamic color unavailable)

| Role             | Light       | Dark        |
| ---------------- | ----------- | ----------- |
| Primary          | `#D32F2F`   | `#FF6B6B`   |
| Secondary        | `#1565C0`   | `#64B5F6`   |
| Tertiary         | `#2E7D32`   | `#81C784`   |
| Surface          | `#FAF5E9`   | `#0F1419`   |
| Surface variant  | `#EFE6CC`   | `#1A2330`   |
| Owned accent     | `#C9A227`   | `#E8C547`   |

### Typography

- Display / headers: condensed sport face (e.g., Bebas Neue or Anton, free, Latin Extended)
- Body: Inter
- Slot numbers: tabular monospace so 4×5 grid lines up

### Motion

- Material 3 spring transitions
- Subtle celebration (confetti / sticker pop) on team 100% — once per team
- Foil/shiny specials (Section 1) get a slow gradient sheen

---

## 8. Architecture

### 8.1 Module structure

Single Gradle module with strict package boundaries.

```
com.vibely.wc26/
├── core/
│   ├── di/                # Hilt modules
│   ├── ui/
│   │   ├── theme/         # M3 theme, colors, typography
│   │   └── components/    # StickerTile, ProgressBar, BadgeChip, …
│   └── util/              # Text normalization, fuzzy matching
├── data/
│   ├── catalog/           # StickerCatalogDataSource (assets/stickers.json)
│   ├── ownership/         # Room: OwnershipDao + OwnershipDatabase
│   └── prefs/             # DataStore
├── domain/
│   ├── model/             # Pure-Kotlin models
│   └── usecase/           # Only where they add value
├── feature/
│   ├── home/
│   ├── browse/            # Groups → Teams → TeamSheet
│   ├── search/
│   ├── scan/              # CameraX + ML Kit + match
│   ├── stickerdetail/     # Shared bottom sheet
│   ├── stats/             # Progress + Missing + Duplicated
│   └── settings/
└── PaniniApp.kt           # @HiltAndroidApp + MainActivity
```

### 8.2 Layer rules

- `feature/*` may depend on `domain` only (never `data`)
- `domain` is pure Kotlin (no Android, Compose, Room)
- `data` implements interfaces declared in `domain`
- `core/ui` is presentation-only (no data, no domain)

### 8.3 Unidirectional flow

```
UI event → ViewModel.onEvent() → UseCase | Repository → DAO
                                                  ↓
UI ← collectAsStateWithLifecycle ← StateFlow<UiState>
```

Each ViewModel exposes a single `StateFlow<XxxUiState>` (sealed/data class)
and accepts `onEvent(XxxUiEvent)`. No LiveData. No two-way binding.

### 8.4 Use cases — only when they add value

Skip:

- `GetTeamSheetUseCase` (pass-through to repo) → call repo from ViewModel

Keep:

- `MatchScannedTextUseCase` (orchestrates OCR text + fuzzy + catalog)
- `GetOverallStatsUseCase` (aggregates catalog + ownership)
- `UpdateStickerQuantityUseCase` (writes ownership + invalidates derived flows)

### 8.5 Navigation 3

```kotlin
@Serializable sealed interface Route : NavKey
@Serializable data object Home : Route
@Serializable data object Browse : Route
@Serializable data class Group(val letter: String) : Route
@Serializable data class TeamSheet(val teamCode: String) : Route
@Serializable data object Search : Route
@Serializable data object Scan : Route
@Serializable data object Stats : Route
@Serializable data object Settings : Route
```

`NavDisplay` + `rememberNavBackStack(Home)` + `entryProvider { entry<Home> { … } }`.

The sticker detail is **bottom-sheet state inside each screen's ViewModel**,
not a route — keeps the back stack honest.

---

## 9. Persistence

- **Catalog**: `assets/stickers.json`, parsed once at app start, kept in memory
  (~185 KB, 994 entries — trivial). Re-parse only on version change.
- **Ownership**: Room. Single table. Sparse rows (only `quantity > 0`).
- **Prefs**: DataStore (sort order, theme, rapid scan toggle, etc.).
- **Backup**: Android Auto-Backup default behavior (Room + DataStore both
  included automatically). No manual export in v1.

---

## 10. Scan pipeline

```
Camera (CameraX)
  → ImageAnalysis frame
  → ML Kit TextRecognizer (on-device)
  → normalize(extractedText)         # uppercase, strip accents, collapse ws
  → MatchScannedTextUseCase
       ├── exact match on sticker ID (e.g., "MEX02")
       ├── fuzzy match (Jaro–Winkler) on displayName + teamName
       └── return top 3 candidates with score
  → UI: best match if score ≥ THRESHOLD, else top-3 picker
  → user confirms quantity → write to ownership
```

Thresholds tunable in `core/util/Fuzzy.kt`. Start with Jaro–Winkler ≥ 0.85
for auto-select.

---

## 11. Tech stack (locked)

| Component          | Choice                                |
| ------------------ | ------------------------------------- |
| Language           | Kotlin (latest stable)                |
| UI                 | Jetpack Compose + Material 3          |
| Navigation         | androidx.navigation3 (Nav3)           |
| DI                 | Hilt + lifecycle-viewmodel-navigation3 add-on |
| Database           | Room (KSP)                            |
| Prefs              | DataStore Preferences                 |
| Async              | Kotlin Coroutines + Flow              |
| Serialization      | kotlinx.serialization                 |
| Camera             | CameraX (latest stable)               |
| OCR                | ML Kit Text Recognition v2 (on-device) |
| Image loading      | Coil 3                                |
| Immutable lists    | kotlinx.collections.immutable         |
| Build              | AGP 9.2.0 / Gradle 9.4.1 / JDK 17     |
| SDK levels         | min 26 / target 36 / compile 36       |
| Version catalog    | `gradle/libs.versions.toml`           |

No deprecated APIs anywhere in the project. No LiveData. No SharedPreferences.
No AsyncTask. No Navigation Compose (use Nav3).

---

## 12. Open questions

- App icon design (placeholder vector for v1, custom asset later)
- Whether to ship a "demo mode" with pre-seeded ownership for screenshots
- Whether to add a celebratory animation library or hand-roll one with Compose
