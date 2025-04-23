/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyStatusChange = functions.firestore
  .document("issues/{issueId}")
  .onUpdate(async (change, context) => {
    const beforeStatus = change.before.data().status;
    const afterStatus = change.after.data().status;

    if (beforeStatus === afterStatus) {
      console.log("No status change");
      return null;
    }

    const submittedBy = change.after.data().submittedBy;

    const userDoc = await admin.firestore().collection("users").doc(submittedBy).get();
    const data = userDoc.data();
    const fcmToken = data && data.fcmToken;

    if (!fcmToken) {
      console.log("No FCM token for user:", submittedBy);
      return null;
    }

    const payload = {
      notification: {
        title: "Issue Status Updated",
        body: `Your issue is now marked as "${afterStatus}"`,
      },
    };

    return admin.messaging().sendToDevice(fcmToken, payload);
  });
