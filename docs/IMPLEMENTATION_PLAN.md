# Brigadeiro — Implementation Plan

## Current state

- Target repo `brigadeiro-mvp` started empty and is not a git repository.
- Reference Android project `../mvp_cards` (com.lekha.cards) builds a working debug APK with:
  Kotlin 2.0.20, Compose BOM 2024.09.03, Room 2.6.1 + KSP, manual DI (`AppContainer`),
  Navigation Compose, DataStore, Robolectric + Compose UI tests. Its Gradle config is
  reused here verbatim to avoid dependency drift.
- Toolchain lives in `~/.android-toolchain`: JDK 17 (Temurin), Gradle 8.9, SDK with only
  `android-34`, no NDK. Internet is available.
- Brand site `../brigadeiro`: maroon `#660014`, sage `#B5C4A1`, orange `#F09D51`, pink
  `#F3E0EC`; fonts Eczar (display) + Varta (body); star motifs.

## Architecture

Layered, manual DI, no empty abstractions:

```
com.brigadeiro.app
├── di/            AppContainer, ViewModelFactory
├── data/          database (Room), local (DataStore + model manager), remote (OkHttp), repository
├── domain/        model, repository interfaces, usecases
├── ai/            AIProvider (fake/local/remote), ingredient extractor, RecipeGenerator
├── audio/         AudioRecorder, SpeechToTextService (system on-device; Whisper behind interface)
└── ui/            theme, components, navigation, screens
```

Data flow: UI → ViewModel → use case → repository → Room. The AI layer is behind
interfaces and never touched directly by the UI.

Validation pipeline (raw AI output is never persisted):

```
extractor → JSON parser → schema validation → domain validation → normalization
          → IngredientDraft(uncertain flag) → confirmation screen → repository merge
```

## Dependencies

Pinned to the `mvp_cards` set (proven on SDK 34): AGP 8.6.1, Kotlin 2.0.20, KSP
2.0.20-1.0.25, Compose BOM 2024.09.03, Navigation 2.8.0, Room 2.6.1, DataStore 1.1.1,
Coroutines 1.8.1, kotlinx-serialization 1.7.3, OkHttp 4.12.0, JUnit4/Truth/Robolectric.
`compileSdk`/`targetSdk` 34, `minSdk` 29.

## Decisions (confirmed)

- **STT:** `SpeechToTextService` interface; default `AndroidSpeechToTextService` using
  `createOnDeviceSpeechRecognizer` + `EXTRA_PREFER_OFFLINE` (no model download);
  `WhisperSpeechToTextService` optional behind the same interface (NDK/model deferred).
- **Extraction:** `HeuristicIngredientExtractor` (PT-BR rules, quantities, intent
  detection, synonyms, uncertainty flags) is the offline default; `LlmIngredientExtractor`
  over `AIProvider` is optional; `FakeAIProvider` for tests.
- **Recipes:** `RecipeGenerator` with `CatalogRecipeGenerator` (bundled offline catalog,
  default), `LocalRecipeGenerator` (optional Gemma), `RemoteRecipeGenerator`
  (OpenAI-compatible, disabled until a key is configured).
- **Theme:** site-driven palette + Eczar/Varta, mapped into Material 3 roles.
- **Device:** physical Android device available for verification.

## Risks

1. whisper.cpp requires NDK+CMake and a 75–150 MB model that cannot be committed → kept
   optional behind an interface; system STT is the shipping path.
2. On-device LLM is ~1 GB and device-dependent → heuristic extractor and offline recipe
   catalog guarantee the three success scenarios without it.
3. Remote keys must not be committed → stored in DataStore, supplied manually.
4. Only `android-34` is installed → dependency versions pinned to the proven set.
5. Orange/maroon contrast → used as accents, text uses ink/maroon roles.

## Phases

0. Plan + scaffold (this document). ✅
1. Theme, design system, navigation, domain models, Room, DI. ✅
2. Minha Cozinha: add/edit/remove/search/filter, empty state. ✅
3. Text input → extraction → validation → confirmation → persistence. ✅
4. Voice input → system STT → same pipeline. ✅ (hardened: fresh recognizer per
   session, availability check, on-device → system-UI fallback, watchdog.)
5. Ideias + RecipeDetail via `RecipeGenerator`, offline/online states. ✅
6. Polish, accessibility, animations, docs, tests. ✅

## SDD MVP alignment (this iteration)

- Delivery-style cards: illustrated image, total/active time, estimated cost,
  difficulty and availability badge. ✅
- Kitchen equipment profile in onboarding + Ajustes, enforced by the catalog. ✅
- Time filters (10/20/30 min / any) on Ideias. ✅
- Ingredient substitutions shown as options in the recipe detail. ✅
- Spec committed as [SDD.md](SDD.md). ✅
- Wireless-ADB install loop (`scripts/`). ✅
- Stock made visible (Home card + Kitchen header) and one-tap entry points. ✅
- **Receipt OCR** (photo → items → review), pulled forward from Phase 3. ✅

Deferred (post-MVP, per SDD): freshness/validity, dashboard, shopping
list, marketplace/delivery integration, effort-level filter.
