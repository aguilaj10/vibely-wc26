# Panini WC26 Tracker — Implementation Plan

Ordered phases. Each phase is independently reviewable, ends at a working build,
and unblocks the next. No code is written until Phase 0 starts and PLAN.md is
approved.

See [DESIGN.md](./DESIGN.md) for the consolidated design reference.

---

## Project metadata

- **App display name**: Panini WC26 tracker
- **Package id**: `com.vibely.wc26`
- **Project root**: `~/personal/desarrollo/vibely-wc26`
- **Min SDK**: 26 · **Target / Compile SDK**: 36
- **Build**: AGP 9.2.0 · Gradle 9.4.1 · JDK 17
- **Catalog source**: `data/stickers.json` (moves to `app/src/main/assets/` in Phase 0)

---

## Phase 0 — Scaffold

**Goal**: An empty but well-structured Android Studio project that builds and
launches an empty "hello" Compose screen.

- [ ] `settings.gradle.kts` + root `build.gradle.kts` (Kotlin DSL, version catalog)
- [ ] `gradle/libs.versions.toml` (AGP 9.2.0, Kotlin, Compose BOM, M3, Nav3, Hilt, Room, DataStore, kotlinx.serialization, Coroutines, immutable collections, CameraX, ML Kit Text Recognition, Coil 3, KSP)
- [ ] `gradle/wrapper/gradle-wrapper.properties` → 9.4.1
- [ ] `:app` module: `build.gradle.kts` with Compose, Hilt, Room/KSP, Kotlin Serialization plugins
- [ ] `AndroidManifest.xml`: `@HiltAndroidApp` Application, single `MainActivity` (no exported intents beyond MAIN/LAUNCHER), camera permission declared (requested at runtime in Phase 7)
- [ ] Package skeleton (`core`, `data`, `domain`, `feature`) — empty packages
- [ ] `core/ui/theme/` with M3 theme using fallback palette + dynamic color support
- [ ] `PaniniApp.kt` + `MainActivity.kt` rendering a stub home screen
- [ ] Move `data/stickers.json` to `app/src/main/assets/stickers.json`
- [ ] `proguard-rules.pro` baseline (R8 9.2 keep rules for kotlinx.serialization)

**Exit criteria**: `./gradlew assembleDebug` succeeds; app installs and launches.

---

## Phase 1 — Data layer

**Goal**: Catalog parses from assets on first launch; ownership persists in Room.

- [ ] `data/catalog/CatalogModels.kt` — `@Serializable` DTOs matching `stickers.json`
- [ ] `data/catalog/CatalogDataSource.kt` — loads + decodes once, caches in-memory
- [ ] `data/catalog/CatalogRepository.kt` (domain interface in `domain/catalog/`)
- [ ] `data/ownership/OwnershipEntity.kt` + `OwnershipDao.kt`
- [ ] `data/ownership/OwnershipDatabase.kt` (Room, version 1, no migrations yet)
- [ ] `data/ownership/OwnershipRepository.kt`
- [ ] `data/prefs/PrefsDataStore.kt` (sort, theme, rapid scan)
- [ ] Hilt `DataModule` (DB, DataStore, repository bindings)

**Exit criteria**: Unit-runnable manual smoke (debug `LaunchedEffect` in main
activity logs `catalog.meta.totalStickers == 994`).

---

## Phase 2 — Domain

**Goal**: Pure-Kotlin model + use cases that orchestrate real logic.

- [ ] `domain/model/` — Sticker, Team, Group, StickerType, CollectionStats
- [ ] `domain/usecase/UpdateStickerQuantityUseCase.kt`
- [ ] `domain/usecase/GetOverallStatsUseCase.kt` (combine catalog + ownership)
- [ ] `domain/usecase/MatchScannedTextUseCase.kt` (stub now; real impl in Phase 7)
- [ ] `core/util/Fuzzy.kt` — Jaro–Winkler + accent-stripping normalizer
- [ ] `core/util/Duplicates.kt` — `duplicatesOf`, `isOwned`, `isDuplicated`

**Exit criteria**: domain module compiles; pure-Kotlin (no Android imports).

---

## Phase 3 — Home + Browse

**Goal**: Walk Home → Groups → Teams → Team Sheet end-to-end with real data.

- [ ] Nav3 setup: `NavKey` route sealed interface, `NavDisplay`, `entryProvider`
- [ ] `feature/home/` — hub with overall progress bar + 4 cards
- [ ] `feature/browse/groups/` — 12 group rows + Specials row
- [ ] `feature/browse/teams/` — 4 seed-ordered team rows
- [ ] `feature/browse/teamsheet/` — 4×5 grid with `StickerTile` component
- [ ] `core/ui/components/StickerTile.kt` — empty/owned/duplicated states
- [ ] Sort toggle (slot / alphabetical), persisted to DataStore
- [ ] Progress bars on every browse row

**Exit criteria**: Can navigate Home → Group A → Mexico → see 4×5 grid.

---

## Phase 4 — Sticker Detail + quantity editing

**Goal**: The bottom sheet works from Team Sheet; swipe gestures wire up.

- [ ] `feature/stickerdetail/` — bottom sheet composable + viewmodel
- [ ] Stepper (− / number / +), direct numeric entry on tap
- [ ] Swipe-right +1 on tile (with haptic), swipe-left −1
- [ ] Long-press tile = quick +1
- [ ] "Last added" line on Home reflects most recent ownership change
- [ ] Celebration animation on team reaching 100% (one-time per team)

**Exit criteria**: Can increment/decrement quantities from Team Sheet and
bottom sheet; UI reflects state instantly.

---

## Phase 5 — Search

**Goal**: Live, accent-insensitive search across all stickers.

- [ ] `feature/search/` screen with single text field + grouped results
- [ ] Reuses `Fuzzy.kt` normalizer + a substring/prefix match strategy
- [ ] Result item opens the same bottom sheet
- [ ] Empty state and "no results" copy

**Exit criteria**: Typing "rodr" returns Carlos Rodríguez, Ricardo Rodriguez, etc.

---

## Phase 6 — Stats / Reports

**Goal**: Progress, Missing, Duplicated reports.

- [ ] `feature/stats/` root with three tabs (or sub-routes)
- [ ] Progress: overall + per-group + per-team
- [ ] Missing: list, filters, copy-to-clipboard
- [ ] Duplicated: list with dupe counts, copy-to-clipboard, total footer
- [ ] Share intent on both Missing and Duplicated

**Exit criteria**: Reports populate from real ownership data; copy/share works.

---

## Phase 7 — Scan

**Goal**: Camera capture → OCR → match → bottom sheet save.

- [ ] Runtime camera permission flow
- [ ] CameraX preview + ImageCapture in `feature/scan/`
- [ ] ML Kit `TextRecognizer` invocation
- [ ] `MatchScannedTextUseCase` real impl: ID exact match → fuzzy on names/teams
- [ ] Match preview screen with best match + "not this one?" → top-3 picker
- [ ] "Rapid mode" toggle in Settings: auto-save +1 on score ≥ threshold
- [ ] Failure path: low/no confidence → manual search fallback

**Exit criteria**: Scanning a real sticker (or printed photo) matches the
correct player ≥ 85% of the time in good light.

---

## Phase 8 — Polish

**Goal**: Empty/error states, settings, theme audit, accessibility pass.

- [ ] Settings screen: about, reset progress (with confirm), sort defaults, rapid scan toggle, theme override (system/light/dark)
- [ ] Empty states across all list screens
- [ ] Error handling: catalog load failure → reinstall hint
- [ ] Dynamic color verified on Android 12+ device
- [ ] TalkBack labels on `StickerTile`, gesture alternatives surfaced in detail sheet
- [ ] Foil/sheen animation on Section 1 specials
- [ ] App icon (placeholder vector — final asset deferred)

**Exit criteria**: No `// TODO`s in feature code; light/dark/dynamic all look right.

---

## Phase 9 — Ship

**Goal**: Signed release APK installed on the user's device.

- [ ] Release signing config (debug-signed acceptable for personal use)
- [ ] `./gradlew assembleRelease` succeeds with R8 enabled
- [ ] Install via `adb install` on the user's device
- [ ] Manual smoke pass against `DESIGN.md` checklist
- [ ] Tag `v1.0.0` in git

**Exit criteria**: APK runs end-to-end on real hardware; user has scanned at
least one real sticker.

---

## Risks & mitigations

| Risk                                       | Mitigation                                            |
| ------------------------------------------ | ----------------------------------------------------- |
| OCR accuracy lower than ~85% in poor light | Top-3 picker + manual search fallback                 |
| Catalog drift if Panini revises the album  | `catalog.version` field + asset swap on next release  |
| Room schema changes mid-development        | Stay on version 1 until Phase 6 closes; migration after if needed |
| Nav3 API churn (still relatively new)      | Pin nav3Core, isolate routes in one file              |
| ML Kit model download on first scan        | Bundle model via Play Services dependency, prewarm on first launch |

---

## Out of scope explicitly

- iOS / cross-platform
- Multi-user, multi-album
- Cloud sync, user accounts
- Trading marketplace, friend matching
- Unit / instrumentation tests (v1)
- Tablet layouts
- Localization (English-only v1; Spanish later)

---

## Next action

Phase 0 — scaffold. Awaiting approval to begin.
