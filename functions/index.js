const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {PubSub} = require("@google-cloud/pubsub");

const pubsub = new PubSub();

exports.forwardIssueToPubSub = onDocumentCreated("Issues/{issueId}",
    async (event) => {
      const snap = event.data;
      if (!snap.exists) {
        console.warn("Document does not exist");
        return;
      }

      const issue = snap.data();
      issue.id = event.params.issueId;  // Add this line to include document ID

      try {
        await pubsub.topic("issue-prioritization")
            .publishMessage({json: issue});
        console.log("Published issue to Pub/Sub:", issue);
      } catch (err) {
        console.error("Failed to publish to Pub/Sub:", err);
      }
    });
