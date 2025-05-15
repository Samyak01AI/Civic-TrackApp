export interface User {
    fcmToken?: string;
    email: string;
    isAdmin?: boolean;
  }

export interface Issue {
    title: string;
    description: string;
    location: string;
    status: "pending" | "in_progress" | "resolved";
    timestamp: admin.firestore.Timestamp;
    userId: string;
    imageUrl?: string;
  }

