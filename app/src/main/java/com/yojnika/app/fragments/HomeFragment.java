package com.yojnika.app.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.yojnika.app.R;
import com.yojnika.app.activities.CategorySchemesActivity;
import com.yojnika.app.activities.ProfileActivity;
import com.yojnika.app.activities.SchemeDetailActivity;
import com.yojnika.app.adapters.SchemeAdapter;
import com.yojnika.app.models.Recommendation;
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

    private TextView tvHomeGreeting;
    private TextView tvHomeSubtitle;
    private TextView tvMlEngineStatus;
    private ShapeableImageView btnQuickProfile, btnLanguage;

    private MaterialCardView cardProfileWarning;
    private MaterialButton btnSetupProfile;
    private RecyclerView rvRecommendations;
    private LinearLayout llHomeEmptyState;
    private MaterialButton btnEmptyCreateProfile;
    private ProgressBar pbHomeLoading;

    private MaterialCardView cardCategoryEducation, cardCategoryAgriculture, cardCategoryHealth, cardCategoryBusiness, cardCategorySocial;

    private SchemeRepository repository;
    private SchemeAdapter adapter;
    private final List<Scheme> recommendedSchemes = new ArrayList<>();

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
        loadRecommendations();
        loadProfileImage();
    }

    private void loadProfileImage() {
        String path = SharedPrefsManager.getInstance(requireContext()).getProfileImagePath();
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
        
        tvHomeGreeting.setText(getString(R.string.home_greeting_default));
    }

    private void setupRecyclerView() {
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchemeAdapter(requireContext(), recommendedSchemes, true, this);
        rvRecommendations.setAdapter(adapter);
    }

    private void setupClickListeners() {
        View.OnClickListener openProfile = v -> {
            Intent intent = new Intent(requireActivity(), ProfileActivity.class);
            startActivity(intent);
        };

        btnQuickProfile.setOnClickListener(openProfile);
        btnSetupProfile.setOnClickListener(openProfile);
        btnEmptyCreateProfile.setOnClickListener(openProfile);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        cardCategoryEducation.setOnClickListener(v -> openCategory("Education", "Education"));
        cardCategoryAgriculture.setOnClickListener(v -> openCategory("Agriculture", "Agri"));
        cardCategoryHealth.setOnClickListener(v -> openCategory("Health", "Health"));
        cardCategoryBusiness.setOnClickListener(v -> openCategory("Business", "Entrepre"));
        cardCategorySocial.setOnClickListener(v -> openCategory("Social Welfare", "Welfare"));
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.hindi), getString(R.string.marathi)};
        String[] languageCodes = {"en", "hi", "mr"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.choose_language);
        builder.setItems(languages, (dialog, which) -> {
            String selectedLang = languageCodes[which];
            LocaleHelper.setLocale(requireContext(), selectedLang);
            requireActivity().recreate();
        });
        builder.show();
    }

    private void openCategory(String name, String dbCategory) {
        Intent intent = new Intent(requireActivity(), CategorySchemesActivity.class);
        intent.putExtra("CATEGORY_NAME", name);
        intent.putExtra("DB_CATEGORY", dbCategory);
        startActivity(intent);
    }

    private void loadRecommendations() {
        UserProfile profile = repository.getUserProfile();

        if (repository.isMlModelLoaded()) {
            tvMlEngineStatus.setText("ONNX On-Device Inference • Active");
        } else {
            tvMlEngineStatus.setText("Edge ML & Rule Engine • Active");
        }

        if (profile == null || !profile.isComplete()) {
            tvHomeGreeting.setText(R.string.home_greeting_default);
            tvHomeSubtitle.setText(R.string.profile_incomplete_warning);
            cardProfileWarning.setVisibility(View.VISIBLE);
            llHomeEmptyState.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.GONE);
            return;
        }

        cardProfileWarning.setVisibility(View.GONE);
        llHomeEmptyState.setVisibility(View.GONE);
        rvRecommendations.setVisibility(View.VISIBLE);
        pbHomeLoading.setVisibility(View.VISIBLE);

        tvHomeGreeting.setText(getString(R.string.home_greeting_default));
        tvHomeSubtitle.setText(getString(R.string.home_subtitle));

        repository.getRecommendedSchemes(profile, recommendations -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                pbHomeLoading.setVisibility(View.GONE);
                recommendedSchemes.clear();
                if (recommendations != null) {
                    for (Recommendation r : recommendations) {
                        if (r.getScheme() != null) {
                            recommendedSchemes.add(r.getScheme());
                        }
                    }
                }
                adapter.updateData(recommendedSchemes);

                if (recommendedSchemes.isEmpty()) {
                    llHomeEmptyState.setVisibility(View.VISIBLE);
                    rvRecommendations.setVisibility(View.GONE);
                } else {
                    llHomeEmptyState.setVisibility(View.GONE);
                    rvRecommendations.setVisibility(View.VISIBLE);
                }
            });
        });
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
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                scheme.setBookmarked(isBookmarked);
                adapter.notifyItemChanged(position);
                Toast.makeText(
                        requireContext(),
                        isBookmarked ? R.string.scheme_saved : R.string.scheme_removed,
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }
}
