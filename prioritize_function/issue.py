import base64
import json
import firebase_admin
from firebase_admin import credentials, firestore, messaging
from flask import Request

cred = credentials.ApplicationDefault()
firebase_admin.initialize_app(cred)

def initialize_firebase():
    if not firebase_admin._apps:
        firebase_admin.initialize_app()

def compute_priority(issue):
    text = (issue.get('title', '') + ' ' + issue.get('description', '')+ ' ' + issue.get('category', '')).lower()
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

def prioritize_from_event(event, context):
    initialize_firebase()
    db = firestore.client()  # ✅ Moved here, after Firebase is initialized

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

    except Exception as e:
        print(f"Error: {str(e)}")
        
def send_notification(issue):
    topic = "issues"
    message = messaging.Message(
        notification=messaging.Notification(
            title="🚨 Recently new Issue is Reported",
            body=f"{issue.get('title', 'No Title')}: {issue.get('description', 'No Description')}",
        ),
        topic=topic,
    )

    try:
        response = messaging.send(message)
        print(f"✅ Notification sent to topic '{topic}': {response}")
    except Exception as e:
        print(f"❌ Failed to send topic notification: {e}")

def notify_on_new_issue(request: Request):
    envelope = request.get_json()
    if not envelope or "message" not in envelope:
        return "Bad Request: no Pub/Sub message received", 400

    pubsub_message = envelope["message"]
    data = pubsub_message.get("data")
    if data:
        payload = base64.b64decode(data).decode("utf-8")
        issue = json.loads(payload)
        print("Received issue:", issue)
        send_notification(issue)
    else:
        print("No data in message")

    return "OK", 200
