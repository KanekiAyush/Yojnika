package com.yojnika.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.yojnika.app.R;
import com.yojnika.app.adapters.SchemeAdapter;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CategorySchemesActivity extends AppCompatActivity implements SchemeAdapter.OnSchemeClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView rvSchemes;
    private LinearLayout llEmptyState;
    private ProgressBar pbLoading;
    private TextView tvEmptyMessage;

    private SchemeRepository repository;
    private SchemeAdapter adapter;
    private final List<Scheme> schemeList = new ArrayList<>();
    private String categoryName;
    private String databaseCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_schemes);

        repository = SchemeRepository.getInstance(this);

        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        databaseCategory = getIntent().getStringExtra("DB_CATEGORY");

        if (categoryName == null) {
            categoryName = "Schemes";
        }

        initViews();
        setupRecyclerView();
        loadSchemes();

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarCategory);
        rvSchemes = findViewById(R.id.rvCategorySchemes);
        llEmptyState = findViewById(R.id.llCategoryEmptyState);
        pbLoading = findViewById(R.id.pbCategoryLoading);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        toolbar.setTitle(categoryName + " Schemes");
    }

    private void setupRecyclerView() {
        rvSchemes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SchemeAdapter(this, schemeList, false, this);
        rvSchemes.setAdapter(adapter);
    }

    private void loadSchemes() {
        pbLoading.setVisibility(View.VISIBLE);
        rvSchemes.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        repository.searchAndFilterSchemes(null, null, null, databaseCategory, schemes -> {
            runOnUiThread(() -> {
                pbLoading.setVisibility(View.GONE);
                schemeList.clear();
                if (schemes != null && !schemes.isEmpty()) {
                    schemeList.addAll(schemes);
                    adapter.notifyDataSetChanged();
                    rvSchemes.setVisibility(View.VISIBLE);
                } else {
                    llEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyMessage.setText("No schemes available for " + categoryName);
                }
            });
        });
    }

    @Override
    public void onSchemeClick(Scheme scheme) {
        Intent intent = new Intent(this, SchemeDetailActivity.class);
        intent.putExtra(Constants.EXTRA_SCHEME_ID, scheme.getSchemeId());
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(Scheme scheme, int position) {
        repository.toggleBookmark(scheme.getSchemeId(), isBookmarked -> {
            runOnUiThread(() -> {
                scheme.setBookmarked(isBookmarked);
                adapter.notifyItemChanged(position);
                Toast.makeText(
                        this,
                        isBookmarked ? R.string.scheme_saved : R.string.scheme_removed,
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }
}
