package com.yojnika.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.yojnika.app.R;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.SharedPrefsManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefsManager prefs = SharedPrefsManager.getInstance(this);
        prefs.applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pre-initialize repository and local SQLite database
        SchemeRepository repository = SchemeRepository.getInstance(this);
        repository.getAllSchemes(schemes -> {
            // DB seeded and warm
        });

        if (prefs.hasUserProfile() && prefs.getLoggedInUserId() == -1) {
            // One-time migration for legacy user from SharedPreferences to SQLite
            UserProfile profile = prefs.getUserProfile();
            String email = prefs.getRegisteredEmail();
            String phone = prefs.getRegisteredPhone();
            String pass = prefs.getRegisteredPassword();
            
            if (pass.isEmpty()) pass = "password123"; // Fallback for old demo users

            repository.migrateLegacyData(profile, email, phone, pass, userId -> {
                if (userId != -1) {
                    prefs.setLoggedIn(userId);
                }
            });
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (prefs.isLoggedIn()) {
                if (!prefs.isProfileComplete()) {
                    // Logged in but profile incomplete
                    intent = new Intent(SplashActivity.this, ProfileActivity.class);
                    intent.putExtra(Constants.EXTRA_IS_SETUP_MODE, true);
                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
