package com.yojnika.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.yojnika.app.services.TranslationManager;
import com.yojnika.app.services.TranslationService;
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
    private MaterialButton btnDownloadHi, btnDownloadMr;
    private ProgressBar pbDownload;
    private View cvEditPhoto;
    private View cvThemeSelection;

    private SchemeRepository repository;
    private TranslationService translationService;
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
        translationService = TranslationService.getInstance();

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
        btnDownloadHi = view.findViewById(R.id.btnDownloadHi);
        btnDownloadMr = view.findViewById(R.id.btnDownloadMr);
        pbDownload = view.findViewById(R.id.pbDownload);
        cvEditPhoto = view.findViewById(R.id.cvEditPhoto);
        cvThemeSelection = view.findViewById(R.id.cvThemeSelection);

        Log.d("TRANSLATE", "ProfileFragment onCreateView: buttons assigned. Hi=" + (btnDownloadHi != null) + ", Mr=" + (btnDownloadMr != null));

        updateThemeSummary();
        updateDownloadButtons();

        ivProfileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfilePhotoViewerActivity.class);
            startActivity(intent);
        });

        ivProfileAvatar.setOnLongClickListener(v -> {
            int userId = repository.getLoggedInUserId();
            String path = SharedPrefsManager.getInstance(requireContext()).getProfileImagePath(userId);
            if (path != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Remove Photo")
                        .setMessage("Are you sure you want to remove your profile photo?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            SharedPrefsManager.getInstance(requireContext()).removeProfileImagePath(userId);
                            loadProfileImage(null);
                            Toast.makeText(requireContext(), "Photo removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
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

        btnDownloadHi.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Hindi download initiated", Toast.LENGTH_SHORT).show();
            downloadLanguage("hi");
        });
        btnDownloadMr.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Marathi download initiated", Toast.LENGTH_SHORT).show();
            downloadLanguage("mr");
        });

        return view;
    }

    private void updateDownloadButtons() {
        translationService.isModelDownloaded("hi", isDownloaded -> {
            Log.d("TRANSLATE", "Check Hindi model: isDownloaded=" + isDownloaded);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (isDownloaded) {
                    btnDownloadHi.setEnabled(false);
                    btnDownloadHi.setText("Downloaded ✓");
                } else {
                    btnDownloadHi.setEnabled(true);
                    btnDownloadHi.setText("Download");
                }
            });
        });
        translationService.isModelDownloaded("mr", isDownloaded -> {
            Log.d("TRANSLATE", "Check Marathi model: isDownloaded=" + isDownloaded);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (isDownloaded) {
                    btnDownloadMr.setEnabled(false);
                    btnDownloadMr.setText("Downloaded ✓");
                } else {
                    btnDownloadMr.setEnabled(true);
                    btnDownloadMr.setText("Download");
                }
            });
        });
    }

    private void downloadLanguage(String lang) {
        String langName = "hi".equalsIgnoreCase(lang) ? "Hindi" : "Marathi";
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Download " + langName + " Model")
                .setMessage("This will download ~30MB. Continue on mobile data?")
                .setPositiveButton("Download", (dialog, which) -> {
                    startLanguageDownload(lang);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startLanguageDownload(String lang) {
        Log.d("TRANSLATE", "startLanguageDownload entry: " + lang);
        if (!isNetworkAvailable()) {
            Log.w("TRANSLATE", "No internet for download");
            Toast.makeText(requireContext(), "Internet connection required for model download", Toast.LENGTH_LONG).show();
            return;
        }

        String langName = "hi".equalsIgnoreCase(lang) ? "Hindi" : "Marathi";
        MaterialButton targetBtn = "hi".equalsIgnoreCase(lang) ? btnDownloadHi : btnDownloadMr;

        Log.d("TRANSLATE", "Starting download flow for " + langName);
        pbDownload.setVisibility(View.VISIBLE);
        targetBtn.setEnabled(false);
        targetBtn.setText("Downloading...");

        translationService.downloadModel(lang, new TranslationManager.ModelDownloadCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    Log.d("TRANSLATE", langName + " model downloaded successfully");
                    pbDownload.setVisibility(View.GONE);
                    targetBtn.setText("Downloaded ✓");
                    targetBtn.setEnabled(false);
                    Toast.makeText(requireContext(), langName + " offline translation ready!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    Log.e("TRANSLATE", langName + " download failed: " + e.getMessage(), e);
                    pbDownload.setVisibility(View.GONE);
                    targetBtn.setEnabled(true);
                    targetBtn.setText("Download");
                    Toast.makeText(requireContext(), "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
            Log.e("TRANSLATE", "Error checking network", e);
            return true; // Fallback to true if check fails
        }
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
            int userId = repository.getLoggedInUserId();
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getFilesDir(), "profile_" + userId + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            SharedPrefsManager.getInstance(requireContext()).saveProfileImagePath(userId, file.getAbsolutePath());
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
        int userId = repository.getLoggedInUserId();
        loadProfileImage(SharedPrefsManager.getInstance(requireContext()).getProfileImagePath(userId));
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
