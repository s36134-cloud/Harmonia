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

    // --- הוספה: ממשק שמאפשר ל-Activity לדעת על לחיצות ---
    public interface OnGenreClickListener {
        void onGenreClick();
    }
    private OnGenreClickListener listener;

    // בנאי מעודכן שמקבל את הליסנר
    public GenresAdapter(List<String> genreNames, OnGenreClickListener listener) {
        this.genreNames = genreNames;
        this.checkedPositions = new HashSet<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String genreName = genreNames.get(position);
        holder.genreText.setText(genreName);

        // עיצוב לפי מצב בחירה
        if (checkedPositions.contains(position)) {
            holder.heartIcon.setColorFilter(android.graphics.Color.parseColor("#CE93D8"));
        } else {
            holder.heartIcon.setColorFilter(android.graphics.Color.parseColor("#E1BEE7"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (checkedPositions.contains(position)) {
                checkedPositions.remove(position);
            } else {
                checkedPositions.add(position);
                // האנימציה היפה שלך
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
            }

            notifyItemChanged(position);

            // --- הפעלת הליסנר כדי לעדכן את ה-Activity ---
            if (listener != null) {
                listener.onGenreClick();
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