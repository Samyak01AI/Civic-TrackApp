import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

exports.sendNewIssueNotification = functions.firestore
  .document("Issues/{issueId}")
  .onCreate(async (snapshot, context) => {
    const issue = snapshot.data();

    // Get all user FCM tokens
    const usersSnapshot = await admin.firestore().collection("users").get();
    const tokens: string[] = [];

    usersSnapshot.forEach((doc) => {
      const token = doc.data()?.fcmToken;
      if (token) tokens.push(token);
    });

    if (tokens.length === 0) {
      console.log("No FCM tokens available");
      return null;
    }

    const payload: admin.messaging.MulticastMessage = {
      notification: {
        title: "New Issue Reported",
        body: `${issue.title} in ${issue.location}`,
      },
      data: {
        issueId: context.params.issueId,
        type: "new_issue",
        click_action: "FLUTTER_NOTIFICATION_CLICK",
      },
      tokens: tokens,
    };

    try {
      const response = await admin.messaging().sendMulticast(payload);
      console.log(`${response.successCount} notifications sent successfully`);
      if (response.failureCount > 0) {
        console.log(`${response.failureCount} notifications failed`);
        response.responses.forEach((resp, idx) => {
          if (!resp.success) {
            console.error(`Failure for token ${tokens[idx]}:`, resp.error);
          }
        });
      }
      return null;
    } catch (error) {
      console.error("Error sending notifications:", error);
      return null;
    }
  });

