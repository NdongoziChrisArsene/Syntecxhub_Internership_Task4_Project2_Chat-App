package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val welcomeText: TextView = findViewById(R.id.welcomeText)
        val logoutButton: Button = findViewById(R.id.logoutButton)
        val openTestChatButton: Button = findViewById(R.id.openTestChatButton)

        openTestChatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", "test_chat")
            startActivity(intent)
        }

        val userId = auth.currentUser?.uid
        welcomeText.text = "Logged in as: ${auth.currentUser?.email}"

        // Quick check that the Firestore profile was created correctly during signup.
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (name != null) {
                        welcomeText.text = "Welcome, $name!"
                    }
                }
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
