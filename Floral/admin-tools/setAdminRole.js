/**
 * Secure administrator provisioning script.
 *
 * This is the ONLY supported way to grant a user administrator access in the
 * Floral app. The Android client can never assign the admin role to itself or
 * to any other account -- registration always creates role="user", and the
 * database rules (see ../firebase/database.rules.json) reject any client
 * write that tries to change a role field unless the actor already has
 * role="admin".
 *
 * What this script does for a given user UID:
 *   1. Sets /users/{uid}/role to "admin" in the Realtime Database.
 *   2. Sets a Firebase Auth custom claim { admin: true } on the account, which
 *      the Storage security rules use (Storage rules cannot read the Realtime
 *      Database, so the RTDB "role" field alone is not enough to authorize
 *      product-image uploads).
 *
 * Usage:
 *   1. Generate a service account key from:
 *      Firebase Console -> Project Settings -> Service Accounts -> Generate new private key
 *   2. Save it locally as admin-tools/serviceAccountKey.json
 *      (this exact filename is already covered by .gitignore -- never commit it)
 *   3. Install dependencies once:
 *      cd admin-tools && npm install firebase-admin
 *   4. Run:
 *      node setAdminRole.js <user-uid>
 *
 * Alternative to the JSON file: set GOOGLE_APPLICATION_CREDENTIALS to the
 * absolute path of the key file instead of placing it in this folder, and the
 * script will use that automatically.
 *
 * After running this script, the affected user must sign out and sign back
 * in (or otherwise force a token refresh) before the admin custom claim shows
 * up on their ID token.
 */

const path = require('path');
const fs = require('fs');
const admin = require('firebase-admin');

const uid = process.argv[2];

if (!uid) {
  console.error('Usage: node setAdminRole.js <user-uid>');
  process.exit(1);
}

const localKeyPath = path.join(__dirname, 'serviceAccountKey.json');

if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    databaseURL: process.env.FIREBASE_DATABASE_URL,
  });
} else if (fs.existsSync(localKeyPath)) {
  const serviceAccount = require(localKeyPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: process.env.FIREBASE_DATABASE_URL,
  });
} else {
  console.error(
    'No credentials found. Either set GOOGLE_APPLICATION_CREDENTIALS or place ' +
    'a service account key at admin-tools/serviceAccountKey.json (see the ' +
    'usage instructions at the top of this file).'
  );
  process.exit(1);
}

if (!process.env.FIREBASE_DATABASE_URL) {
  console.error(
    'Set FIREBASE_DATABASE_URL to your Realtime Database URL, e.g.\n' +
    '  export FIREBASE_DATABASE_URL="https://floral-29504-default-rtdb.firebaseio.com"'
  );
  process.exit(1);
}

async function main() {
  const userRecord = await admin.auth().getUser(uid);

  await admin.auth().setCustomUserClaims(uid, { admin: true });
  await admin.database().ref(`users/${uid}`).update({
    role: 'admin',
    updatedAt: Date.now(),
  });

  console.log(`Granted admin role to ${userRecord.email || uid} (${uid}).`);
  console.log('The user must sign out and back in for the change to take effect.');
}

main()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error('Failed to set admin role:', err);
    process.exit(1);
  });
