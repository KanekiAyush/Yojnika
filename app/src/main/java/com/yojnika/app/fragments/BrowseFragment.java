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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    private ChipGroup chipGroupCategoryTabs;
    private Chip chipStateFilter, chipTypeFilter, chipCategoryFilter, chipResetFilter;
    private TextView tvBrowseCount;
    private RecyclerView rvBrowseSchemes;
    private LinearLayout llBrowseEmptyState;
    private MaterialCardView llBrowsePagination;
    private MaterialButton btnBrowsePrevious, btnBrowseNext;
    private TextView tvBrowsePageIndicator;
    private ProgressBar pbBrowseLoading;

    private SchemeRepository repository;
    private SchemeAdapter adapter;
    private final List<Scheme> schemeList = new ArrayList<>();

    private String selectedState = "All India";
    private String selectedType = "All Types";
    private String selectedCategory = "All";
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        repository = SchemeRepository.getInstance(requireContext());

        etSearchQuery = view.findViewById(R.id.etSearchQuery);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        chipGroupCategoryTabs = view.findViewById(R.id.chipGroupCategoryTabs);
        chipStateFilter = view.findViewById(R.id.chipStateFilter);
        chipTypeFilter = view.findViewById(R.id.chipTypeFilter);
        chipCategoryFilter = view.findViewById(R.id.chipCategoryFilter);
        chipResetFilter = view.findViewById(R.id.chipResetFilter);
        tvBrowseCount = view.findViewById(R.id.tvBrowseCount);
        rvBrowseSchemes = view.findViewById(R.id.rvBrowseSchemes);
        llBrowseEmptyState = view.findViewById(R.id.llBrowseEmptyState);
        llBrowsePagination = view.findViewById(R.id.llBrowsePagination);
        btnBrowsePrevious = view.findViewById(R.id.btnBrowsePrevious);
        btnBrowseNext = view.findViewById(R.id.btnBrowseNext);
        tvBrowsePageIndicator = view.findViewById(R.id.tvBrowsePageIndicator);
        pbBrowseLoading = view.findViewById(R.id.pbBrowseLoading);

        setupRecyclerView();
        setupCategoryTabs();
        setupSearchAndFilters();
        setupPaginationListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentPage = 1;
        loadSchemes();
    }

    private void setupRecyclerView() {
        rvBrowseSchemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchemeAdapter(requireContext(), schemeList, false, this);
        rvBrowseSchemes.setAdapter(adapter);
    }

    private void setupCategoryTabs() {
        if (chipGroupCategoryTabs != null) {
            chipGroupCategoryTabs.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipTabEducation) selectedCategory = "Education";
                else if (checkedId == R.id.chipTabHealth) selectedCategory = "Health";
                else if (checkedId == R.id.chipTabAgriculture) selectedCategory = "Agriculture";
                else if (checkedId == R.id.chipTabWomen) selectedCategory = "Women";
                else if (checkedId == R.id.chipTabEmployment) selectedCategory = "Employment";
                else if (checkedId == R.id.chipTabHousing) selectedCategory = "Housing";
                else selectedCategory = "All";
                updateFilterChipsUI();
                currentPage = 1;
                loadSchemes();
            });
        }
    }

    private void setupSearchAndFilters() {
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                currentPage = 1;
                loadSchemes();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> etSearchQuery.setText(""));
        chipStateFilter.setOnClickListener(v -> showStateFilterDialog());
        chipTypeFilter.setOnClickListener(v -> showTypeFilterDialog());
        chipCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());

        chipResetFilter.setOnClickListener(v -> {
            selectedState = "All India";
            selectedType = "All Types";
            selectedCategory = "All";
            etSearchQuery.setText("");
            if (chipGroupCategoryTabs != null) chipGroupCategoryTabs.check(R.id.chipTabAll);
            updateFilterChipsUI();
            currentPage = 1;
            loadSchemes();
        });
    }

    private void setupPaginationListeners() {
        btnBrowsePrevious.setOnClickListener(v -> { if (currentPage > 1) { currentPage--; loadSchemes(); } });
        btnBrowseNext.setOnClickListener(v -> { currentPage++; loadSchemes(); });
    }

    private void loadSchemes() {
        if (pbBrowseLoading != null) pbBrowseLoading.setVisibility(View.VISIBLE);

        String query = etSearchQuery.getText().toString().trim();
        String state = selectedState.equals("All India") ? null : selectedState;
        String type = selectedType.equals("All Types") ? null : selectedType;
        String category = selectedCategory.equals("All") ? null : selectedCategory;

        repository.searchAndFilterSchemesPaged(currentPage, PAGE_SIZE, query, state, type, category, schemes -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (pbBrowseLoading != null) pbBrowseLoading.setVisibility(View.GONE);
                schemeList.clear();
                if (schemes != null && !schemes.isEmpty()) {
                    schemeList.addAll(schemes);
                    adapter.notifyDataSetChanged();
                    rvBrowseSchemes.setVisibility(View.VISIBLE);
                    llBrowseEmptyState.setVisibility(View.GONE);
                    llBrowsePagination.setVisibility(View.VISIBLE);
                    tvBrowseCount.setText("Showing page " + currentPage + " (" + schemes.size() + " schemes)");
                    // Using a placeholder for total pages for now, or just showing Page X
                    tvBrowsePageIndicator.setText(getString(R.string.pagination_page_indicator, currentPage, (schemes.size() < PAGE_SIZE ? currentPage : currentPage + 1)));
                    btnBrowsePrevious.setEnabled(currentPage > 1);
                    btnBrowseNext.setEnabled(schemes.size() >= PAGE_SIZE);
                } else {
                    rvBrowseSchemes.setVisibility(View.GONE);
                    llBrowseEmptyState.setVisibility(View.VISIBLE);
                    tvBrowseCount.setText("No matching schemes found");
                    if (currentPage == 1) llBrowsePagination.setVisibility(View.GONE);
                    else btnBrowseNext.setEnabled(false);
                }
            });
        });
    }

    private void showStateFilterDialog() {
        String[] states = getResources().getStringArray(R.array.indian_states_array);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_state_title)
                .setItems(states, (dialog, which) -> { selectedState = states[which]; updateFilterChipsUI(); currentPage = 1; loadSchemes(); })
                .show();
    }

    private void showTypeFilterDialog() {
        String[] types = getResources().getStringArray(R.array.scheme_type_filter_array);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_type_title)
                .setItems(types, (dialog, which) -> { selectedType = types[which]; updateFilterChipsUI(); currentPage = 1; loadSchemes(); })
                .show();
    }

    private void showCategoryFilterDialog() {
        String[] categories = getResources().getStringArray(R.array.category_filter_array);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_category_title)
                .setItems(categories, (dialog, which) -> { selectedCategory = categories[which]; updateFilterChipsUI(); currentPage = 1; loadSchemes(); })
                .show();
    }

    private void updateFilterChipsUI() {
        String allText = getString(R.string.all);
        chipStateFilter.setText(getString(R.string.state_label, (selectedState.equals(getString(R.string.filter_all)) || selectedState.equals("All India")) ? allText : selectedState));
        chipTypeFilter.setText(getString(R.string.type_label, (selectedType.equals(getString(R.string.filter_all)) || selectedType.equals("All Types")) ? allText : selectedType));
        chipCategoryFilter.setText(getString(R.string.category_label, (selectedCategory.equals(getString(R.string.filter_all)) || selectedCategory.equals("All")) ? allText : selectedCategory));
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
                Toast.makeText(requireContext(), isBookmarked ? R.string.scheme_saved : R.string.scheme_removed, Toast.LENGTH_SHORT).show();
            });
        });
    }
}