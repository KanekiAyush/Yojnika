package com.yojnika.app.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.appcompat.app.AppCompatDelegate;
import com.yojnika.app.R;
import com.yojnika.app.activities.LoginActivity;
import com.yojnika.app.activities.ProfileActivity;
import com.yojnika.app.activities.ProfilePhotoViewerActivity;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.SharedPrefsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private ShapeableImageView ivProfileAvatar;
    private TextView tvProfileName;
    private TextView tvProfileLocation;
    private TextView tvSummaryAge;
    private TextView tvSummaryGender;
    private TextView tvSummaryIncome;
    private TextView tvSummaryOccupation;
    private TextView tvSummaryEducation;
    private TextView tvSummaryCategory;
    private TextView tvSummaryMarital;
    private TextView tvCurrentTheme;
    private MaterialButton btnEditProfileHeader;
    private MaterialButton btnLogout;
    private View cvEditPhoto;
    private View cvThemeSelection;

    private SchemeRepository repository;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                saveAndSetProfileImage(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        repository = SchemeRepository.getInstance(requireContext());

        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileLocation = view.findViewById(R.id.tvProfileLocation);
        tvSummaryAge = view.findViewById(R.id.tvSummaryAge);
        tvSummaryGender = view.findViewById(R.id.tvSummaryGender);
        tvSummaryIncome = view.findViewById(R.id.tvSummaryIncome);
        tvSummaryOccupation = view.findViewById(R.id.tvSummaryOccupation);
        tvSummaryEducation = view.findViewById(R.id.tvSummaryEducation);
        tvSummaryCategory = view.findViewById(R.id.tvSummaryCategory);
        tvSummaryMarital = view.findViewById(R.id.tvSummaryMarital);
        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        btnEditProfileHeader = view.findViewById(R.id.btnEditProfileHeader);
        btnLogout = view.findViewById(R.id.btnLogout);
        cvEditPhoto = view.findViewById(R.id.cvEditPhoto);
        cvThemeSelection = view.findViewById(R.id.cvThemeSelection);

        updateThemeSummary();

        ivProfileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfilePhotoViewerActivity.class);
            startActivity(intent);
        });

        cvEditPhoto.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        cvThemeSelection.setOnClickListener(v -> showThemeSelectionDialog());

        btnEditProfileHeader.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void showThemeSelectionDialog() {
        String[] options = {
                getString(R.string.theme_light),
                getString(R.string.theme_dark),
                getString(R.string.theme_system)
        };

        int currentMode = SharedPrefsManager.getInstance(requireContext()).getThemeMode();
        int checkedItem;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 0;
        else if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 1;
        else checkedItem = 2;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.theme_title)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int mode;
                    switch (which) {
                        case 0: mode = AppCompatDelegate.MODE_NIGHT_NO; break;
                        case 1: mode = AppCompatDelegate.MODE_NIGHT_YES; break;
                        default: mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
                    }
                    SharedPrefsManager.getInstance(requireContext()).setThemeMode(mode);
                    SharedPrefsManager.getInstance(requireContext()).applyTheme();
                    updateThemeSummary();
                    dialog.dismiss();
                })
                .show();
    }

    private void updateThemeSummary() {
        int mode = SharedPrefsManager.getInstance(requireContext()).getThemeMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            tvCurrentTheme.setText(R.string.theme_light);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            tvCurrentTheme.setText(R.string.theme_dark);
        } else {
            tvCurrentTheme.setText(R.string.theme_system);
        }
    }

    private void saveAndSetProfileImage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getFilesDir(), "profile_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            SharedPrefsManager.getInstance(requireContext()).saveProfileImagePath(file.getAbsolutePath());
            loadProfileImage(file.getAbsolutePath());
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImage(String path) {
        if (path != null && new File(path).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivProfileAvatar.setImageBitmap(bitmap);
            ivProfileAvatar.setPadding(0, 0, 0, 0);
            ivProfileAvatar.setImageTintList(null);
        } else {
            ivProfileAvatar.setImageResource(R.drawable.ic_profile);
            ivProfileAvatar.setPadding(16, 16, 16, 16);
            ivProfileAvatar.setImageTintList(requireContext().getColorStateList(R.color.primary));
        }
    }

    private void logout() {
        SharedPrefsManager.getInstance(requireContext()).clearSession();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
        loadProfileImage(SharedPrefsManager.getInstance(requireContext()).getProfileImagePath());
    }

    private void loadProfileData() {
        UserProfile profile = repository.getUserProfile();
        if (profile != null && profile.isComplete()) {
            tvProfileName.setText(profile.getFullName());
            tvProfileLocation.setText(profile.getDistrict() + ", " + profile.getState());
            tvSummaryAge.setText(getString(R.string.years_label, String.valueOf(profile.getAge())));
            
            // Translate gender
            tvSummaryGender.setText(translateValue(profile.getGender(), Constants.GENDERS, R.array.gender_array));
            
            tvSummaryIncome.setText("₹ " + String.format("%,d", profile.getAnnualIncome()));
            
            // Translate other fields
            tvSummaryOccupation.setText(translateValue(profile.getOccupation(), Constants.OCCUPATIONS, R.array.occupation_array));
            tvSummaryEducation.setText(translateValue(profile.getEducationLevel(), Constants.EDUCATION_LEVELS, R.array.education_array));
            tvSummaryCategory.setText(translateValue(profile.getCategory(), Constants.CATEGORIES, R.array.category_array));
            tvSummaryMarital.setText(translateValue(profile.getMaritalStatus(), Constants.MARITAL_STATUSES, R.array.marital_status_array));
            
        } else {
            tvProfileName.setText(R.string.guest_citizen);
            tvProfileLocation.setText(R.string.profile_not_configured);
            String notSet = getString(R.string.not_set);
            tvSummaryAge.setText(notSet);
            tvSummaryGender.setText(notSet);
            tvSummaryIncome.setText(notSet);
            tvSummaryOccupation.setText(notSet);
            tvSummaryEducation.setText(notSet);
            tvSummaryCategory.setText(notSet);
            tvSummaryMarital.setText(notSet);
        }
    }

    private String translateValue(String englishValue, String[] englishArray, int arrayResId) {
        if (englishValue == null) return getString(R.string.not_set);
        
        String[] translatedArray = getResources().getStringArray(arrayResId);
        for (int i = 0; i < englishArray.length; i++) {
            if (englishValue.equalsIgnoreCase(englishArray[i])) {
                if (i < translatedArray.length) {
                    return translatedArray[i];
                }
            }
        }
        return englishValue; // Return original if no match found (for "Other" values)
    }
}
