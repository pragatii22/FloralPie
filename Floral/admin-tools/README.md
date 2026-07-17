# admin-tools

Trusted, out-of-app scripts for administrator provisioning. This folder is
never bundled into the Android application and never contains credentials.

See `setAdminRole.js` for full usage instructions (also summarized in
`../firebase/SETUP.md`, section 5).

Quick start:

```bash
cd admin-tools
npm install
export FIREBASE_DATABASE_URL="https://<your-project>-default-rtdb.firebaseio.com"
export GOOGLE_APPLICATION_CREDENTIALS="/absolute/path/to/serviceAccountKey.json"
node setAdminRole.js <user-uid>
```

`serviceAccountKey.json`, if placed inside this folder instead of using
`GOOGLE_APPLICATION_CREDENTIALS`, is covered by the repository's `.gitignore`
— never commit it regardless.

## migrateOrdersToV2.js (Phase 4)

Backfills pre-Phase-4 orders with the new OrderModel v2 fields (schema version,
normalized status, synthesized order number, pricing wrapper). Never modifies
product stock, never rewrites the original total or timestamps. Dry-run by
default:

```bash
node migrateOrdersToV2.js --dry-run
node migrateOrdersToV2.js --apply
```

Back up your Realtime Database (export JSON from the Console) before running
with `--apply`. See `../firebase/SETUP.md` section 7.4 for details.
