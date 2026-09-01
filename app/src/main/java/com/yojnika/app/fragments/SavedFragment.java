package com.yojnika.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yojnika.app.R;
import com.yojnika.app.activities.SchemeDetailActivity;
import com.yojnika.app.adapters.SavedSchemeAdapter;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment implements SavedSchemeAdapter.OnSavedSchemeClickListener {

    private TextView tvSavedCount;
    private RecyclerView rvSavedSchemes;
    private LinearLayout llSavedEmptyState;

    private SchemeRepository repository;
    private SavedSchemeAdapter adapter;
    private final List<Scheme> savedSchemes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        repository = SchemeRepository.getInstance(requireContext());

        tvSavedCount = view.findViewById(R.id.tvSavedCount);
        rvSavedSchemes = view.findViewById(R.id.rvSavedSchemes);
        llSavedEmptyState = view.findViewById(R.id.llSavedEmptyState);

        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedSchemes();
    }

    private void setupRecyclerView() {
        rvSavedSchemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SavedSchemeAdapter(requireContext(), savedSchemes, this);
        rvSavedSchemes.setAdapter(adapter);
    }

    private void loadSavedSchemes() {
        repository.getBookmarkedSchemes(list -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                savedSchemes.clear();
                if (list != null) {
                    savedSchemes.addAll(list);
                }
                adapter.updateData(savedSchemes);

                tvSavedCount.setText(savedSchemes.size() + " bookmarked schemes");

                if (savedSchemes.isEmpty()) {
                    llSavedEmptyState.setVisibility(View.VISIBLE);
                    rvSavedSchemes.setVisibility(View.GONE);
                } else {
                    llSavedEmptyState.setVisibility(View.GONE);
                    rvSavedSchemes.setVisibility(View.VISIBLE);
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
    public void onRemoveBookmark(Scheme scheme, int position) {
        repository.toggleBookmark(scheme.getSchemeId(), isBookmarked -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                savedSchemes.remove(position);
                adapter.notifyItemRemoved(position);
                tvSavedCount.setText(savedSchemes.size() + " bookmarked schemes");

                if (savedSchemes.isEmpty()) {
                    llSavedEmptyState.setVisibility(View.VISIBLE);
                    rvSavedSchemes.setVisibility(View.GONE);
                }

                Toast.makeText(requireContext(), R.string.scheme_removed, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
