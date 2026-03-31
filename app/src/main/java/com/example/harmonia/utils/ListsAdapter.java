package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.harmonia.R;
import com.example.harmonia.UserList;
import java.util.List;

public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.ListViewHolder> {

    private List<UserList> lists;
    private OnListClickListener listener;

    public interface OnListClickListener {
        void onListClick(UserList userList);
    }

    public ListsAdapter(List<UserList> lists, OnListClickListener listener) {
        this.lists = lists;
        this.listener = listener;
    }

    public void updateList(List<UserList> newList) {
        this.lists = newList;
        notifyDataSetChanged();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView listName;
        ImageView listImage;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.list_name);
            listImage = itemView.findViewById(R.id.upload_image);
        }
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_summary, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        UserList userList = lists.get(position);

        holder.listName.setText(userList.getName());

        if (userList.getImageUrl() != null && !userList.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(userList.getImageUrl())
                    .circleCrop()
                    .into(holder.listImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onListClick(userList);
        });
    }

    @Override
    public int getItemCount() { return lists != null ? lists.size() : 0; }
}