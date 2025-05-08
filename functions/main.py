import firebase_admin
from firebase_admin import credentials, firestore, messaging
import threading

# Initialize Firebase Admin SDK
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Track already seen documents to prevent duplicate sends
seen_ids = set()

def send_fcm_notification(title, body):
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        topic="issues"
    )
    response = messaging.send(message)
    print("✅ Notification sent with message ID:", response)

def listen_for_latest_issue():
    print("🚀 Listening for new issues in 'Issues' collection...")
    # Flag to ignore the first snapshot containing all existing documents
    initial_snapshot = True

    def on_snapshot(col_snapshot, changes, read_time):
        nonlocal initial_snapshot
        # If this is the initial snapshot, don't process any changes.
        if initial_snapshot:
            print("🔔 Initial snapshot received. Skipping existing issues.")
            initial_snapshot = False
            return

        for change in changes:
            # Process only newly added documents.
            if change.type.name == "ADDED":
                doc = change.document
                # Prevent duplicate notifications
                if doc.id in seen_ids:
                    continue
                seen_ids.add(doc.id)
                data = doc.to_dict()
                title = data.get("title", "New Issue")
                category = data.get("category", "")
                body = f"New issue reported: {category}" if category else "A new issue has been reported."
                print(f"📄 New issue: {title} | Category: {category}")
                send_fcm_notification(title, body)

    db.collection("Issues").on_snapshot(on_snapshot)

if __name__ == "__main__":
    listen_for_latest_issue()
    threading.Event().wait()  # Keeps script running
