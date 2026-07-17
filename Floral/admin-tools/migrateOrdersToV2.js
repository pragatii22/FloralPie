/**
 * One-time, idempotent migration of legacy (Phase 1-3) orders to the Phase 4
 * OrderModel v2 shape. Never touches product stock. Never rewrites the original
 * total or timestamps -- it only *adds* the new fields so legacy orders render
 * consistently in the new order-history / order-details UI.
 *
 * What it adds to each legacy order (schemaVersion missing or < 2):
 *   - schemaVersion: 2
 *   - orderNumber (synthesized, stable once written -- re-running does not change it)
 *   - status: normalized to the new lowercase values (Pending -> placed, etc.)
 *   - pricing: { currency, subtotalMinor: 0, deliveryFeeMinor: 0, discountMinor: 0,
 *                totalMinor: round(oldTotalAmount * 100) }
 *     (subtotal/delivery-fee are unknown for legacy orders, so they're left at 0 --
 *     only the total is preserved, converted to minor units)
 *   - statusHistory: a single synthesized entry using the old order's own timestamp
 *   - deliveryAddress / deliverySlot / giftMessage: left as empty/unavailable markers
 *     (this information was never captured before Phase 4 and cannot be reconstructed)
 *
 * Idempotent: an order that already has schemaVersion >= 2 is skipped entirely.
 *
 * Usage:
 *   node migrateOrdersToV2.js --dry-run     # prints what would change, writes nothing
 *   node migrateOrdersToV2.js --apply       # actually writes the migrated fields
 *
 * Credentials: same as setAdminRole.js -- set GOOGLE_APPLICATION_CREDENTIALS, or place
 * a service account key at admin-tools/serviceAccountKey.json (gitignored), and set
 * FIREBASE_DATABASE_URL. Back up your database (Realtime Database -> export JSON) before
 * running with --apply.
 */

const path = require("path");
const fs = require("fs");
const admin = require("firebase-admin");

const APPLY = process.argv.includes("--apply");
const DRY_RUN = !APPLY;

const localKeyPath = path.join(__dirname, "serviceAccountKey.json");

if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    databaseURL: process.env.FIREBASE_DATABASE_URL,
  });
} else if (fs.existsSync(localKeyPath)) {
  admin.initializeApp({
    credential: admin.credential.cert(require(localKeyPath)),
    databaseURL: process.env.FIREBASE_DATABASE_URL,
  });
} else {
  console.error(
    "No credentials found. Set GOOGLE_APPLICATION_CREDENTIALS or place a service " +
      "account key at admin-tools/serviceAccountKey.json."
  );
  process.exit(1);
}

if (!process.env.FIREBASE_DATABASE_URL) {
  console.error("Set FIREBASE_DATABASE_URL to your Realtime Database URL.");
  process.exit(1);
}

function normalizeLegacyStatus(rawStatus) {
  switch (rawStatus) {
    case "Pending":
      return "placed";
    case "Confirmed":
      return "confirmed";
    case "Cancelled":
      return "cancelled";
    default:
      return typeof rawStatus === "string" ? rawStatus : "placed";
  }
}

function synthesizeOrderNumber(orderId, createdAt) {
  const date = new Date(createdAt || Date.now());
  const y = date.getUTCFullYear();
  const m = String(date.getUTCMonth() + 1).padStart(2, "0");
  const d = String(date.getUTCDate()).padStart(2, "0");
  const suffix = orderId.slice(-6).toUpperCase();
  return `FL-${y}${m}${d}-LEGACY-${suffix}`;
}

async function migrate() {
  const ordersSnapshot = await admin.database().ref("orders").get();
  if (!ordersSnapshot.exists()) {
    console.log("No orders found. Nothing to migrate.");
    return;
  }

  let scanned = 0;
  let migrated = 0;
  let skippedAlreadyV2 = 0;
  const updates = {};

  ordersSnapshot.forEach((userOrders) => {
    const uid = userOrders.key;
    userOrders.forEach((orderSnap) => {
      scanned++;
      const order = orderSnap.val();
      const orderId = orderSnap.key;

      if ((order.schemaVersion ?? 1) >= 2) {
        skippedAlreadyV2++;
        return false;
      }

      const createdAt = order.orderDate ?? order.createdAt ?? Date.now();
      const status = normalizeLegacyStatus(order.status);
      const totalMinor = Math.round((order.totalAmount ?? 0) * 100);

      const patch = {
        schemaVersion: 2,
        orderNumber: synthesizeOrderNumber(orderId, createdAt),
        status,
        createdAt,
        updatedAt: Date.now(),
        pricing: {
          currency: "NPR",
          subtotalMinor: totalMinor,
          deliveryFeeMinor: 0,
          discountMinor: 0,
          totalMinor,
        },
        deliveryAddress: {
          recipientName: "",
          phoneNumber: "",
          addressLine: "Historical order -- delivery details unavailable",
          city: "",
          area: "",
          landmark: "",
          deliveryInstructions: "",
        },
        requestedDeliveryDate: "",
        deliverySlot: { slotId: "", label: "Unavailable", start: "", end: "" },
        giftMessage: "",
        paymentMethod: "cash_on_delivery",
        paymentStatus: status === "cancelled" ? "cancelled" : status === "delivered" ? "collected" : "pending",
        statusHistory: [
          {
            status,
            changedAt: createdAt,
            changedByUid: order.userId ?? uid,
            changedByRole: "system_migration",
            note: "Backfilled by migrateOrdersToV2.js",
          },
        ],
        cancellation: status === "cancelled" ? {
          reasonCode: "unknown",
          reasonText: "Historical cancellation -- reason not captured before Phase 4",
          cancelledByUid: "",
          cancelledByRole: "unknown",
          cancelledAt: createdAt,
          stockRestoredAt: null,
        } : null,
      };

      updates[`orders/${uid}/${orderId}`] = { ...order, ...patch };
      migrated++;
      return false;
    });
  });

  console.log(`Scanned ${scanned} orders.`);
  console.log(`Already schemaVersion >= 2 (skipped): ${skippedAlreadyV2}.`);
  console.log(`${DRY_RUN ? "Would migrate" : "Migrating"}: ${migrated}.`);

  if (DRY_RUN) {
    console.log("\nDry run only -- no writes performed. Re-run with --apply to write these changes.");
    console.log("Sample of paths that would be updated:", Object.keys(updates).slice(0, 5));
    return;
  }

  if (migrated === 0) {
    console.log("Nothing to write.");
    return;
  }

  await admin.database().ref().update(updates);
  console.log(`Wrote migrated fields for ${migrated} orders.`);
}

migrate()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("Migration failed:", err);
    process.exit(1);
  });
