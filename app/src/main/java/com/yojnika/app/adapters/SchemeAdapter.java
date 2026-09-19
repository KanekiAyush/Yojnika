package com.yojnika.app.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yojnika.app.R;
import com.yojnika.app.models.Scheme;

import java.util.ArrayList;
import java.util.List;

public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.SchemeViewHolder> {

    public interface OnSchemeClickListener {
        void onSchemeClick(Scheme scheme);
        void onBookmarkClick(Scheme scheme, int position);
    }

    private final Context context;
    private final List<Scheme> schemeList;
    private final boolean showMatchScore;
    private final OnSchemeClickListener listener;

    public SchemeAdapter(Context context, List<Scheme> schemeList, boolean showMatchScore, OnSchemeClickListener listener) {
        this.context = context;
        this.schemeList = schemeList != null ? schemeList : new ArrayList<>();
        this.showMatchScore = showMatchScore;
        this.listener = listener;
    }

    public void updateData(List<Scheme> newSchemes) {
        this.schemeList.clear();
        if (newSchemes != null) {
            this.schemeList.addAll(newSchemes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SchemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scheme, parent, false);
        return new SchemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeViewHolder holder, int position) {
        Scheme scheme = schemeList.get(position);

        holder.tvSchemeName.setText(scheme.getSchemeName());
        holder.tvDescription.setText(scheme.getSchemeDescription());
        holder.tvSchemeType.setText(scheme.getSchemeType());

        // Dynamic Category Tag
        if (scheme.getSchemeCategory() != null && !scheme.getSchemeCategory().trim().isEmpty()) {
            holder.tvCategory.setText(scheme.getSchemeCategory().trim());
        } else {
            String occ = scheme.getEligibleOccupations();
            if (occ != null && occ.contains("Farmer")) {
                holder.tvCategory.setText("Farmer");
            } else if (occ != null && occ.contains("Student")) {
                holder.tvCategory.setText("Student");
            } else if (occ != null && occ.contains("Business")) {
                holder.tvCategory.setText("Business");
            } else if (occ != null && occ.contains("Unemployed")) {
                holder.tvCategory.setText("Youth / Unemployed");
            } else {
                holder.tvCategory.setText("General");
            }
        }

        // Benefits preview
        if (scheme.getBenefits() != null && !scheme.getBenefits().isEmpty()) {
            holder.tvBenefitsPreview.setText(scheme.getBenefits());
            holder.tvBenefitsPreview.setVisibility(View.VISIBLE);
        } else {
            holder.tvBenefitsPreview.setVisibility(View.GONE);
        }

        // Bookmark Icon State
        if (scheme.isBookmarked()) {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
            holder.btnBookmark.setColorFilter(ContextCompat.getColor(context, R.color.primary));
        } else {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark);
            holder.btnBookmark.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
        }

        // Click Listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSchemeClick(scheme);
            }
        });

        holder.btnBookmark.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookmarkClick(scheme, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return schemeList.size();
    }

    static class SchemeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSchemeType;
        TextView tvCategory;
        TextView tvSchemeName;
        TextView tvDescription;
        TextView tvBenefitsPreview;
        ImageView btnBookmark;

        public SchemeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchemeType = itemView.findViewById(R.id.tvItemSchemeType);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvSchemeName = itemView.findViewById(R.id.tvItemSchemeName);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvBenefitsPreview = itemView.findViewById(R.id.tvBenefitsPreview);
            btnBookmark = itemView.findViewById(R.id.btnItemBookmark);
        }
    }
}
