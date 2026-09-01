package com.yojnika.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.yojnika.app.R;
import com.yojnika.app.activities.ProfileActivity;
import com.yojnika.app.activities.SchemeDetailActivity;
import com.yojnika.app.adapters.SchemeAdapter;
import com.yojnika.app.models.Recommendation;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements SchemeAdapter.OnSchemeClickListener {

    private TextView tvHomeGreeting;
    private TextView tvHomeSubtitle;
    private TextView tvMlEngineStatus;
    private ImageView btnQuickProfile;
    private ImageView btnRefreshRecs;
    private MaterialCardView cardProfileWarning;
    private MaterialButton btnSetupProfile;
    private RecyclerView rvRecommendations;
    private LinearLayout llHomeEmptyState;
    private MaterialButton btnEmptyCreateProfile;
    private ProgressBar pbHomeLoading;

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
        btnRefreshRecs = view.findViewById(R.id.btnRefreshRecs);
        cardProfileWarning = view.findViewById(R.id.cardProfileWarning);
        btnSetupProfile = view.findViewById(R.id.btnSetupProfile);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        llHomeEmptyState = view.findViewById(R.id.llHomeEmptyState);
        btnEmptyCreateProfile = view.findViewById(R.id.btnEmptyCreateProfile);
        pbHomeLoading = view.findViewById(R.id.pbHomeLoading);

        setupRecyclerView();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecommendations();
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

        btnRefreshRecs.setOnClickListener(v -> loadRecommendations());
    }

    private void loadRecommendations() {
        UserProfile profile = repository.getUserProfile();

        if (repository.isMlModelLoaded()) {
            tvMlEngineStatus.setText("ONNX On-Device Inference • Active");
        } else {
            tvMlEngineStatus.setText("Edge ML & Rule Engine • Active");
        }

        if (profile == null || !profile.isComplete()) {
            tvHomeGreeting.setText("Namaste, Citizen!");
            tvHomeSubtitle.setText("Complete your profile for personalized recommendations");
            cardProfileWarning.setVisibility(View.VISIBLE);
            llHomeEmptyState.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.GONE);
            return;
        }

        cardProfileWarning.setVisibility(View.GONE);
        llHomeEmptyState.setVisibility(View.GONE);
        rvRecommendations.setVisibility(View.VISIBLE);
        pbHomeLoading.setVisibility(View.VISIBLE);

        String firstName = profile.getFullName().split(" ")[0];
        tvHomeGreeting.setText(getString(R.string.home_greeting, firstName));
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
