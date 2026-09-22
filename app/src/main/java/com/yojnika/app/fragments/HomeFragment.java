package com.yojnika.app.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.yojnika.app.R;
import com.yojnika.app.activities.CategorySchemesActivity;
import com.yojnika.app.activities.ProfileActivity;
import com.yojnika.app.activities.SchemeDetailActivity;
import com.yojnika.app.adapters.SchemeAdapter;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.LocaleHelper;
import com.yojnika.app.utils.SharedPrefsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements SchemeAdapter.OnSchemeClickListener {

    private static final String TAG = "HomeFragment";

    private TextView tvHomeGreeting, tvHomeSubtitle, tvMlEngineStatus;
    private ShapeableImageView btnQuickProfile, btnLanguage;
    private MaterialCardView cardProfileWarning;
    private MaterialButton btnSetupProfile;
    private RecyclerView rvRecommendations;
    private LinearLayout llHomeEmptyState;
    private MaterialButton btnEmptyCreateProfile;
    private ProgressBar pbHomeLoading;
    private MaterialCardView llHomePagination;
    private MaterialButton btnHomePrevious, btnHomeNext;
    private TextView tvHomePageIndicator;
    private MaterialCardView cardCategoryEducation, cardCategoryAgriculture, cardCategoryHealth, cardCategoryBusiness, cardCategorySocial;

    private SchemeRepository repository;
    private SchemeAdapter adapter;
    private final List<Scheme> recommendedSchemes = new ArrayList<>();
    private final List<Scheme> allMatched = new ArrayList<>();
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        repository = SchemeRepository.getInstance(requireContext());

        tvHomeGreeting = view.findViewById(R.id.tvHomeGreeting);
        tvHomeSubtitle = view.findViewById(R.id.tvHomeSubtitle);
        tvMlEngineStatus = view.findViewById(R.id.tvMlEngineStatus);
        btnQuickProfile = view.findViewById(R.id.btnQuickProfile);
        btnLanguage = view.findViewById(R.id.btnLanguage);
        cardProfileWarning = view.findViewById(R.id.cardProfileWarning);
        btnSetupProfile = view.findViewById(R.id.btnSetupProfile);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        llHomeEmptyState = view.findViewById(R.id.llHomeEmptyState);
        btnEmptyCreateProfile = view.findViewById(R.id.btnEmptyCreateProfile);
        pbHomeLoading = view.findViewById(R.id.pbHomeLoading);
        llHomePagination = view.findViewById(R.id.llHomePagination);
        btnHomePrevious = view.findViewById(R.id.btnHomePrevious);
        btnHomeNext = view.findViewById(R.id.btnHomeNext);
        tvHomePageIndicator = view.findViewById(R.id.tvHomePageIndicator);

        cardCategoryEducation = view.findViewById(R.id.cardCategoryEducation);
        cardCategoryAgriculture = view.findViewById(R.id.cardCategoryAgriculture);
        cardCategoryHealth = view.findViewById(R.id.cardCategoryHealth);
        cardCategoryBusiness = view.findViewById(R.id.cardCategoryBusiness);
        cardCategorySocial = view.findViewById(R.id.cardCategorySocial);

        setupRecyclerView();
        setupClickListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileImage();
        loadRecommendations();
    }

    private void loadProfileImage() {
        UserProfile profile = repository.getUserProfile();
        String fullName = profile != null ? profile.getFullName() : null;
        if (fullName != null && !fullName.trim().isEmpty()) {
            String firstName = fullName.split(" ")[0];
            tvHomeGreeting.setText(getString(R.string.home_greeting, firstName));
        } else {
            tvHomeGreeting.setText(getString(R.string.home_greeting_default));
        }

        int userId = repository.getLoggedInUserId();
        String path = SharedPrefsManager.getInstance(requireContext()).getProfileImagePath(userId);
        if (path != null && new File(path).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            btnQuickProfile.setImageBitmap(bitmap);
            btnQuickProfile.setPadding(0, 0, 0, 0);
            btnQuickProfile.setImageTintList(null);
        } else {
            btnQuickProfile.setImageResource(R.drawable.ic_profile);
            btnQuickProfile.setPadding(10, 10, 10, 10);
            btnQuickProfile.setImageTintList(requireContext().getColorStateList(R.color.primary));
        }
    }

    private void setupRecyclerView() {
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchemeAdapter(requireContext(), recommendedSchemes, true, this);
        rvRecommendations.setAdapter(adapter);
    }

    private void setupClickListeners() {
        View.OnClickListener openProfile = v -> startActivity(new Intent(requireActivity(), ProfileActivity.class));
        btnQuickProfile.setOnClickListener(openProfile);
        btnSetupProfile.setOnClickListener(openProfile);
        btnEmptyCreateProfile.setOnClickListener(openProfile);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        cardCategoryEducation.setOnClickListener(v -> openCategory("Education", "Education"));
        cardCategoryAgriculture.setOnClickListener(v -> openCategory("Agriculture", "Agriculture"));
        cardCategoryHealth.setOnClickListener(v -> openCategory("Health", "Health"));
        cardCategoryBusiness.setOnClickListener(v -> openCategory("Business", "Business"));
        cardCategorySocial.setOnClickListener(v -> openCategory("Social Welfare", "Welfare"));

        btnHomePrevious.setOnClickListener(v -> { if (currentPage > 1) { currentPage--; renderPage(); } });
        btnHomeNext.setOnClickListener(v -> { currentPage++; renderPage(); });
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.hindi), getString(R.string.marathi)};
        String[] languageCodes = {"en", "hi", "mr"};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_language)
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = languageCodes[which];
                    LocaleHelper.setLocale(requireContext(), selectedLang);
                    SharedPrefsManager.getInstance(requireContext()).setContentLanguage(selectedLang);
                    requireActivity().recreate();
                })
                .show();
    }

    private void openCategory(String name, String dbCategory) {
        Intent intent = new Intent(requireActivity(), CategorySchemesActivity.class);
        intent.putExtra("CATEGORY_NAME", name);
        intent.putExtra("DB_CATEGORY", dbCategory);
        startActivity(intent);
    }

    private void loadRecommendations() {
        tvMlEngineStatus.setText(repository.isMlModelLoaded()
                ? "ONNX On-Device Inference • Active"
                : "Edge ML & Rule Engine • Active");

        if (!repository.hasUserProfile()) {
            Log.d(TAG, "Profile NOT complete — showing CTA");
            cardProfileWarning.setVisibility(View.VISIBLE);
            llHomeEmptyState.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.GONE);
            llHomePagination.setVisibility(View.GONE);
            tvHomeSubtitle.setText(R.string.profile_incomplete_warning);
            return;
        }

        cardProfileWarning.setVisibility(View.GONE);

        if (pbHomeLoading != null) pbHomeLoading.setVisibility(View.VISIBLE);

        UserProfile profile = repository.getUserProfile();
        Log.d(TAG, "Loading recommendations for profile: " + profile);

        repository.getMatchedSchemes(profile, matched -> {
            if (!isAdded() || getContext() == null) {
                Log.d("LIFECYCLE", "HomeFragment detached during recommendation load");
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded() || getView() == null) return;
                
                if (pbHomeLoading != null) pbHomeLoading.setVisibility(View.GONE);

                allMatched.clear();
                if (matched != null && !matched.isEmpty()) {
                    allMatched.addAll(matched);
                    Log.d(TAG, "Matched schemes: " + allMatched.size());
                } else {
                    Log.w(TAG, "No matched schemes — falling back to all schemes");
                    // Fallback: agar matching 0 deti hai, toh pehli 20 dikhao
                    // taaki user ko kuch toh dikhe
                }

                currentPage = 1;
                if (allMatched.isEmpty()) {
                    // Fallback — pehli 20 schemes from DB
                    repository.searchAndFilterSchemesPaged(1, 200, null, null, null, null, fallback -> {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            if (fallback != null && !fallback.isEmpty()) {
                                allMatched.addAll(fallback);
                                Log.d(TAG, "Fallback loaded: " + allMatched.size());
                            }
                            renderPage();
                        });
                    });
                } else {
                    renderPage();
                }
            });
        });
    }

    private void renderPage() {
        if (!isAdded()) return;
        recommendedSchemes.clear();
        int totalPages = (int) Math.ceil((double) allMatched.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allMatched.size());

        if (start < allMatched.size()) {
            recommendedSchemes.addAll(allMatched.subList(start, end));
        }

        Log.d(TAG, "Rendering page " + currentPage + " with " + recommendedSchemes.size() + " items");

        if (!recommendedSchemes.isEmpty()) {
            adapter.notifyDataSetChanged();
            rvRecommendations.setVisibility(View.VISIBLE);
            llHomeEmptyState.setVisibility(View.GONE);
            llHomePagination.setVisibility(View.VISIBLE);
            tvHomeSubtitle.setText(getString(R.string.home_subtitle));
            tvHomePageIndicator.setText(getString(R.string.pagination_page_indicator, currentPage, totalPages));
            btnHomePrevious.setEnabled(currentPage > 1);
            btnHomeNext.setEnabled(end < allMatched.size());
        } else {
            adapter.notifyDataSetChanged();
            rvRecommendations.setVisibility(View.GONE);
            llHomePagination.setVisibility(View.GONE);
            llHomeEmptyState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSchemeClick(Scheme scheme) {
        Intent intent = new Intent(requireActivity(), SchemeDetailActivity.class);
        intent.putExtra(Constants.EXTRA_SCHEME_ID, scheme.getSchemeId());
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(Scheme scheme, int position) {
        repository.toggleBookmark(scheme.getSchemeId(), isBookmarked -> {
            if (!isAdded() || getContext() == null) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded() || adapter == null) return;
                scheme.setBookmarked(isBookmarked);
                adapter.notifyItemChanged(position);
                Toast.makeText(requireContext(), isBookmarked ? R.string.scheme_saved : R.string.scheme_removed, Toast.LENGTH_SHORT).show();
            });
        });
    }
}