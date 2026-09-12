package com.yojnika.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.TextView;
import com.yojnika.app.R;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.SharedPrefsManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String input = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (input.isEmpty()) {
            tilEmail.setError(getString(R.string.email_or_phone) + " is required");
            return;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_empty));
            return;
        } else {
            tilPassword.setError(null);
        }

        SharedPrefsManager prefs = SharedPrefsManager.getInstance(this);
        String regEmail = prefs.getRegisteredEmail();
        String regPhone = prefs.getRegisteredPhone();
        String regPassword = prefs.getRegisteredPassword();

        // Check against Registered Credentials (Email OR Phone) or Demo Credentials
        boolean isRegisteredMatch = (!regEmail.isEmpty() && input.equals(regEmail) && password.equals(regPassword)) 
                                || (!regPhone.isEmpty() && input.equals(regPhone) && password.equals(regPassword));
        
        boolean isDemoMatch = (input.equals("user@yojnika.com") && password.equals("password123"));

        if (isRegisteredMatch || isDemoMatch) {
            prefs.setLoggedIn(true);
            
            Intent intent;
            if (!prefs.isProfileComplete()) {
                // Profile not complete, go to Setup
                intent = new Intent(LoginActivity.this, ProfileActivity.class);
                intent.putExtra(Constants.EXTRA_IS_SETUP_MODE, true);
            } else {
                // Profile complete, go to Home
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
        }
    }
}
