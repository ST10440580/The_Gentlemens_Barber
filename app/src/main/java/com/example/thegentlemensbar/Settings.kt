package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupUI()
    }

    private fun setupUI() {
        val nameInput = findViewById<EditText>(R.id.settingsName)
        val emailInput = findViewById<EditText>(R.id.settingsEmail)
        val newPasswordInput = findViewById<EditText>(R.id.settingsNewPassword)

        emailInput?.setText(FirebaseRepo.currentUserEmail ?: "")
        FirebaseRepo.loadProfile { profile ->
            profile?.let { nameInput?.setText(it.fullName) }
        }

        findViewById<Button>(R.id.updateProfileBtn)?.setOnClickListener {
            val newName = nameInput?.text.toString().trim()
            if (newName.isNotEmpty()) {
                FirebaseRepo.updateDisplayName(newName) { success ->
                    if (success) {
                        getSharedPreferences("UserPref", MODE_PRIVATE).edit()
                            .putString("USERNAME", newName).apply()
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.changePasswordBtn)?.setOnClickListener {
            val newPass = newPasswordInput?.text.toString().trim()
            if (newPass.length >= 6) {
                FirebaseRepo.updatePassword(newPass) { success, error ->
                    if (success) {
                        newPasswordInput?.text?.clear()
                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } else if (newPass.isNotEmpty()) {
                Toast.makeText(this, "Min 6 characters required", Toast.LENGTH_SHORT).show()
            }
        }

        // Feature Links
        findViewById<TextView>(R.id.savedCardsBtn)?.setOnClickListener {
            startActivity(Intent(this, SavedCardsActivity::class.java))
        }
        
        findViewById<TextView>(R.id.aboutBtn)?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        
        findViewById<TextView>(R.id.contactBtn)?.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }

        // Placeholders for remaining features
        findViewById<TextView>(R.id.membershipStatusBtn)?.setOnClickListener { showPlaceholder("Membership") }
        findViewById<ImageButton>(R.id.editAvatarBtn)?.setOnClickListener { showPlaceholder("Avatar Upload") }

        findViewById<Button>(R.id.settingsLogoutBtn)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out") { _, _ ->
                    FirebaseRepo.logout()
                    getSharedPreferences("UserPref", MODE_PRIVATE).edit().clear().apply()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showPlaceholder(feature: String) {
        Toast.makeText(this, "$feature feature coming soon!", Toast.LENGTH_SHORT).show()
    }
}
