package com.yojnika.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.yojnika.app.R;
import com.yojnika.app.models.CriteriaCheckResult;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.EligibilityChecker;

public class EligibilityCheckActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvCheckSchemeName;
    private TextView tvApplicantName;
    private MaterialCardView cardResult;
    private ImageView ivResultIcon;
    private TextView tvOverallStatus;
    private TextView tvResultScore;
    private LinearLayout llCriteriaContainer;
    private MaterialButton btnEditProfileForEligibility;

    private SchemeRepository repository;
    private int schemeId = -1;
    private Scheme currentScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eligibility_check);

        repository = SchemeRepository.getInstance(this);

        initViews();

        schemeId = getIntent().getIntExtra(Constants.EXTRA_SCHEME_ID, -1);
        if (schemeId == -1) {
            Toast.makeText(this, "Scheme not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        btnEditProfileForEligibility.setOnClickListener(v -> {
            Intent intent = new Intent(EligibilityCheckActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataAndCheck();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCheckSchemeName = findViewById(R.id.tvCheckSchemeName);
        tvApplicantName = findViewById(R.id.tvApplicantName);
        cardResult = findViewById(R.id.cardResult);
        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvOverallStatus = findViewById(R.id.tvOverallStatus);
        tvResultScore = findViewById(R.id.tvResultScore);
        llCriteriaContainer = findViewById(R.id.llCriteriaContainer);
        btnEditProfileForEligibility = findViewById(R.id.btnEditProfileForEligibility);
    }

    private void loadDataAndCheck() {
        repository.getSchemeById(schemeId, scheme -> {
            runOnUiThread(() -> {
                if (scheme == null) {
                    Toast.makeText(EligibilityCheckActivity.this, "Scheme not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentScheme = scheme;
                tvCheckSchemeName.setText(scheme.getSchemeName());

                UserProfile profile = repository.getUserProfile();
                if (profile == null || !profile.isComplete()) {
                    showProfileIncompleteState();
                } else {
                    tvApplicantName.setText("Applicant: " + profile.getFullName() + " (" + profile.getDistrict() + ", " + profile.getState() + ")");
                    evaluateEligibility(profile, scheme);
                }
            });
        });
    }

    private void showProfileIncompleteState() {
        tvApplicantName.setText("Applicant: Guest (Profile Incomplete)");
        ivResultIcon.setImageResource(R.drawable.ic_close);
        ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.score_low));
        tvOverallStatus.setText("Profile Incomplete");
        tvOverallStatus.setTextColor(ContextCompat.getColor(this, R.color.score_low));
        tvResultScore.setText("Please set up your profile to run the automated eligibility check.");
        cardResult.setCardBackgroundColor(ContextCompat.getColor(this, R.color.score_low_bg));
        cardResult.setStrokeColor(ContextCompat.getColor(this, R.color.score_low));

        llCriteriaContainer.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("You need to enter your personal details (Age, Income, Occupation, Category, District) to verify against this scheme's guidelines.");
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tv.setPadding(0, 16, 0, 16);
        llCriteriaContainer.addView(tv);
    }

    private void evaluateEligibility(UserProfile profile, Scheme scheme) {
        EligibilityChecker.EligibilityReport report = EligibilityChecker.checkEligibility(profile, scheme);

        if (report.getScore() >= 0.99f) {
            ivResultIcon.setImageResource(R.drawable.ic_check_circle);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.score_high));
            tvOverallStatus.setText(getString(R.string.status_eligible));
            tvOverallStatus.setTextColor(ContextCompat.getColor(this, R.color.score_high));
            tvResultScore.setText("Matched " + report.getMatchedCount() + " of " + report.getTotalCount() + " conditions (100% Match)");
            cardResult.setCardBackgroundColor(ContextCompat.getColor(this, R.color.score_high_bg));
            cardResult.setStrokeColor(ContextCompat.getColor(this, R.color.score_high));
        } else if (report.getScore() >= 0.50f) {
            ivResultIcon.setImageResource(R.drawable.ic_sparkles);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.score_medium));
            tvOverallStatus.setText(getString(R.string.status_partially_eligible));
            tvOverallStatus.setTextColor(ContextCompat.getColor(this, R.color.score_medium));
            tvResultScore.setText("Matched " + report.getMatchedCount() + " of " + report.getTotalCount() + " conditions (" + report.getScorePercentage() + "% Match)");
            cardResult.setCardBackgroundColor(ContextCompat.getColor(this, R.color.score_medium_bg));
            cardResult.setStrokeColor(ContextCompat.getColor(this, R.color.score_medium));
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_close);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.score_low));
            tvOverallStatus.setText(getString(R.string.status_not_eligible));
            tvOverallStatus.setTextColor(ContextCompat.getColor(this, R.color.score_low));
            tvResultScore.setText("Matched " + report.getMatchedCount() + " of " + report.getTotalCount() + " conditions (" + report.getScorePercentage() + "% Match)");
            cardResult.setCardBackgroundColor(ContextCompat.getColor(this, R.color.score_low_bg));
            cardResult.setStrokeColor(ContextCompat.getColor(this, R.color.score_low));
        }

        // Render criteria list
        llCriteriaContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CriteriaCheckResult cr : report.getCriteriaResults()) {
            View itemView = inflater.inflate(R.layout.item_criteria_check, llCriteriaContainer, false);

            TextView tvCriteriaName = itemView.findViewById(R.id.tvCriteriaName);
            TextView tvUserValue = itemView.findViewById(R.id.tvUserValue);
            TextView tvRequiredValue = itemView.findViewById(R.id.tvRequiredValue);
            TextView tvCriteriaBadge = itemView.findViewById(R.id.tvCriteriaBadge);
            ImageView ivStatusIcon = itemView.findViewById(R.id.ivCriteriaStatus);

            tvCriteriaName.setText(cr.getCriteriaName());
            tvUserValue.setText("Your Value: " + cr.getUserValue());
            tvRequiredValue.setText("Requirement: " + cr.getRequiredValue());

            if (cr.isMatched()) {
                ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                ivStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.score_high));
                tvCriteriaBadge.setText("Match");
                tvCriteriaBadge.setTextColor(ContextCompat.getColor(this, R.color.score_high));
                tvCriteriaBadge.setBackgroundResource(R.drawable.bg_score_high);
            } else {
                ivStatusIcon.setImageResource(R.drawable.ic_close);
                ivStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.score_low));
                tvCriteriaBadge.setText("Mismatch");
                tvCriteriaBadge.setTextColor(ContextCompat.getColor(this, R.color.score_low));
                tvCriteriaBadge.setBackgroundResource(R.drawable.bg_score_low);
            }

            llCriteriaContainer.addView(itemView);
        }
    }
}
