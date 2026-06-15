package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Shows the messages of a single chat (chats/{chatId}/messages) in real time
 * and lets the user send new messages.
 *
 * Expects the chat ID to be passed via the intent extra "chatId".
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MessageAdapter
    private var messagesListener: ListenerRegistration? = null

    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // For now, fall back to a fixed test chat ID if none was passed in,
        // so this screen can be opened directly while building/testing.
        chatId = intent.getStringExtra("chatId") ?: "test_chat"

        val recyclerView: RecyclerView = findViewById(R.id.messagesRecyclerView)
        val messageEditText: EditText = findViewById(R.id.messageEditText)
        val sendButton: Button = findViewById(R.id.sendButton)

        val currentUserId = auth.currentUser?.uid ?: ""
        adapter = MessageAdapter(currentUserId)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                messageEditText.text.clear()
            }
        }

        listenForMessages(recyclerView, layoutManager)
    }

    /**
     * Attaches a real-time listener to chats/{chatId}/messages, ordered by
     * timestamp ascending. Every time a message is added/changed, Firestore
     * fires this listener with the full updated list, and ListAdapter's
     * DiffUtil figures out what actually changed.
     */
    private fun listenForMessages(recyclerView: RecyclerView, layoutManager: LinearLayoutManager) {
        messagesListener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load messages: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                val messages = snapshots.documents.map { doc ->
                    val message = doc.toObject(Message::class.java) ?: Message()
                    message.copy(id = doc.id)
                }

                adapter.submitList(messages) {
                    // Scroll to the bottom once the new list has been laid out,
                    // so the most recent message is always visible.
                    if (messages.isNotEmpty()) {
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }

    /**
     * Writes a new message document and updates the parent chat document's
     * lastMessage / lastMessageTimestamp fields (used by the chat list screen).
     */
    private fun sendMessage(text: String) {
        val senderId = auth.currentUser?.uid ?: return

        val message = hashMapOf(
            "senderId" to senderId,
            "text" to text,
            "type" to "text",
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "sent"
        )

        val chatDocRef = firestore.collection("chats").document(chatId)

        chatDocRef.collection("messages")
            .add(message)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Update the chat summary so the chat list can show a preview
        // without re-reading every message.
        val chatUpdate = hashMapOf(
            "lastMessage" to text,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "lastMessageSenderId" to senderId
        )
        chatDocRef.set(chatUpdate, com.google.firebase.firestore.SetOptions.merge())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Always detach Firestore listeners when the screen is destroyed
        // to avoid memory leaks and unnecessary background reads.
        messagesListener?.remove()
    }
}
