package com.yojnika.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yojnika.app.R;
import com.yojnika.app.models.Scheme;

import java.util.ArrayList;
import java.util.List;

public class SavedSchemeAdapter extends RecyclerView.Adapter<SavedSchemeAdapter.SavedViewHolder> {

    public interface OnSavedSchemeClickListener {
        void onSchemeClick(Scheme scheme);
        void onRemoveBookmark(Scheme scheme, int position);
    }

    private final Context context;
    private final List<Scheme> savedSchemes;
    private final OnSavedSchemeClickListener listener;

    public SavedSchemeAdapter(Context context, List<Scheme> savedSchemes, OnSavedSchemeClickListener listener) {
        this.context = context;
        this.savedSchemes = savedSchemes != null ? savedSchemes : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Scheme> newSchemes) {
        this.savedSchemes.clear();
        if (newSchemes != null) {
            this.savedSchemes.addAll(newSchemes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_scheme, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        Scheme scheme = savedSchemes.get(position);

        holder.tvSchemeName.setText(scheme.getSchemeName());
        holder.tvDescription.setText(scheme.getSchemeDescription());
        holder.tvSchemeType.setText(scheme.getSchemeType());

        if (scheme.getBenefits() != null && !scheme.getBenefits().isEmpty()) {
            holder.tvBenefits.setText(scheme.getBenefits());
            holder.tvBenefits.setVisibility(View.VISIBLE);
        } else {
            holder.tvBenefits.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSchemeClick(scheme);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveBookmark(scheme, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedSchemes.size();
    }

    static class SavedViewHolder extends RecyclerView.ViewHolder {
        TextView tvSchemeType;
        TextView tvSchemeName;
        TextView tvDescription;
        TextView tvBenefits;
        ImageView btnRemove;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchemeType = itemView.findViewById(R.id.tvSavedSchemeType);
            tvSchemeName = itemView.findViewById(R.id.tvSavedSchemeName);
            tvDescription = itemView.findViewById(R.id.tvSavedDescription);
            tvBenefits = itemView.findViewById(R.id.tvSavedBenefits);
            btnRemove = itemView.findViewById(R.id.btnRemoveSaved);
        }
    }
}
