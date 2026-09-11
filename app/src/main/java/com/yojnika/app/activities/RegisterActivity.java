package com.yojnika.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yojnika.app.R;
import com.yojnika.app.utils.SharedPrefsManager;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.tilRegName);
        tilEmail = findViewById(R.id.tilRegEmail);
        tilPhone = findViewById(R.id.tilRegPhone);
        tilPassword = findViewById(R.id.tilRegPassword);
        tilConfirmPassword = findViewById(R.id.tilRegConfirmPassword);

        etName = findViewById(R.id.etRegName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            isValid = false;
        } else {
            tilName.setError(null);
        }

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_email_empty));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (phone.isEmpty()) {
            tilPhone.setError(getString(R.string.phone_number) + " is required");
            isValid = false;
        } else if (phone.length() != 10) {
            tilPhone.setError(getString(R.string.error_mobile_invalid));
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_empty));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_password_empty));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (isValid) {
            SharedPrefsManager.getInstance(this).registerUser(email, phone, password, name);
            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
