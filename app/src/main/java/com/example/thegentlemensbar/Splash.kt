package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (FirebaseRepo.currentUid == null) {
                goWelcome()
            } else {
                FirebaseRepo.loadProfile { p ->
                    if (p == null) {
                        goWelcome()
                    } else {
                        saveProfileToPrefs(this, p)
                        routeByRole(this, p.role)
                        finish()
                    }
                }
            }
        }, 3000)
    }

    private fun goWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}