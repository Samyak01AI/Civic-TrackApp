import base64
import json
import firebase_admin
from firebase_admin import credentials, firestore, messaging

def initialize_firebase():
    if not firebase_admin._apps:
        firebase_admin.initialize_app()

def compute_priority(issue):
    text = (issue.get('title', '') + ' ' + issue.get('description', '') + ' ' + issue.get('category', '')).lower()
    keywords = {
        'fire': 5,
        'accident': 4,
        'injury': 4,
        'sewage': 3,
        'garbage': 2,
        'pothole': 2,
        'urgent': 2
    }

    score = 0
    for word, value in keywords.items():
        if word in text:
            score += value
    if issue.get('imageUrl'):
        score += 1
    if issue.get('location'):
        score += 1
    return min(score, 10)

def send_notification(issue, score):
    title = f"New Issue Reported: {issue.get('title', 'New Issue')}"
    body = f"Priority: {score} - {issue.get('description', '')[:100]}"

    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        topic='issues'  # Change to token if sending directly to a device
    )

    try:
        response = messaging.send(message)
        print(f"✅ Notification sent successfully. Message ID: {response}")
    except Exception as e:
        print(f"❌ Failed to send notification: {e}")

def prioritize_from_event(event, context):
    initialize_firebase()
    db = firestore.client()

    if 'data' not in event:
        print("No data in Pub/Sub message.")
        return

    try:
        message = base64.b64decode(event['data']).decode('utf-8')
        issue = json.loads(message)

        score = compute_priority(issue)
        doc_id = issue['id']

        db.collection('Issues').document(doc_id).update({
            'priority_score': score,
            'last_prioritized': firestore.SERVER_TIMESTAMP
        })

        print(f"Issue {doc_id} updated with priority {score}")
        send_notification(issue, score)

    except Exception as e:
        print(f"Error: {str(e)}")
