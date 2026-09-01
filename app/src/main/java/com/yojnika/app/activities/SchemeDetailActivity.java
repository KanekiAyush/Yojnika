package com.yojnika.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.yojnika.app.R;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;

public class SchemeDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView btnBookmark;
    private TextView tvSchemeTypeTag;
    private TextView tvCategoryTag;
    private TextView tvSchemeName;
    private TextView tvSchemeDescription;
    private TextView tvBenefits;
    private TextView tvCriteriaAge;
    private TextView tvCriteriaGender;
    private TextView tvCriteriaIncome;
    private TextView tvCriteriaOccupation;
    private TextView tvCriteriaEducation;
    private TextView tvCriteriaState;
    private TextView tvApplicationProcess;
    private MaterialButton btnCheckEligibility;
    private MaterialButton btnOpenPortal;

    private SchemeRepository repository;
    private Scheme currentScheme;
    private int schemeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_detail);

        repository = SchemeRepository.getInstance(this);

        initViews();

        schemeId = getIntent().getIntExtra(Constants.EXTRA_SCHEME_ID, -1);
        if (schemeId != -1) {
            loadSchemeDetails(schemeId);
        } else {
            Toast.makeText(this, "Scheme not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        btnBookmark.setOnClickListener(v -> toggleBookmark());

        btnCheckEligibility.setOnClickListener(v -> {
            Intent intent = new Intent(SchemeDetailActivity.this, EligibilityCheckActivity.class);
            intent.putExtra(Constants.EXTRA_SCHEME_ID, schemeId);
            startActivity(intent);
        });

        btnOpenPortal.setOnClickListener(v -> {
            if (currentScheme != null && currentScheme.getOfficialWebsite() != null && !currentScheme.getOfficialWebsite().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentScheme.getOfficialWebsite()));
                startActivity(browserIntent);
            } else {
                Toast.makeText(SchemeDetailActivity.this, "Official portal link not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnBookmark = findViewById(R.id.btnBookmark);
        tvSchemeTypeTag = findViewById(R.id.tvSchemeTypeTag);
        tvCategoryTag = findViewById(R.id.tvCategoryTag);
        tvSchemeName = findViewById(R.id.tvSchemeName);
        tvSchemeDescription = findViewById(R.id.tvSchemeDescription);
        tvBenefits = findViewById(R.id.tvBenefits);
        tvCriteriaAge = findViewById(R.id.tvCriteriaAge);
        tvCriteriaGender = findViewById(R.id.tvCriteriaGender);
        tvCriteriaIncome = findViewById(R.id.tvCriteriaIncome);
        tvCriteriaOccupation = findViewById(R.id.tvCriteriaOccupation);
        tvCriteriaEducation = findViewById(R.id.tvCriteriaEducation);
        tvCriteriaState = findViewById(R.id.tvCriteriaState);
        tvApplicationProcess = findViewById(R.id.tvApplicationProcess);
        btnCheckEligibility = findViewById(R.id.btnCheckEligibility);
        btnOpenPortal = findViewById(R.id.btnOpenPortal);
    }

    private void loadSchemeDetails(int id) {
        repository.getSchemeById(id, scheme -> {
            runOnUiThread(() -> {
                if (scheme == null) {
                    Toast.makeText(SchemeDetailActivity.this, "Scheme details unavailable", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                currentScheme = scheme;
                bindData(scheme);
            });
        });
    }

    private void bindData(Scheme scheme) {
        tvSchemeName.setText(scheme.getSchemeName());
        tvSchemeTypeTag.setText(scheme.getSchemeType());
        tvSchemeDescription.setText(scheme.getSchemeDescription());
        tvBenefits.setText(scheme.getBenefits());
        tvApplicationProcess.setText(scheme.getApplicationProcess());

        // Category Tag
        String occ = scheme.getEligibleOccupations();
        if (occ != null && occ.contains("Farmer")) {
            tvCategoryTag.setText("Agriculture / Farmer");
        } else if (occ != null && occ.contains("Student")) {
            tvCategoryTag.setText("Education / Youth");
        } else if (occ != null && occ.contains("Business")) {
            tvCategoryTag.setText("Entrepreneurship");
        } else {
            tvCategoryTag.setText("Citizen Welfare");
        }

        // Criteria Age
        if (scheme.getMinAge() != null && scheme.getMaxAge() != null) {
            tvCriteriaAge.setText("• Age Range: " + scheme.getMinAge() + " - " + scheme.getMaxAge() + " years");
        } else if (scheme.getMinAge() != null) {
            tvCriteriaAge.setText("• Minimum Age: " + scheme.getMinAge() + " years");
        } else {
            tvCriteriaAge.setText("• Age Range: All age groups");
        }

        // Criteria Gender
        tvCriteriaGender.setText("• Eligible Gender: " + scheme.getGenderEligible());

        // Criteria Income
        if (scheme.getIncomeLimit() != null && scheme.getIncomeLimit() > 0) {
            tvCriteriaIncome.setText("• Income Limit: Up to ₹ " + String.format("%,d", scheme.getIncomeLimit()) + " per year");
        } else {
            tvCriteriaIncome.setText("• Income Limit: No upper income ceiling");
        }

        // Criteria Occupations
        String cleanOcc = scheme.getEligibleOccupations().replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
        tvCriteriaOccupation.setText("• Occupations: " + cleanOcc);

        // Criteria Education
        String edu = scheme.getMinEducationLevel() != null ? scheme.getMinEducationLevel() : "None";
        tvCriteriaEducation.setText("• Education: " + edu);

        // Criteria State
        String cleanState = scheme.getEligibleStates().replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
        tvCriteriaState.setText("• Eligible States: " + cleanState);

        updateBookmarkIcon(scheme.isBookmarked());
    }

    private void updateBookmarkIcon(boolean isBookmarked) {
        if (isBookmarked) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
            btnBookmark.setColorFilter(getColor(R.color.white));
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
            btnBookmark.setColorFilter(getColor(R.color.white));
        }
    }

    private void toggleBookmark() {
        if (currentScheme == null) return;
        repository.toggleBookmark(currentScheme.getSchemeId(), isBookmarked -> {
            runOnUiThread(() -> {
                currentScheme.setBookmarked(isBookmarked);
                updateBookmarkIcon(isBookmarked);
                Toast.makeText(
                        SchemeDetailActivity.this,
                        isBookmarked ? R.string.scheme_saved : R.string.scheme_removed,
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }
}
