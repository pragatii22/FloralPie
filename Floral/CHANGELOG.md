# Changelog

This project was built in five phases. Each phase's entry describes what changed and, where
relevant, what was deliberately left out.

## Phase 5 — Notifications, support, analytics, settings, and production hardening

**Backend (Cloud Functions)**
- FCM push notifications: an outbox pattern (`/notificationOutbox/{eventId}`) written atomically
  inside the same transaction as the order/ticket mutation that triggers it, processed out-of-band
  by `processNotificationOutbox` (claim/lease based, idempotent, retries safely).
- In-app notification center backed by `/notifications/{uid}`, with per-user preferences at
  `/notificationPreferences/{uid}` (order updates, support replies, general announcements, and the
  admin-only new-order / inventory-alert toggles).
- Device token registration (`registerDeviceToken` / `unregisterDeviceToken`) with a secondary
  `deviceTokenOwners` index so a token always serves its current owner, even across account
  switches on the same device.
- Inventory alerts: a trigger on `/products/{productId}/quantity` classifies low-stock /
  out-of-stock severity and only notifies on a genuine state transition (not on every write),
  with an admin-only acknowledge action that never auto-resolves the underlying alert.
- Administrator analytics (`getAdminAnalytics`): order/revenue/customer/inventory rollups over a
  7/30/90-day range, computed from existing `/orders`, `/users`, `/products`, `/inventoryAlerts`.
- Customer support tickets: creation, threaded replies (customer/admin), and admin-controlled
  status/priority changes, each triggering the appropriate notification to the other party.
- `FUNCTIONS_REGION` (`asia-south1`) and `enforceAppCheck` are now centralized in
  `common/functionOptions.ts`; every callable is built through `buildCallableOptions()`.

**Android**
- Firebase Cloud Messaging: a foreground/background messaging service, four notification channels
  (order updates, support updates, inventory alerts, general), a deep-link parser that only lets a
  notification navigate somewhere the signed-in user's role/ownership actually permits, and a
  runtime `POST_NOTIFICATIONS` permission request after sign-in.
- New customer screens: notification center, settings (theme + notification preferences), help &
  support (ticket list, new ticket form, ticket detail/reply thread).
- New administrator screens: analytics dashboard, inventory alerts, support ticket queue and
  detail/reply.
- App-wide theme preference (system / light / dark), persisted via DataStore, applied at the
  `MainActivity` root so it affects every screen including auth and both role shells.
- Firebase App Check: a Play Integrity provider in release builds, a Debug provider in debug
  builds (via separate `src/debug` / `src/release` source sets), installed unconditionally;
  server-side enforcement stays behind the `FLORAL_ENFORCE_APP_CHECK` environment flag so
  un-migrated clients are never locked out by an unplanned enable.
- Firebase Analytics (login, sign_up, view_item, add_to_cart, begin_checkout, purchase),
  Crashlytics (per-user tagging, non-fatal recording on unexpected repository errors), and
  Performance Monitoring (custom traces around checkout quote and order placement).
- Release build hardening: environment/`keystore.properties`-driven signing (never a committed
  keystore), `isMinifyEnabled` / `isShrinkResources` with scoped ProGuard/R8 keep rules,
  `allowBackup="false"`, `usesCleartextTraffic="false"`.
- ktlint added as the Android static analysis tool (see `app/build.gradle.kts`), with a baseline
  file (`app/config/ktlint/baseline.xml`) grandfathering pre-Phase-5 style so the check gates new
  code without demanding an unrelated repo-wide reformat.
- GitHub Actions CI (`.github/workflows/ci.yml`): Cloud Functions lint/build/test, and Android
  ktlint + unit tests + debug assembly.

**Known gap carried into this phase:** favorites, product search, category browsing, sort order,
and in-cart quantity controls (Phase 3 scope) are still not implemented — see the Phase 3 note
below. The Phase 5 specification for this project was received truncated (cut short partway
through, before its final acceptance-criteria and response-format sections); implementation
proceeded on the received portion, per explicit instruction.

## Phase 4 — Secure, server-authoritative checkout

- All order mutations (`placeOrder`, `cancelOrder`, `updateOrderStatus`) moved into Cloud
  Functions; `/orders` became write-locked for every client, including admins.
- Atomic, idempotent checkout: a `checkoutRequestId` (persisted across process death) makes a
  retried submission replay the original result rather than creating a duplicate order.
- Delivery addresses and scheduling (recipient details, delivery slot, requested date, gift
  message), order history/details/cancellation for customers, and admin order management with a
  controlled status-transition graph.
- Legacy order compatibility: orders placed before this phase (no `schemaVersion`, capitalized
  statuses) render with a "historical order" indicator instead of being silently rewritten.

## Phase 3 — Not implemented as its own phase

Favorites, search, category browsing, sort order, and in-cart quantity controls were never
implemented. This is called out explicitly (rather than silently backfilled) per standing
instruction across every subsequent phase's report.

## Phase 2 — Single-Activity architecture

- Consolidated every screen behind one `MainActivity` and Navigation Compose, replacing the
  original multi-Activity structure.
- Introduced `AppContainer`/`DefaultAppContainer` for manual dependency injection and
  `floralViewModelFactory` as the single ViewModel factory, with StateFlow-based ViewModels and
  coroutine-based repositories throughout.

## Phase 1 — Foundation and security recovery

- Removed plaintext password storage from `/users/{uid}` (Firebase Authentication was always the
  actual credential store).
- Removed the hardcoded administrator account; registration always writes `role: "user"`, and
  database rules reject a client attempt to set `role: "admin"` on a new profile.
- Hand-written Realtime Database and Storage security rules, plus `admin-tools/` scripts for
  administrator provisioning and legacy order migration.
