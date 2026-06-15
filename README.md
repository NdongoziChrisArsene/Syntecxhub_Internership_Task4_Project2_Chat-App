# ChatApp 💬

A real-time Android chat application built with Firebase. Users can register, log in, send and receive messages instantly, and manage their profile — all powered by Firebase Authentication, Firestore, and Storage.

---

## Features

- **User Authentication** — Register and log in with email and password using Firebase Auth
- **Real-time Messaging** — Messages appear instantly on both sides without refreshing, powered by Firestore's real-time listeners
- **Chat Bubbles** — Sent messages appear right-aligned (green), received messages appear left-aligned (white)
- **User Profiles** — Each user has a profile stored in Firestore (name, email, status, profile picture URL)
- **Profile Pictures** — Users can upload a profile photo stored in Firebase Storage
- **Offline Support** — Firestore caches data locally; messages sent offline are queued and synced automatically when connection is restored
- **Message Ordering** — Messages are always displayed in correct chronological order using Firebase server timestamps

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Firebase Authentication | User login and registration |
| Firebase Firestore | Real-time database for messages and user profiles |
| Firebase Storage | Profile picture storage |
| RecyclerView + ListAdapter | Efficient real-time message list with DiffUtil |
| Glide | Loading profile images from URLs |

---

## Project Structure

```
ChatApp/
├── build.gradle.kts                  # Root project build file
├── settings.gradle.kts               # Project settings
└── app/
     ├── google-services.json         # Firebase config (your own file)
     ├── build.gradle.kts             # App-level build file + dependencies
     └── src/main/
          ├── AndroidManifest.xml     # App manifest + activity registration
          ├── java/com/example/chatapp/
          │    ├── LoginActivity.kt        # Login screen
          │    ├── SignupActivity.kt       # Registration screen
          │    ├── MainActivity.kt         # Chat list screen (home)
          │    ├── ChatActivity.kt         # Individual chat conversation screen
          │    ├── Message.kt              # Message data model
          │    └── MessageAdapter.kt       # RecyclerView adapter for chat bubbles
          └── res/
               ├── layout/
               │    ├── activity_login.xml
               │    ├── activity_signup.xml
               │    ├── activity_main.xml
               │    ├── activity_chat.xml
               │    ├── item_message_sent.xml      # Green right-aligned bubble
               │    └── item_message_received.xml  # White left-aligned bubble
               └── drawable/
                    ├── bg_bubble_sent.xml          # Sent bubble shape
                    └── bg_bubble_received.xml      # Received bubble shape
```

---

## Firestore Data Structure

```
users (collection)
 └── {userId} (document)
      ├── name: "John Doe"
      ├── email: "john@example.com"
      ├── profileImageUrl: "https://..."
      ├── status: "Hey there! I'm using ChatApp"
      ├── isOnline: true
      └── lastSeen: Timestamp

chats (collection)
 └── {chatId} (document)
      ├── participants: ["uid1", "uid2"]
      ├── lastMessage: "See you tomorrow!"
      ├── lastMessageTimestamp: Timestamp
      └── lastMessageSenderId: "uid1"
      │
      └── messages (subcollection)
           └── {messageId} (document)
                ├── senderId: "uid1"
                ├── text: "Hey!"
                ├── type: "text"
                ├── timestamp: Timestamp
                └── status: "sent"
```

---

## Getting Started

### Prerequisites

- Android Studio (Hedgehog or newer recommended)
- Android device or emulator running API 24 (Android 7.0) or higher
- A Firebase account at [console.firebase.google.com](https://console.firebase.google.com)

### Setup Steps

#### 1. Clone or extract the project
Open Android Studio → **File → Open** → select the `ChatApp` folder.

#### 2. Connect to Firebase
- Go to [console.firebase.google.com](https://console.firebase.google.com)
- Create a new project (or use an existing one)
- Register an Android app with package name `com.example.chatapp`
- Download `google-services.json` and place it in the `app/` folder

#### 3. Enable Firebase services
In your Firebase console, enable the following:

| Service | Path in Console |
|---|---|
| Email/Password Auth | Build → Authentication → Sign-in method → Email/Password → Enable |
| Firestore Database | Build → Firestore Database → Create database → Test mode |
| Firebase Storage | Build → Storage → Get started |

#### 4. Sync and Run
- Click **Sync Now** when Android Studio prompts you after placing `google-services.json`
- Wait for Gradle to finish downloading dependencies
- Press **Run ▶** to launch on your device or emulator

---

## Firebase Security Rules

### Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    match /chats/{chatId} {
      allow read, write: if request.auth.uid in resource.data.participants;
      match /messages/{messageId} {
        allow read, write: if request.auth.uid in
          get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
      }
    }
  }
}
```

### Storage Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
```

> ⚠️ The app starts in **Test mode** (all reads/writes open for 30 days).
> Replace with the rules above before your 30-day test window expires.

---

## How It Works

### Authentication Flow
1. App launches → `LoginActivity` checks if a user is already logged in
2. If logged in → goes straight to `MainActivity` (chat list)
3. If not → user logs in or registers via `SignupActivity`
4. On registration → Firebase Auth creates the account AND a `users/{uid}` document is saved to Firestore with the user's name, email, and default status

### Real-time Messaging Flow
1. User opens a chat → `ChatActivity` attaches a Firestore `addSnapshotListener` to `chats/{chatId}/messages` ordered by timestamp
2. User sends a message → written to Firestore using `FieldValue.serverTimestamp()`
3. Firestore pushes the update to all listeners instantly → `MessageAdapter` uses `DiffUtil` to update only the new message in the `RecyclerView`
4. The parent `chats/{chatId}` document is also updated with `lastMessage` and `lastMessageTimestamp` for the chat list preview

### Offline Handling
Firestore offline persistence is enabled by default on Android. When a user has no internet:
- Previously loaded messages are still visible from local cache
- New messages typed are written to local cache immediately (appear in UI instantly)
- Once internet is restored, Firestore syncs all pending writes automatically in the correct order

---

## Dependencies

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Glide (image loading)
implementation("com.github.bumptech.glide:glide:4.16.0")
```

---

## Author

Built as a student project demonstrating Firebase integration with Android (Kotlin).
