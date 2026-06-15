package com.example.chatapp

import com.google.firebase.Timestamp

/**
 * Represents one document inside chats/{chatId}/messages
 *
 * NOTE: Firestore's toObject() requires a no-argument constructor and
 * public properties, which is why every property has a default value.
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: String = "text",
    val timestamp: Timestamp? = null,
    val status: String = "sent"
)
