package com.yojnika.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.yojnika.app.R;
import com.yojnika.app.activities.ProfileActivity;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileLocation;
    private TextView tvSummaryAge;
    private TextView tvSummaryGender;
    private TextView tvSummaryIncome;
    private TextView tvSummaryOccupation;
    private TextView tvSummaryEducation;
    private TextView tvSummaryCategory;
    private TextView tvSummaryMarital;
    private MaterialButton btnEditProfileHeader;

    private SchemeRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        repository = SchemeRepository.getInstance(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileLocation = view.findViewById(R.id.tvProfileLocation);
        tvSummaryAge = view.findViewById(R.id.tvSummaryAge);
        tvSummaryGender = view.findViewById(R.id.tvSummaryGender);
        tvSummaryIncome = view.findViewById(R.id.tvSummaryIncome);
        tvSummaryOccupation = view.findViewById(R.id.tvSummaryOccupation);
        tvSummaryEducation = view.findViewById(R.id.tvSummaryEducation);
        tvSummaryCategory = view.findViewById(R.id.tvSummaryCategory);
        tvSummaryMarital = view.findViewById(R.id.tvSummaryMarital);
        btnEditProfileHeader = view.findViewById(R.id.btnEditProfileHeader);

        btnEditProfileHeader.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        UserProfile profile = repository.getUserProfile();
        if (profile != null && profile.isComplete()) {
            tvProfileName.setText(profile.getFullName());
            tvProfileLocation.setText(profile.getDistrict() + ", " + profile.getState());
            tvSummaryAge.setText(profile.getAge() + " years");
            tvSummaryGender.setText(profile.getGender());
            tvSummaryIncome.setText("₹ " + String.format("%,d", profile.getAnnualIncome()));
            tvSummaryOccupation.setText(profile.getOccupation());
            tvSummaryEducation.setText(profile.getEducationLevel());
            tvSummaryCategory.setText(profile.getCategory());
            tvSummaryMarital.setText(profile.getMaritalStatus());
        } else {
            tvProfileName.setText("Guest Citizen");
            tvProfileLocation.setText("Profile not yet configured");
            tvSummaryAge.setText("Not set");
            tvSummaryGender.setText("Not set");
            tvSummaryIncome.setText("Not set");
            tvSummaryOccupation.setText("Not set");
            tvSummaryEducation.setText("Not set");
            tvSummaryCategory.setText("Not set");
            tvSummaryMarital.setText("Not set");
        }
    }
}
