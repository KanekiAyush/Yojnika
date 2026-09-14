package com.yojnika.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.yojnika.app.utils.LocaleHelper;
import com.yojnika.app.utils.LocationData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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
        
        String otherText = getString(R.string.other);
        
        if (spinnerIncomeRange.getSelectedItem() != null) {
            String incomeRange = spinnerIncomeRange.getSelectedItem().toString();
            if (!incomeRange.equals(otherText) || !etCustomIncome.getText().toString().trim().isEmpty()) completedFields++;
        }
        
        if (spinnerDistrict.getSelectedItem() != null) {
            String district = spinnerDistrict.getSelectedItem().toString();
            if (!district.equals(otherText) || !etCustomDistrict.getText().toString().trim().isEmpty()) completedFields++;
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

        // State Dropdown - Use translated array for UI
        String[] translatedStates = getResources().getStringArray(R.array.indian_states_array);
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, translatedStates);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateAdapter);

        // Get English states list for mapping (MATCHING ORDER OF translatedStates)
        List<String> englishStates = LocationData.getAllStates("Select State", "Other");

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // IMPORTANT: Use the same index to get the English name
                String selectedStateEnglish = (position >= 0 && position < englishStates.size()) ? englishStates.get(position) : "Other";
                String otherText = getString(R.string.other);
                
                // Show custom state input only if the LAST option (Other) is selected
                boolean isOther = (position == englishStates.size() - 1);
                tilCustomState.setVisibility(isOther ? View.VISIBLE : View.GONE);
                
                updateDistrictSpinner(selectedStateEnglish);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerIncomeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                String otherText = getString(R.string.other);
                tilCustomIncome.setVisibility(selected.equals(otherText) ? View.VISIBLE : View.GONE);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                String otherText = getString(R.string.other);
                tilCustomDistrict.setVisibility(selected.equals(otherText) ? View.VISIBLE : View.GONE);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                String otherText = getString(R.string.other);
                tilCustomCategory.setVisibility(selected.equals(otherText) ? View.VISIBLE : View.GONE);
                updateProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDistrictSpinner(String stateEnglish) {
        String selectStatePrompt = "Select State";
        if (stateEnglish.equals(selectStatePrompt)) {
            spinnerDistrict.setAdapter(null);
            spinnerDistrict.setEnabled(false);
            return;
        }

        spinnerDistrict.setEnabled(true);
        
        // Load districts from translated XML based on state name
        int arrayResId = getDistrictArrayResourceId(stateEnglish);
        String[] districtArray = getResources().getStringArray(arrayResId);
        
        List<String> districts = new ArrayList<>(Arrays.asList(districtArray));
        districts.add(getString(R.string.other));
        
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        // Add fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        spinnerDistrict.startAnimation(fadeIn);
    }

    private int getDistrictArrayResourceId(String stateEnglish) {
        switch (stateEnglish) {
            case "Maharashtra": return R.array.districts_maharashtra;
            case "Bihar": return R.array.districts_bihar;
            case "Uttar Pradesh": return R.array.districts_uttar_pradesh;
            case "Gujarat": return R.array.districts_gujarat;
            case "Madhya Pradesh": return R.array.districts_madhya_pradesh;
            case "Rajasthan": return R.array.districts_rajasthan;
            default: return R.array.districts_empty;
        }
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
            String otherText = getString(R.string.other);
            if (income > 0) {
                if (!setIncomeRangeSelection(income)) {
                    setSpinnerSelection(spinnerIncomeRange, otherText);
                    etCustomIncome.setText(String.valueOf(income));
                    tilCustomIncome.setVisibility(View.VISIBLE);
                }
            }

            // Set Spinners by matching with Constants English arrays
            setSpinnerByIndex(spinnerGender, Constants.GENDERS, profile.getGender());
            setSpinnerByIndex(spinnerMaritalStatus, Constants.MARITAL_STATUSES, profile.getMaritalStatus());
            setSpinnerByIndex(spinnerOccupation, Constants.OCCUPATIONS, profile.getOccupation());
            setSpinnerByIndex(spinnerEducation, Constants.EDUCATION_LEVELS, profile.getEducationLevel());
            
            if (!setSpinnerByIndex(spinnerCategory, Constants.CATEGORIES, profile.getCategory())) {
                setSpinnerSelection(spinnerCategory, otherText);
                etCustomCategory.setText(profile.getCategory());
                tilCustomCategory.setVisibility(View.VISIBLE);
            }

            // State and District
            if (profile.getState() != null && !profile.getState().isEmpty()) {
                String stateEng = profile.getState();
                int stateIdx = -1;
                for (int i = 0; i < LocationData.STATE_ORDER.length; i++) {
                    if (stateEng.equalsIgnoreCase(LocationData.STATE_ORDER[i])) {
                        stateIdx = i + 1; // +1 because of "Select State" at index 0
                        break;
                    }
                }
                
                if (stateIdx != -1) {
                    spinnerState.setSelection(stateIdx);
                    updateDistrictSpinner(stateEng);
                    
                    // Set District
                    String districtSaved = profile.getDistrict();
                    if (!setDistrictSpinnerSelection(stateEng, districtSaved)) {
                        setSpinnerSelection(spinnerDistrict, otherText);
                        etCustomDistrict.setText(districtSaved);
                        tilCustomDistrict.setVisibility(View.VISIBLE);
                    }
                } else {
                    setSpinnerSelection(spinnerState, otherText);
                    etCustomState.setText(stateEng);
                    tilCustomState.setVisibility(View.VISIBLE);
                    updateDistrictSpinner("Other");
                }
            }
        }
        updateProgress();
    }

    private boolean setDistrictSpinnerSelection(String stateEnglish, String savedDistrictEnglish) {
        int arrayResId = getDistrictArrayResourceId(stateEnglish);
        // Load the ENGLISH version of districts to match with saved data
        // We use a themed context or forced locale to get English version
        String[] englishDistricts = getEnglishStringArray(arrayResId);
        
        for (int i = 0; i < englishDistricts.length; i++) {
            if (savedDistrictEnglish.equalsIgnoreCase(englishDistricts[i])) {
                spinnerDistrict.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private String[] getEnglishStringArray(int resId) {
        Configuration conf = new Configuration(getResources().getConfiguration());
        conf.setLocale(new Locale("en"));
        Context englishContext = createConfigurationContext(conf);
        return englishContext.getResources().getStringArray(resId);
    }

    private boolean setSpinnerByIndex(Spinner spinner, String[] englishArray, String savedValue) {
        if (savedValue == null) return false;
        for (int i = 0; i < englishArray.length; i++) {
            if (savedValue.equalsIgnoreCase(englishArray[i])) {
                spinner.setSelection(i);
                return true;
            }
        }
        return false;
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
        if (range.equals("₹0 – ₹15,000")) return 15000;
        if (range.equals("₹15,000 – ₹30,000")) return 30000;
        if (range.equals("₹30,000 – ₹50,000")) return 50000;
        if (range.equals("₹50,000 – ₹1 Lakh")) return 100000;
        if (range.equals("₹1 Lakh – ₹2 Lakh")) return 200000;
        if (range.equals("₹2 Lakh – ₹3 Lakh")) return 300000;
        if (range.equals("₹3 Lakh – ₹5 Lakh")) return 500000;
        if (range.equals("₹5 Lakh – ₹8 Lakh")) return 800000;
        return 0;
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
                tilAge.setError(getString(R.string.error_age_range));
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
        String otherText = getString(R.string.other);
        if (selectedIncomeRange.equals(otherText)) {
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
        int stateIdx = spinnerState.getSelectedItemPosition();
        List<String> englishStates = LocationData.getAllStates("Select State", "Other");
        String selectedStateEnglish = (stateIdx >= 0 && stateIdx < englishStates.size()) ? englishStates.get(stateIdx) : "Other";

        if (selectedStateEnglish.equals("Select State")) {
            Toast.makeText(this, getString(R.string.error_state_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStateEnglish.equals("Other")) {
            state = etCustomState.getText().toString().trim();
            if (state.isEmpty()) {
                tilCustomState.setError(getString(R.string.error_state_required));
                etCustomState.requestFocus();
                return;
            }
        } else {
            state = selectedStateEnglish;
        }
        tilCustomState.setError(null);

        String district;
        int distIdx = spinnerDistrict.getSelectedItemPosition();
        String selectedDistrictText = spinnerDistrict.getSelectedItem().toString();
        
        if (selectedDistrictText.equals(otherText)) {
            district = etCustomDistrict.getText().toString().trim();
            if (district.isEmpty()) {
                tilCustomDistrict.setError(getString(R.string.error_district_required));
                etCustomDistrict.requestFocus();
                return;
            }
        } else {
            // Map selected index to ENGLISH district name
            int arrayResId = getDistrictArrayResourceId(state);
            String[] englishDistricts = getEnglishStringArray(arrayResId);
            district = (distIdx >= 0 && distIdx < englishDistricts.length) ? englishDistricts[distIdx] : selectedDistrictText;
        }
        tilCustomDistrict.setError(null);

        // Get English values from Constants using the selected index
        int genderIdx = spinnerGender.getSelectedItemPosition();
        String gender = (genderIdx >= 0 && genderIdx < Constants.GENDERS.length) ? Constants.GENDERS[genderIdx] : "Other";

        int maritalIdx = spinnerMaritalStatus.getSelectedItemPosition();
        String maritalStatus = (maritalIdx >= 0 && maritalIdx < Constants.MARITAL_STATUSES.length) ? Constants.MARITAL_STATUSES[maritalIdx] : "Unmarried";

        int occIdx = spinnerOccupation.getSelectedItemPosition();
        String occupation = (occIdx >= 0 && occIdx < Constants.OCCUPATIONS.length) ? Constants.OCCUPATIONS[occIdx] : "Other";

        int eduIdx = spinnerEducation.getSelectedItemPosition();
        String education = (eduIdx >= 0 && eduIdx < Constants.EDUCATION_LEVELS.length) ? Constants.EDUCATION_LEVELS[eduIdx] : "Graduate";
        
        String category;
        int catIdx = spinnerCategory.getSelectedItemPosition();
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        if (selectedCategory.equals(otherText)) {
            category = etCustomCategory.getText().toString().trim();
            if (category.isEmpty()) {
                tilCustomCategory.setError(getString(R.string.error_category_required));
                etCustomCategory.requestFocus();
                return;
            }
        } else {
            category = (catIdx >= 0 && catIdx < Constants.CATEGORIES.length) ? Constants.CATEGORIES[catIdx] : selectedCategory;
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
