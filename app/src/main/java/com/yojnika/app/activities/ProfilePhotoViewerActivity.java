package com.yojnika.app.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.yojnika.app.R;
import com.yojnika.app.utils.SharedPrefsManager;

import java.io.File;

public class ProfilePhotoViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Full screen
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_profile_photo_viewer);

        ImageView ivFullPhoto = findViewById(R.id.ivFullProfilePhoto);
        ImageButton btnBack = findViewById(R.id.btnBackViewer);

        btnBack.setOnClickListener(v -> finish());

        int userId = SharedPrefsManager.getInstance(this).getLoggedInUserId();
        String path = SharedPrefsManager.getInstance(this).getProfileImagePath(userId);
        if (path != null && new File(path).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivFullPhoto.setImageBitmap(bitmap);
        } else {
            ivFullPhoto.setImageResource(R.drawable.ic_profile);
            ivFullPhoto.setPadding(100, 100, 100, 100);
            ivFullPhoto.setColorFilter(getColor(R.color.primary));
        }
    }
}
