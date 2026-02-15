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

import com.bumptech.glide.Glide;
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
            holder.createdTextView.setText(timestampToString(post.getCreatedAt()));
        } else {
            holder.createdTextView.setText("No date");
        }

        // טעינת התמונה מהפוסט
        String imageUrl = post.getImageUrl();
        Log.d(TAG, "Post #" + position + " imageUrl: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // אם יש תמונה - תציג אותה
            holder.postImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.transparent_placeholder) // 👈 שקוף!
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.postImageView);
        } else {
            // אם אין תמונה - תסתיר את ה-ImageView
            holder.postImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (posts != null) {
            count = posts.size();
        }
        Log.d(TAG, "getItemCount: returning " + count + " items.");
        return count;
    }

    private String timestampToString(Timestamp timestamp) {
        Date messageDate = timestamp.toDate();
        boolean isToday = DateUtils.isToday(messageDate.getTime());

        SimpleDateFormat fmt;
        if (isToday) {
            fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        } else {
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