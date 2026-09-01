package com.yojnika.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.yojnika.app.R;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilFullName, tilAge, tilIncome, tilDistrict;
    private EditText etFullName, etAge, etIncome, etDistrict;
    private Spinner spinnerGender, spinnerMaritalStatus, spinnerOccupation, spinnerEducation, spinnerCategory, spinnerState;
    private MaterialButton btnSaveProfile;

    private SchemeRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = SchemeRepository.getInstance(this);

        initViews();
        setupSpinners();
        loadExistingProfile();

        toolbar.setNavigationOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilFullName = findViewById(R.id.tilFullName);
        tilAge = findViewById(R.id.tilAge);
        tilIncome = findViewById(R.id.tilIncome);
        tilDistrict = findViewById(R.id.tilDistrict);

        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etIncome = findViewById(R.id.etIncome);
        etDistrict = findViewById(R.id.etDistrict);

        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerMaritalStatus = findViewById(R.id.spinnerMaritalStatus);
        spinnerOccupation = findViewById(R.id.spinnerOccupation);
        spinnerEducation = findViewById(R.id.spinnerEducation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerState = findViewById(R.id.spinnerState);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupSpinners() {
        setSpinnerAdapter(spinnerGender, R.array.gender_array);
        setSpinnerAdapter(spinnerMaritalStatus, R.array.marital_status_array);
        setSpinnerAdapter(spinnerOccupation, R.array.occupation_array);
        setSpinnerAdapter(spinnerEducation, R.array.education_array);
        setSpinnerAdapter(spinnerCategory, R.array.category_array);
        setSpinnerAdapter(spinnerState, R.array.indian_states_array);
    }

    private void setSpinnerAdapter(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadExistingProfile() {
        UserProfile profile = repository.getUserProfile();
        if (profile != null) {
            etFullName.setText(profile.getFullName());
            if (profile.getAge() > 0) {
                etAge.setText(String.valueOf(profile.getAge()));
            }
            if (profile.getAnnualIncome() > 0) {
                etIncome.setText(String.valueOf(profile.getAnnualIncome()));
            }
            etDistrict.setText(profile.getDistrict());

            setSpinnerSelection(spinnerGender, profile.getGender());
            setSpinnerSelection(spinnerMaritalStatus, profile.getMaritalStatus());
            setSpinnerSelection(spinnerOccupation, profile.getOccupation());
            setSpinnerSelection(spinnerEducation, profile.getEducationLevel());
            setSpinnerSelection(spinnerCategory, profile.getCategory());
            setSpinnerSelection(spinnerState, profile.getState());
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equalsIgnoreCase(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String incomeStr = etIncome.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_required));
            etFullName.requestFocus();
            return;
        } else {
            tilFullName.setError(null);
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 18 || age > 100) {
                tilAge.setError(getString(R.string.error_age_invalid));
                etAge.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            tilAge.setError(getString(R.string.error_age_invalid));
            etAge.requestFocus();
            return;
        }
        tilAge.setError(null);

        long income = 0;
        if (!incomeStr.isEmpty()) {
            try {
                income = Long.parseLong(incomeStr);
            } catch (NumberFormatException e) {
                tilIncome.setError(getString(R.string.error_income_invalid));
                etIncome.requestFocus();
                return;
            }
        }
        tilIncome.setError(null);

        if (district.isEmpty()) {
            tilDistrict.setError(getString(R.string.error_district_required));
            etDistrict.requestFocus();
            return;
        } else {
            tilDistrict.setError(null);
        }

        String gender = spinnerGender.getSelectedItem().toString();
        String maritalStatus = spinnerMaritalStatus.getSelectedItem().toString();
        String occupation = spinnerOccupation.getSelectedItem().toString();
        String education = spinnerEducation.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String state = spinnerState.getSelectedItem().toString();

        UserProfile profile = new UserProfile(
                fullName, age, gender, income,
                occupation, education, category,
                state, district, maritalStatus
        );

        repository.saveUserProfile(profile);

        Toast.makeText(this, R.string.profile_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
