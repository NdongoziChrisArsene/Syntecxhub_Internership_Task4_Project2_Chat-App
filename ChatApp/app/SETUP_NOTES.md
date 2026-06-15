# Auth Setup Notes

## 1. Add dependencies to app/build.gradle (Module: app)

Inside the `dependencies { ... }` block, add:

```gradle
// Firebase BoM keeps all Firebase library versions compatible with each other
implementation platform('com.google.firebase:firebase-bom:33.1.0')

implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
```

Then sync Gradle (File > Sync Project with Gradle Files).

## 2. Register the activities in AndroidManifest.xml

Inside the `<application> ... </application>` tags, add:

```xml
<activity android:name=".LoginActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity android:name=".SignupActivity" />

<activity android:name=".MainActivity" />

<activity android:name=".ChatActivity" />
```

Important: LoginActivity should be the launcher activity (the one with the
MAIN/LAUNCHER intent filter). If your manifest currently has MainActivity
set as the launcher, remove that intent-filter from MainActivity and move
it to LoginActivity as shown above.

## 3. Enable Email/Password sign-in in Firebase

In the Firebase console:
1. Go to Build > Authentication > Sign-in method
2. Click "Email/Password"
3. Toggle it to Enabled, then Save

## 4. Files included in this package

- LoginActivity.kt   -> handles sign in
- SignupActivity.kt  -> handles registration + creates the Firestore user profile
- MainActivity.kt    -> placeholder screen after login (has a button to open the test chat)
- ChatActivity.kt    -> chat screen: real-time messages + sending
- Message.kt         -> data model for a single message
- MessageAdapter.kt  -> RecyclerView adapter (sent vs received bubbles)
- activity_login.xml
- activity_signup.xml
- activity_main.xml
- activity_chat.xml
- item_message_sent.xml      -> right-aligned green bubble
- item_message_received.xml  -> left-aligned white bubble
- bg_bubble_sent.xml / bg_bubble_received.xml -> bubble shapes

## 5. Quick test (auth)

1. Run the app -> you should land on the Login screen.
2. Tap "Don't have an account? Sign up" -> fill in the form -> Sign Up.
3. You should be taken to MainActivity showing "Welcome, <your name>!"
4. Check Firebase console > Firestore Database > users collection -> a new
   document with your UID should exist.
5. Tap "Log Out" -> you should return to the Login screen.
6. Log back in with the same email/password -> should go straight to MainActivity.

## 6. Quick test (chat screen)

1. From MainActivity, tap "Open Test Chat".
2. Type a message and tap "Send".
3. The bubble should appear right-aligned (green) since you're the sender.
4. Check Firestore: a new document should appear under
   chats/test_chat/messages, and chats/test_chat itself should now have
   lastMessage / lastMessageTimestamp / lastMessageSenderId fields.
5. To see a "received" (left-aligned, white) bubble: open Firestore console,
   manually add a document to chats/test_chat/messages with a different
   senderId (any random string) and a text field + timestamp -> it should
   appear instantly on the left in the running app (real-time update).
6. To test ordering/offline: turn on airplane mode, send a couple of
   messages (they should appear immediately), then turn airplane mode off
   -> they should sync to Firestore without duplicating or reordering.
