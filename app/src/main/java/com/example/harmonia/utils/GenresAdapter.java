package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.ViewHolder> {

    private List<String> genreNames;
    private Set<Integer> checkedPositions;

    public GenresAdapter(List<String> genreNames) {
        this.genreNames = genreNames;
        this.checkedPositions = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkbox, parent, false); // ← שני את השם אם קראת לו אחרת
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String genreName = genreNames.get(position);
        holder.genreText.setText(genreName);

        if (checkedPositions.contains(position)) {
            holder.heartIcon.setColorFilter(android.graphics.Color.parseColor("#CE93D8"));
            holder.heartIcon.setScaleX(1f);
            holder.heartIcon.setScaleY(1f);
        } else {
            holder.heartIcon.setColorFilter(android.graphics.Color.parseColor("#" +
                    "E1BEE7"));
            holder.heartIcon.setScaleX(1f);
            holder.heartIcon.setScaleY(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (checkedPositions.contains(position)) {
                checkedPositions.remove(position);
                notifyItemChanged(position);
            } else {
                checkedPositions.add(position);
                holder.heartIcon.animate()
                        .scaleX(1.4f)
                        .scaleY(1.4f)
                        .setDuration(200)
                        .withEndAction(() ->
                                holder.heartIcon.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start()
                        ).start();
                holder.heartIcon.setColorFilter(android.graphics.Color.parseColor("#CE93D8"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreNames.size();
    }

    public List<String> getCheckedGenres() {
        List<String> checkedGenres = new ArrayList<>();
        for (int pos : checkedPositions) {
            checkedGenres.add(genreNames.get(pos));
        }
        return checkedGenres;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView heartIcon;
        TextView genreText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            genreText = itemView.findViewById(R.id.genreText);
        }
    }
}