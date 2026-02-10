package com.example.harmonia.utils;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private static final String TAG = "PostsAdapter";
    private List<HarmoniaPost> posts;

    public PostsAdapter(List<HarmoniaPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        HarmoniaPost post = posts.get(position);

        Log.d(TAG, "onBindViewHolder: adding post item #" + position);
        holder.titleTextView.setText(post.getTitle());
        holder.descriptionTextView.setText(post.getDescription());
        holder.ownerTextView.setText(post.getOwnerNickname());

        if (post.getCreatedAt() != null) {
            holder.createdTextView.setText(post.getCreatedAt().toDate().toString());
        } else {
            holder.createdTextView.setText("No date");
        }

        holder.createdTextView.setText(timestampToString(post.getCreatedAt()));

        if (position % 2 == 0) {
            // עבור מיקום זוגי (0, 2, 4...)
            holder.postImageView.setImageResource(R.drawable.ic_launcher_foreground);
        } else {
            // עבור מיקום אי-זוגי (1, 3, 5...)
            holder.postImageView.setImageResource(R.drawable.ic_launcher_background);
        }

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(posts!= null){
            count= posts.size();
        }
        Log.d(TAG, "getItemCount:returning" + count + " items.");
        return count;
    }
    private String timestampToString(Timestamp timestamp) {

        Date messageDate = timestamp.toDate();

        boolean isToday = DateUtils.isToday(messageDate.getTime());

        SimpleDateFormat fmt;
        if (isToday) {
            // only show hour:minute, e.g. "14:35"
            fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        } else {
            // only show date, e.g. "Aug 03, 2025"
            fmt = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        }

        return fmt.format(messageDate);
    }




    static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView descriptionTextView;
        TextView createdTextView;
        TextView ownerTextView;
        ImageView postImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_post_title);
            descriptionTextView = itemView.findViewById(R.id.tv_post_description);
            ownerTextView = itemView.findViewById(R.id.tv_post_owner);
            createdTextView = itemView.findViewById(R.id.tv_post_created_at);
            postImageView = itemView.findViewById(R.id.iv_post_image);
        }


    }
}


