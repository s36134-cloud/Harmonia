package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.ViewHolder> {
    private List<String> genreNames;
    private Set<Integer> checkedPositions;
    private OnGenreCheckedListener listener;

    public interface OnGenreCheckedListener {
        void onGenreChecked(String genreName, boolean isChecked);
    }

    public GenresAdapter(List<String> genreNames, OnGenreCheckedListener listener) {
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

        holder.genreCheckBox.setText(genreName);
        holder.genreCheckBox.setChecked(checkedPositions.contains(position));

        holder.genreCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkedPositions.add(position);
            } else {
                checkedPositions.remove(position);
            }

            // שמירה ב-Firebase
            if (listener != null) {
                listener.onGenreChecked(genreName, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreNames.size();
    }

    public List<String> getCheckedGenres() {
        List<String> checkedGenres = new java.util.ArrayList<>();
        for (int position : checkedPositions) {
            checkedGenres.add(genreNames.get(position));
        }
        return checkedGenres;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox genreCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            genreCheckBox = itemView.findViewById(R.id.genreCheckBox);
        }
    }
}