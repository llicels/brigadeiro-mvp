# Third-party notices

Brigadeiro is MIT-licensed (see [LICENSE](LICENSE)). It uses the following
third-party components, each under its own license:

## Fonts
- **Inter** — © The Inter Project Authors, licensed under the
  [SIL Open Font License 1.1](https://openfontlicense.org/).
  Bundled at `app/src/main/res/font/inter.ttf`.

## Android / Kotlin libraries
- **AndroidX / Jetpack Compose / Room / DataStore / Navigation** — Apache License 2.0.
- **Kotlin, kotlinx.coroutines, kotlinx.serialization** — Apache License 2.0.
- **OkHttp** — Apache License 2.0.
- **Coil** — Apache License 2.0.

## Machine learning / vision
- **ML Kit Text Recognition v2 (Google)** — used under the
  [ML Kit terms](https://developers.google.com/ml-kit/terms). The bundled OCR model
  runs on-device; receipt images are not uploaded by the app.

## Icons
- **Material Icons** (via `androidx.compose.material:material-icons-extended`) —
  Apache License 2.0.

## Food photos
- Recipe photos are illustrative and hotlinked from
  [TheMealDB](https://www.themealdb.com/) (free API, subject to its own terms).
  When offline or when a URL fails, the app falls back to locally drawn
  illustrated tiles, so no third-party image is bundled.

No model binaries (`*.gguf`, `*.task`, `*.litertlm`, ...) are committed to this
repository.
