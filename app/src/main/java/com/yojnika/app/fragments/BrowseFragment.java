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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.yojnika.app.R;
import com.yojnika.app.activities.SchemeDetailActivity;
import com.yojnika.app.adapters.SchemeAdapter;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.viewmodels.BrowseViewModel;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment implements SchemeAdapter.OnSchemeClickListener {

    private EditText etSearchQuery;
    private ImageView btnClearSearch;
    private ChipGroup chipGroupCategoryTabs;
    private Chip chipStateFilter;
    private Chip chipTypeFilter;
    private Chip chipCategoryFilter;
    private Chip chipResetFilter;
    private TextView tvBrowseCount;
    private RecyclerView rvBrowseSchemes;
    private LinearLayout llBrowseEmptyState;

    private LinearLayout llBrowsePagination;
    private MaterialButton btnBrowsePrevious, btnBrowseNext;
    private TextView tvBrowsePageIndicator;

    private SchemeRepository repository;
    private BrowseViewModel viewModel;
    private SchemeAdapter adapter;
    private final List<Scheme> schemeList = new ArrayList<>();

    private String selectedState = "All India";
    private String selectedType = "All Types";
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        repository = SchemeRepository.getInstance(requireContext());
        viewModel = new ViewModelProvider(this).get(BrowseViewModel.class);

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

        setupRecyclerView();
        setupCategoryTabs();
        setupSearchAndFilters();
        setupPaginationListeners();
        observeViewModel();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadSchemes();
    }

    private void setupRecyclerView() {
        rvBrowseSchemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchemeAdapter(requireContext(), schemeList, false, this);
        rvBrowseSchemes.setAdapter(adapter);
    }

    private void setupCategoryTabs() {
        if (chipGroupCategoryTabs != null) {
            chipGroupCategoryTabs.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipTabEducation) {
                    selectedCategory = "Education";
                } else if (checkedId == R.id.chipTabHealth) {
                    selectedCategory = "Health";
                } else if (checkedId == R.id.chipTabAgriculture) {
                    selectedCategory = "Agriculture";
                } else if (checkedId == R.id.chipTabWomen) {
                    selectedCategory = "Women";
                } else if (checkedId == R.id.chipTabEmployment) {
                    selectedCategory = "Employment";
                } else if (checkedId == R.id.chipTabHousing) {
                    selectedCategory = "Housing";
                } else {
                    selectedCategory = "All";
                }
                updateFilterChipsUI();
                performSearchAndFilter();
            });
        }
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

        chipStateFilter.setOnClickListener(v -> showStateFilterDialog());
        chipTypeFilter.setOnClickListener(v -> showTypeFilterDialog());
        chipCategoryFilter.setOnClickListener(v -> showCategoryFilterDialog());

        chipResetFilter.setOnClickListener(v -> {
            selectedState = "All India";
            selectedType = "All Types";
            selectedCategory = "All";
            etSearchQuery.setText("");
            if (chipGroupCategoryTabs != null) {
                chipGroupCategoryTabs.check(R.id.chipTabAll);
            }
            updateFilterChipsUI();
            performSearchAndFilter();
        });
    }

    private void setupPaginationListeners() {
        btnBrowsePrevious.setOnClickListener(v -> viewModel.previousPage());
        btnBrowseNext.setOnClickListener(v -> viewModel.nextPage());
    }

    private void observeViewModel() {
        viewModel.getPagedSchemes().observe(getViewLifecycleOwner(), schemes -> {
            schemeList.clear();
            if (schemes != null && !schemes.isEmpty()) {
                schemeList.addAll(schemes);
                adapter.updateData(schemeList);
                rvBrowseSchemes.setVisibility(View.VISIBLE);
                llBrowseEmptyState.setVisibility(View.GONE);
                llBrowsePagination.setVisibility(View.VISIBLE);

                tvBrowseCount.setText("Showing page " + viewModel.getCurrentPage() + " (" + schemeList.size() + " schemes on page)");
                tvBrowsePageIndicator.setText("Page " + viewModel.getCurrentPage());
                btnBrowsePrevious.setEnabled(viewModel.getCurrentPage() > 1);
                btnBrowseNext.setEnabled(schemeList.size() >= 20);
            } else {
                adapter.updateData(new ArrayList<>());
                rvBrowseSchemes.setVisibility(View.GONE);
                llBrowseEmptyState.setVisibility(View.VISIBLE);
                tvBrowseCount.setText("No matching schemes found");

                if (viewModel.getCurrentPage() > 1) {
                    btnBrowseNext.setEnabled(false);
                } else {
                    llBrowsePagination.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showStateFilterDialog() {
        String[] states = getResources().getStringArray(R.array.indian_states_array);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_state_title)
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
                .setTitle(R.string.filter_type_title)
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
                .setTitle(R.string.filter_category_title)
                .setItems(categories, (dialog, which) -> {
                    selectedCategory = categories[which];
                    updateFilterChipsUI();
                    performSearchAndFilter();
                })
                .show();
    }

    private void updateFilterChipsUI() {
        String allText = getString(R.string.all);
        chipStateFilter.setText(getString(R.string.state_label, (selectedState.equals(getString(R.string.filter_all)) || selectedState.equals("All India")) ? allText : selectedState));
        chipTypeFilter.setText(getString(R.string.type_label, (selectedType.equals(getString(R.string.filter_all)) || selectedType.equals("All Types")) ? allText : selectedType));
        chipCategoryFilter.setText(getString(R.string.category_label, (selectedCategory.equals(getString(R.string.filter_all)) || selectedCategory.equals("All Categories")) ? allText : selectedCategory));
    }

    private void performSearchAndFilter() {
        String query = etSearchQuery.getText().toString().trim();
        viewModel.setFilters(query, selectedState, selectedType, selectedCategory);
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
