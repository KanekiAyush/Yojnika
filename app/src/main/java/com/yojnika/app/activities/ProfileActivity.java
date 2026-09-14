package com.yojnika.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.yojnika.app.R;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.LocationData;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilFullName, tilAge, tilCustomIncome, tilCustomDistrict, tilCustomState, tilCustomCategory;
    private EditText etFullName, etAge, etCustomIncome, etCustomDistrict, etCustomState, etCustomCategory;
    private Spinner spinnerGender, spinnerMaritalStatus, spinnerOccupation, spinnerEducation, spinnerCategory, spinnerState, spinnerIncomeRange, spinnerDistrict;
    private MaterialButton btnSaveProfile;
    private LinearProgressIndicator progressIndicator;

    private SchemeRepository repository;
    private boolean isSetupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = SchemeRepository.getInstance(this);
        isSetupMode = getIntent().getBooleanExtra(Constants.EXTRA_IS_SETUP_MODE, false);

        initViews();
        setupSpinners();
        loadExistingProfile();

        if (isSetupMode) {
            toolbar.setNavigationIcon(null);
            toolbar.setTitle("Setup Your Profile");
        } else {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    @Override
    public void onBackPressed() {
        if (isSetupMode) {
            // Disable back button during mandatory setup
            Toast.makeText(this, "Please complete your profile to continue", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressIndicator = findViewById(R.id.progressIndicator);
        
        tilFullName = findViewById(R.id.tilFullName);
        tilAge = findViewById(R.id.tilAge);
        tilCustomIncome = findViewById(R.id.tilCustomIncome);
        tilCustomDistrict = findViewById(R.id.tilCustomDistrict);
        tilCustomState = findViewById(R.id.tilCustomState);
        tilCustomCategory = findViewById(R.id.tilCustomCategory);

        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etCustomIncome = findViewById(R.id.etCustomIncome);
        etCustomDistrict = findViewById(R.id.etCustomDistrict);
        etCustomState = findViewById(R.id.etCustomState);
        etCustomCategory = findViewById(R.id.etCustomCategory);

        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerMaritalStatus = findViewById(R.id.spinnerMaritalStatus);
        spinnerOccupation = findViewById(R.id.spinnerOccupation);
        spinnerEducation = findViewById(R.id.spinnerEducation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerState = findViewById(R.id.spinnerState);
        spinnerIncomeRange = findViewById(R.id.spinnerIncomeRange);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        setupProgressTrackers();
    }

    private void setupProgressTrackers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateProgress();
            }
        };

        etFullName.addTextChangedListener(watcher);
        etAge.addTextChangedListener(watcher);
        etCustomIncome.addTextChangedListener(watcher);
        etCustomDistrict.addTextChangedListener(watcher);
        etCustomState.addTextChangedListener(watcher);
        etCustomCategory.addTextChangedListener(watcher);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateProgress();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerGender.setOnItemSelectedListener(listener);
        spinnerMaritalStatus.setOnItemSelectedListener(listener);
        spinnerOccupation.setOnItemSelectedListener(listener);
        spinnerEducation.setOnItemSelectedListener(listener);
        spinnerCategory.setOnItemSelectedListener(listener);
        spinnerState.setOnItemSelectedListener(listener);
        spinnerIncomeRange.setOnItemSelectedListener(listener);
        spinnerDistrict.setOnItemSelectedListener(listener);
    }

    private void updateProgress() {
        int totalFields = 10;
        int completedFields = 0;

        if (!etFullName.getText().toString().trim().isEmpty()) completedFields++;
        if (!etAge.getText().toString().trim().isEmpty()) completedFields++;
        
        if (spinnerGender.getSelectedItemPosition() >= 0) completedFields++;
        if (spinnerMaritalStatus.getSelectedItemPosition() >= 0) completedFields++;
        if (spinnerOccupation.getSelectedItemPosition() >= 0) completedFields++;
        if (spinnerEducation.getSelectedItemPosition() >= 0) completedFields++;
        if (spinnerCategory.getSelectedItemPosition() >= 0) completedFields++;
        
        if (spinnerState.getSelectedItemPosition() > 0) completedFields++; // > 0 to skip "Select State"
        
        String incomeRange = spinnerIncomeRange.getSelectedItem().toString();
        if (!incomeRange.equals("Other") || !etCustomIncome.getText().toString().trim().isEmpty()) completedFields++;
        
        if (spinnerDistrict.getSelectedItem() != null) {
            String district = spinnerDistrict.getSelectedItem().toString();
            if (!district.equals("Other") || !etCustomDistrict.getText().toString().trim().isEmpty()) completedFields++;
        }

        int progress = (completedFields * 100) / totalFields;
        progressIndicator.setProgress(progress, true);
    }

    private void setupSpinners() {
        setSpinnerAdapter(spinnerGender, R.array.gender_array);
        setSpinnerAdapter(spinnerMaritalStatus, R.array.marital_status_array);
        setSpinnerAdapter(spinnerOccupation, R.array.occupation_array);
        setSpinnerAdapter(spinnerEducation, R.array.education_array);
        setSpinnerAdapter(spinnerCategory, R.array.category_array);
        setSpinnerAdapter(spinnerIncomeRange, R.array.income_range_array);

        // State Dropdown
        List<String> states = LocationData.getAllStates();
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateAdapter);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states.get(position);
                tilCustomState.setVisibility(selectedState.equals("Other") ? View.VISIBLE : View.GONE);
                updateDistrictSpinner(selectedState);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerIncomeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                tilCustomIncome.setVisibility(selected.equals("Other") ? View.VISIBLE : View.GONE);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                tilCustomDistrict.setVisibility(selected.equals("Other") ? View.VISIBLE : View.GONE);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                tilCustomCategory.setVisibility(selected.equals("Other") ? View.VISIBLE : View.GONE);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDistrictSpinner(String state) {
        if (state.equals("Select State")) {
            spinnerDistrict.setAdapter(null);
            spinnerDistrict.setEnabled(false);
            return;
        }

        spinnerDistrict.setEnabled(true);
        List<String> districts = LocationData.getDistrictsForState(state);
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        // Add fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        spinnerDistrict.startAnimation(fadeIn);
    }

    private void setSpinnerAdapter(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadExistingProfile() {
        if (isSetupMode) return; // Don't pre-fill if it's the mandatory setup

        UserProfile profile = repository.getUserProfile();
        if (profile != null) {
            etFullName.setText(profile.getFullName());
            if (profile.getAge() > 0) {
                etAge.setText(String.valueOf(profile.getAge()));
            }

            // Handle Income Range
            long income = profile.getAnnualIncome();
            if (income > 0) {
                if (!setIncomeRangeSelection(income)) {
                    setSpinnerSelection(spinnerIncomeRange, "Other");
                    etCustomIncome.setText(String.valueOf(income));
                    tilCustomIncome.setVisibility(View.VISIBLE);
                }
            }

            setSpinnerSelection(spinnerGender, profile.getGender());
            setSpinnerSelection(spinnerMaritalStatus, profile.getMaritalStatus());
            setSpinnerSelection(spinnerOccupation, profile.getOccupation());
            setSpinnerSelection(spinnerEducation, profile.getEducationLevel());
            
            if (!setSpinnerSelection(spinnerCategory, profile.getCategory())) {
                setSpinnerSelection(spinnerCategory, "Other");
                etCustomCategory.setText(profile.getCategory());
                tilCustomCategory.setVisibility(View.VISIBLE);
            }

            // State and District
            if (profile.getState() != null && !profile.getState().isEmpty()) {
                if (!setSpinnerSelection(spinnerState, profile.getState())) {
                    setSpinnerSelection(spinnerState, "Other");
                    etCustomState.setText(profile.getState());
                    tilCustomState.setVisibility(View.VISIBLE);
                    updateDistrictSpinner("Other");
                } else {
                    updateDistrictSpinner(profile.getState());
                }

                if (!setSpinnerSelection(spinnerDistrict, profile.getDistrict())) {
                    setSpinnerSelection(spinnerDistrict, "Other");
                    etCustomDistrict.setText(profile.getDistrict());
                    tilCustomDistrict.setVisibility(View.VISIBLE);
                }
            }
            updateProgress();
        }
    }

    private boolean setIncomeRangeSelection(long income) {
        String[] ranges = getResources().getStringArray(R.array.income_range_array);
        int index = -1;

        if (income == 15000) index = 0;
        else if (income == 30000) index = 1;
        else if (income == 50000) index = 2;
        else if (income == 100000) index = 3;
        else if (income == 200000) index = 4;
        else if (income == 300000) index = 5;
        else if (income == 500000) index = 6;
        else if (income == 800000) index = 7;

        if (index != -1 && index < ranges.length) {
            spinnerIncomeRange.setSelection(index);
            return true;
        }
        return false;
    }

    private long parseIncomeRange(String range) {
        switch (range) {
            case "₹0 – ₹15,000": return 15000;
            case "₹15,000 – ₹30,000": return 30000;
            case "₹30,000 – ₹50,000": return 50000;
            case "₹50,000 – ₹1 Lakh": return 100000;
            case "₹1 Lakh – ₹2 Lakh": return 200000;
            case "₹2 Lakh – ₹3 Lakh": return 300000;
            case "₹3 Lakh – ₹5 Lakh": return 500000;
            case "₹5 Lakh – ₹8 Lakh": return 800000;
            default: return 0;
        }
    }

    private boolean setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return false;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return false;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equalsIgnoreCase(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();

        // Validation - Full Name (Letters and Spaces only)
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_required));
            etFullName.requestFocus();
            return;
        } else if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            tilFullName.setError(getString(R.string.error_name_invalid));
            etFullName.requestFocus();
            return;
        } else {
            tilFullName.setError(null);
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 1 || age > 100) {
                tilAge.setError("Age must be between 1 and 100");
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
        String selectedIncomeRange = spinnerIncomeRange.getSelectedItem().toString();
        if (selectedIncomeRange.equals("Other")) {
            String incomeStr = etCustomIncome.getText().toString().trim();
            if (!incomeStr.isEmpty()) {
                try {
                    income = Long.parseLong(incomeStr);
                } catch (NumberFormatException e) {
                    tilCustomIncome.setError(getString(R.string.error_income_invalid));
                    etCustomIncome.requestFocus();
                    return;
                }
            }
        } else {
            income = parseIncomeRange(selectedIncomeRange);
        }
        tilCustomIncome.setError(null);

        String state;
        String selectedState = spinnerState.getSelectedItem().toString();
        if (selectedState.equals("Select State")) {
            Toast.makeText(this, "Please select your state", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedState.equals("Other")) {
            state = etCustomState.getText().toString().trim();
            if (state.isEmpty()) {
                tilCustomState.setError("State is required");
                etCustomState.requestFocus();
                return;
            }
        } else {
            state = selectedState;
        }
        tilCustomState.setError(null);

        String district;
        String selectedDistrict = spinnerDistrict.getSelectedItem().toString();
        if (selectedDistrict.equals("Other")) {
            district = etCustomDistrict.getText().toString().trim();
            if (district.isEmpty()) {
                tilCustomDistrict.setError(getString(R.string.error_district_required));
                etCustomDistrict.requestFocus();
                return;
            }
        } else {
            district = selectedDistrict;
        }
        tilCustomDistrict.setError(null);

        String gender = spinnerGender.getSelectedItem().toString();
        String maritalStatus = spinnerMaritalStatus.getSelectedItem().toString();
        String occupation = spinnerOccupation.getSelectedItem().toString();
        String education = spinnerEducation.getSelectedItem().toString();
        
        String category;
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        if (selectedCategory.equals("Other")) {
            category = etCustomCategory.getText().toString().trim();
            if (category.isEmpty()) {
                tilCustomCategory.setError("Category is required");
                etCustomCategory.requestFocus();
                return;
            }
        } else {
            category = selectedCategory;
        }
        tilCustomCategory.setError(null);

        UserProfile profile = new UserProfile(
                fullName, age, gender, income,
                occupation, education, category,
                state, district, maritalStatus
        );

        repository.saveUserProfile(profile);

        Toast.makeText(this, R.string.profile_saved_success, Toast.LENGTH_SHORT).show();
        
        if (isSetupMode) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }
}
