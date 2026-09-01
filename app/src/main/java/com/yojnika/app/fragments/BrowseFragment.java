package com.yojnika.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.yojnika.app.R;
import com.yojnika.app.activities.SchemeDetailActivity;
import com.yojnika.app.adapters.SchemeAdapter;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment implements SchemeAdapter.OnSchemeClickListener {

    private EditText etSearchQuery;
    private ImageView btnClearSearch;
    private Chip chipStateFilter;
    private Chip chipTypeFilter;
    private Chip chipCategoryFilter;
    private Chip chipResetFilter;
    private TextView tvBrowseCount;
    private RecyclerView rvBrowseSchemes;
    private LinearLayout llBrowseEmptyState;

    private SchemeRepository repository;
    private SchemeAdapter adapter;
    private final List<Scheme> schemeList = new ArrayList<>();

    private String selectedState = "All";
    private String selectedType = "All";
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        repository = SchemeRepository.getInstance(requireContext());

        etSearchQuery = view.findViewById(R.id.etSearchQuery);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        chipStateFilter = view.findViewById(R.id.chipStateFilter);
        chipTypeFilter = view.findViewById(R.id.chipTypeFilter);
        chipCategoryFilter = view.findViewById(R.id.chipCategoryFilter);
        chipResetFilter = view.findViewById(R.id.chipResetFilter);
        tvBrowseCount = view.findViewById(R.id.tvBrowseCount);
        rvBrowseSchemes = view.findViewById(R.id.rvBrowseSchemes);
        llBrowseEmptyState = view.findViewById(R.id.llBrowseEmptyState);

        setupRecyclerView();
        setupSearchAndFilters();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        performSearchAndFilter();
    }

    private void setupRecyclerView() {
        rvBrowseSchemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchemeAdapter(requireContext(), schemeList, false, this);
        rvBrowseSchemes.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                performSearchAndFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearchQuery.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        // State filter dialog
        chipStateFilter.setOnClickListener(v -> showStateFilterDialog());

        // Type filter dialog
        chipTypeFilter.setOnClickListener(v -> showTypeFilterDialog());

        // Category filter dialog
        chipCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());

        // Reset filter
        chipResetFilter.setOnClickListener(v -> {
            selectedState = "All";
            selectedType = "All";
            selectedCategory = "All";
            etSearchQuery.setText("");
            updateFilterChipsUI();
            performSearchAndFilter();
        });
    }

    private void showStateFilterDialog() {
        String[] states = getResources().getStringArray(R.array.indian_states_array);
        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by State / UT")
                .setItems(states, (dialog, which) -> {
                    selectedState = states[which];
                    updateFilterChipsUI();
                    performSearchAndFilter();
                })
                .show();
    }

    private void showTypeFilterDialog() {
        String[] types = getResources().getStringArray(R.array.scheme_type_filter_array);
        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Scheme Type")
                .setItems(types, (dialog, which) -> {
                    selectedType = types[which];
                    updateFilterChipsUI();
                    performSearchAndFilter();
                })
                .show();
    }

    private void showCategoryFilterDialog() {
        String[] categories = getResources().getStringArray(R.array.category_filter_array);
        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Category / Caste")
                .setItems(categories, (dialog, which) -> {
                    selectedCategory = categories[which];
                    updateFilterChipsUI();
                    performSearchAndFilter();
                })
                .show();
    }

    private void updateFilterChipsUI() {
        chipStateFilter.setText("State: " + (selectedState.equals("All India") ? "All" : selectedState));
        chipTypeFilter.setText("Type: " + (selectedType.equals("All Types") ? "All" : selectedType));
        chipCategoryFilter.setText("Category: " + (selectedCategory.equals("All Categories") ? "All" : selectedCategory));
    }

    private void performSearchAndFilter() {
        String query = etSearchQuery.getText().toString().trim();
        repository.searchAndFilterSchemes(query, selectedState, selectedType, selectedCategory, list -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                schemeList.clear();
                if (list != null) {
                    schemeList.addAll(list);
                }
                adapter.updateData(schemeList);

                tvBrowseCount.setText("Showing " + schemeList.size() + " government schemes");

                if (schemeList.isEmpty()) {
                    llBrowseEmptyState.setVisibility(View.VISIBLE);
                    rvBrowseSchemes.setVisibility(View.GONE);
                } else {
                    llBrowseEmptyState.setVisibility(View.GONE);
                    rvBrowseSchemes.setVisibility(View.VISIBLE);
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
