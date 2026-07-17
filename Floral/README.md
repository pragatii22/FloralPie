# Floral

A flower-selling e-commerce Android app (Kotlin, Jetpack Compose, Firebase) with a customer
storefront and an administrator console in a single app, gated by role.

## Stack

- **Android**: Kotlin, Jetpack Compose, Navigation Compose, single-Activity architecture, MVVM +
  Repository pattern, manual dependency injection (`AppContainer`), Kotlin Coroutines/Flow,
  WorkManager, DataStore.
- **Backend**: Firebase Authentication, Realtime Database, Storage, Cloud Functions (TypeScript,
  Functions v2), Cloud Messaging (FCM), App Check, Crashlytics, Analytics, Performance Monitoring.
- **CI**: GitHub Actions (`.github/workflows/ci.yml`) — Cloud Functions lint/build/test, Android
  ktlint + unit tests + lint + debug/release assembly, Firebase Rules and Functions integration
  tests against the Emulator Suite.

## Repository layout

```
app/                    Android application module
functions/               Firebase Cloud Functions (TypeScript) -- all trusted server-side logic
functions/integration-tests/  Emulator-backed Cloud Functions integration tests
firebase/                Realtime Database + Storage security rules, and firebase/SETUP.md
firebase-tests/          Emulator-backed Firebase Rules tests (database + storage)
admin-tools/             One-off Admin SDK scripts (role provisioning, legacy order migration)
docs/                    Architecture notes, release checklist, privacy checklist
```

## Getting started

1. Firebase project setup, security rules deployment, and administrator provisioning:
   see [`firebase/SETUP.md`](firebase/SETUP.md).
2. Cloud Functions local development, testing, and deployment:
   see [`functions/README.md`](functions/README.md).
3. Android app: open `app/` in Android Studio, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```
   Release builds require a signing configuration — see
   [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md).
4. Firebase Rules tests (requires the Firebase Emulator Suite):
   ```bash
   firebase emulators:exec --only database,storage "cd firebase-tests && npm ci && npm test"
   ```

## Continuous Integration

`.github/workflows/ci.yml` runs on every push/PR to `main` and requires one repository secret to
build the Android app at all:

- `GOOGLE_SERVICES_JSON_BASE64` — base64-encoded contents of `app/google-services.json` (per-
  Firebase-project config, gitignored, never committed). Without it, the Android job fails at the
  "Write google-services.json" step.

Optional secrets, used only if present (their absence produces an unsigned release build/bundle,
which is still useful for verifying the release configuration compiles and R8 succeeds):

- `FLORAL_KEYSTORE_PATH`, `FLORAL_KEYSTORE_PASSWORD`, `FLORAL_KEY_ALIAS`, `FLORAL_KEY_PASSWORD` —
  see [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md) § Release signing.

CI never deploys anything (`firebase deploy`, Play Console upload) — deployment is a manual,
deliberate step documented in `firebase/SETUP.md` and `functions/README.md`.

## Architecture and history

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — how the Android app and backend fit together.
- [`CHANGELOG.md`](CHANGELOG.md) — what changed in each phase of the project's development.
- [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md) — steps before shipping a release build.
- [`docs/PRIVACY_CHECKLIST.md`](docs/PRIVACY_CHECKLIST.md) — what user data this app collects and
  where it lives.

## Known gaps

- Cash on delivery is the only payment method — no online payments.
- No live delivery tracking, product reviews, coupons, or loyalty points.
- The production Android application ID is still the placeholder `com.example.floral` — a
  deliberate release blocker requiring the project owner's decision (see
  [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md) § Application ID).
- Full password-reauthentication UI for account deletion is not implemented (a server-side
  recent-login-window check stands in for it — see
  [`docs/PRIVACY_CHECKLIST.md`](docs/PRIVACY_CHECKLIST.md) § Account deletion).

See `CHANGELOG.md` (Phase 6 entry) for the full, current list of what's implemented and what
remains — the storefront gap noted in earlier phases (favorites, search, categories, sort, cart
quantity controls) was closed in Phase 6.
