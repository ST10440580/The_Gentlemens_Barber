package com.example.thegentlemensbar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginBtn.isEnabled = false
            FirebaseRepo.login(email, password) { ok, profile, err ->
                loginBtn.isEnabled = true
                if (ok && profile != null) {
                    saveProfileToPrefs(this, profile)
                    if (profile.role == "admin") Seeder.seedIfEmpty()
                    routeByRole(this, profile.role)
                    finish()
                } else {
                    Toast.makeText(this, err ?: "Login failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}