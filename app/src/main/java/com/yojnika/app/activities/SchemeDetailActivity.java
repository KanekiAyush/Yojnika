package com.yojnika.app.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.yojnika.app.R;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.services.TranslationManager;
import com.yojnika.app.services.TranslationService;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.SharedPrefsManager;

public class SchemeDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView btnBookmark, btnTranslate;
    private ProgressBar pbTranslate;
    private TextView tvTranslationNotice;
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
    private TranslationService translationService;
    private SharedPrefsManager prefsManager;
    private Scheme currentScheme;
    private int schemeId = -1;
    private boolean isTranslated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_detail);

        // Ensure status bar color is solid primary
        getWindow().setStatusBarColor(getColor(R.color.primary));

        repository = SchemeRepository.getInstance(this);
        translationService = TranslationService.getInstance();
        prefsManager = SharedPrefsManager.getInstance(this);

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
        btnTranslate.setOnClickListener(v -> checkAndTranslate());

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
        btnTranslate = findViewById(R.id.btnTranslate);
        pbTranslate = findViewById(R.id.pbTranslate);
        tvTranslationNotice = findViewById(R.id.tvTranslationNotice);
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
                checkLanguageAndNotify();
            });
        });
    }

    private void checkLanguageAndNotify() {
        String lang = prefsManager.getContentLanguage();
        if (!"en".equalsIgnoreCase(lang)) {
            btnTranslate.setVisibility(View.VISIBLE);
            
            translationService.isModelDownloaded(lang, isDownloaded -> {
                runOnUiThread(() -> {
                    if (!isDownloaded) {
                        tvTranslationNotice.setVisibility(View.VISIBLE);
                        tvTranslationNotice.setOnClickListener(v -> checkAndTranslate());
                    } else if (!isTranslated) {
                        checkAndTranslate();
                    }
                });
            });
        } else {
            btnTranslate.setVisibility(View.GONE);
            tvTranslationNotice.setVisibility(View.GONE);
        }
    }

    private void checkAndTranslate() {
        if (isTranslated) {
            // Switch back to English
            isTranslated = false;
            bindData(currentScheme);
            btnTranslate.setAlpha(1.0f);
            return;
        }

        String lang = prefsManager.getContentLanguage();
        if ("en".equalsIgnoreCase(lang)) return;

        translationService.isModelDownloaded(lang, isDownloaded -> {
            runOnUiThread(() -> {
                if (isDownloaded) {
                    performTranslation(lang);
                } else {
                    showDownloadDialog(lang);
                }
            });
        });
    }

    private void showDownloadDialog(String lang) {
        String langName = lang.equalsIgnoreCase("hi") ? "Hindi" : "Marathi";
        new AlertDialog.Builder(this)
                .setTitle("Download " + langName + " Model")
                .setMessage("This will download ~30MB for offline translation. Continue on mobile data?")
                .setPositiveButton("Download", (dialog, which) -> {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(this, "Internet connection required for model download", Toast.LENGTH_LONG).show();
                        return;
                    }
                    pbTranslate.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Download initiated...", Toast.LENGTH_LONG).show();
                    translationService.downloadModel(lang, new TranslationManager.ModelDownloadCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                pbTranslate.setVisibility(View.GONE);
                                performTranslation(lang);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                pbTranslate.setVisibility(View.GONE);
                                Toast.makeText(SchemeDetailActivity.this, R.string.model_download_failed, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void performTranslation(String lang) {
        if (currentScheme == null) return;
        tvTranslationNotice.setVisibility(View.GONE);

        // Check if we already have it in DB
        String cachedName = lang.equalsIgnoreCase("hi") ? currentScheme.getSchemeNameHi() : currentScheme.getSchemeNameMr();
        if (cachedName != null && !cachedName.isEmpty()) {
            applyTranslationsFromScheme(lang);
            return;
        }

        pbTranslate.setVisibility(View.VISIBLE);
        
        // Translate fields one by one
        translateField(currentScheme.getSchemeName(), lang, translated -> currentScheme.setSchemeNameHi(translated),
                translated -> currentScheme.setSchemeNameMr(translated), () -> {
            translateField(currentScheme.getSchemeDescription(), lang, translated -> currentScheme.setSchemeDescriptionHi(translated),
                    translated -> currentScheme.setSchemeDescriptionMr(translated), () -> {
                translateField(currentScheme.getBenefits(), lang, translated -> currentScheme.setBenefitsHi(translated),
                        translated -> currentScheme.setBenefitsMr(translated), () -> {
                    translateField(currentScheme.getApplicationProcess(), lang, translated -> currentScheme.setApplicationProcessHi(translated),
                            translated -> currentScheme.setApplicationProcessMr(translated), () -> {
                        translateField(currentScheme.getDocuments(), lang, translated -> currentScheme.setDocumentsHi(translated),
                                translated -> currentScheme.setDocumentsMr(translated), () -> {
                            translateField(currentScheme.getEligibilityText(), lang, translated -> currentScheme.setEligibilityTextHi(translated),
                                    translated -> currentScheme.setEligibilityTextMr(translated), () -> {
                                
                                runOnUiThread(() -> {
                                    pbTranslate.setVisibility(View.GONE);
                                    isTranslated = true;
                                    applyTranslationsFromScheme(lang);
                                    repository.updateSchemeTranslations(currentScheme);
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    interface TranslationFieldSetter {
        void set(String translated);
    }

    private void translateField(String text, String lang, TranslationFieldSetter hiSetter, TranslationFieldSetter mrSetter, Runnable next) {
        if (text == null || text.trim().isEmpty()) {
            next.run();
            return;
        }
        translationService.translate(text, lang, new TranslationManager.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                if (lang.equalsIgnoreCase("hi")) hiSetter.set(translatedText);
                else mrSetter.set(translatedText);
                next.run();
            }

            @Override
            public void onError(Exception e) {
                next.run();
            }
        });
    }

    private void applyTranslationsFromScheme(String lang) {
        boolean isHi = lang.equalsIgnoreCase("hi");
        tvSchemeName.setText(isHi ? currentScheme.getSchemeNameHi() : currentScheme.getSchemeNameMr());
        tvSchemeDescription.setText(isHi ? currentScheme.getSchemeDescriptionHi() : currentScheme.getSchemeDescriptionMr());
        tvBenefits.setText(isHi ? currentScheme.getBenefitsHi() : currentScheme.getBenefitsMr());
        
        String appProc = isHi ? currentScheme.getApplicationProcessHi() : currentScheme.getApplicationProcessMr();
        String docs = isHi ? currentScheme.getDocumentsHi() : currentScheme.getDocumentsMr();
        
        StringBuilder appText = new StringBuilder();
        if (appProc != null && !appProc.isEmpty()) {
            appText.append(appProc);
        }
        if (docs != null && !docs.isEmpty()) {
            String reqDocs = getString(R.string.required_documents);
            if (appText.length() > 0) appText.append("\n\n").append(reqDocs).append("\n");
            else appText.append(reqDocs).append("\n");
            appText.append(docs);
        }
        tvApplicationProcess.setText(appText.toString());

        // Eligibility text
        String eligText = isHi ? currentScheme.getEligibilityTextHi() : currentScheme.getEligibilityTextMr();
        if (eligText != null && !eligText.isEmpty()) {
            String cleanState = currentScheme.getEligibleStates().replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
            tvCriteriaState.setText(getString(R.string.criteria_states, cleanState) + "\n\n" + getString(R.string.eligibility_details) + "\n" + eligText);
        }
        
        isTranslated = true;
        btnTranslate.setAlpha(0.5f); // Indicate it's already translated
        Toast.makeText(this, R.string.translated_badge, Toast.LENGTH_SHORT).show();
    }

    private void bindData(Scheme scheme) {
        tvSchemeName.setText(scheme.getSchemeName());
        tvSchemeTypeTag.setText(scheme.getSchemeType());
        tvSchemeDescription.setText(scheme.getSchemeDescription());
        tvBenefits.setText(scheme.getBenefits());

        StringBuilder appText = new StringBuilder();
        if (scheme.getApplicationProcess() != null && !scheme.getApplicationProcess().isEmpty()) {
            appText.append(scheme.getApplicationProcess());
        }
        if (scheme.getDocuments() != null && !scheme.getDocuments().isEmpty()) {
            String reqDocs = getString(R.string.required_documents);
            if (appText.length() > 0) appText.append("\n\n").append(reqDocs).append("\n");
            else appText.append(reqDocs).append("\n");
            appText.append(scheme.getDocuments());
        }
        tvApplicationProcess.setText(appText.toString());

        // Category Tag
        if (scheme.getSchemeCategory() != null && !scheme.getSchemeCategory().isEmpty()) {
            tvCategoryTag.setText(scheme.getSchemeCategory());
        } else {
            String occ = scheme.getEligibleOccupations();
            if (occ != null && occ.contains("Farmer")) {
                tvCategoryTag.setText(R.string.cat_agri);
            } else if (occ != null && occ.contains("Student")) {
                tvCategoryTag.setText(R.string.cat_edu);
            } else if (occ != null && occ.contains("Business")) {
                tvCategoryTag.setText(R.string.cat_ent);
            } else {
                tvCategoryTag.setText(R.string.cat_welfare);
            }
        }

        // Criteria Age
        if (scheme.getMinAge() != null && scheme.getMaxAge() != null) {
            tvCriteriaAge.setText(getString(R.string.criteria_age_range, scheme.getMinAge().toString(), scheme.getMaxAge().toString()));
        } else if (scheme.getMinAge() != null) {
            tvCriteriaAge.setText(getString(R.string.criteria_min_age, scheme.getMinAge().toString()));
        } else {
            tvCriteriaAge.setText(R.string.criteria_all_ages);
        }

        // Criteria Gender
        tvCriteriaGender.setText(getString(R.string.criteria_gender, scheme.getGenderEligible()));

        // Criteria Income
        if (scheme.getIncomeLimit() != null && scheme.getIncomeLimit() > 0) {
            tvCriteriaIncome.setText(getString(R.string.criteria_income, String.format("%,d", scheme.getIncomeLimit())));
        } else {
            tvCriteriaIncome.setText(R.string.criteria_no_income);
        }

        // Criteria Occupations
        String cleanOcc = scheme.getEligibleOccupations().replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
        tvCriteriaOccupation.setText(getString(R.string.criteria_occ, cleanOcc));

        // Criteria Education
        String edu = scheme.getMinEducationLevel() != null ? scheme.getMinEducationLevel() : getString(R.string.none);
        tvCriteriaEducation.setText(getString(R.string.criteria_edu, edu));

        // Criteria State
        String cleanState = scheme.getEligibleStates().replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
        if (scheme.getEligibilityText() != null && !scheme.getEligibilityText().isEmpty()) {
            tvCriteriaState.setText(getString(R.string.criteria_states, cleanState) + "\n\n" + getString(R.string.eligibility_details) + "\n" + scheme.getEligibilityText());
        } else {
            tvCriteriaState.setText(getString(R.string.criteria_states, cleanState));
        }

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

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network capabilities = cm.getActiveNetwork();
                if (capabilities == null) return false;
                NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(capabilities);
                if (activeNetwork == null) return false;
                return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            return true;
        }
    }
}
