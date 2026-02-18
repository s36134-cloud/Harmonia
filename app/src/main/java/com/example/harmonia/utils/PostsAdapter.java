package com.example.harmonia.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.harmonia.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // בדיקה: האם המשתמש הנוכחי הוא בעל הפוסט?
        if (post.getOwnerUid() != null && post.getOwnerUid().equals(currentUserId)) {
            // המשתמש הוא הבעלים - הצג את הפח
            holder.deleteImageView.setVisibility(View.VISIBLE);

            holder.deleteImageView.setOnClickListener(v -> {
                // קריאה לפונקציית המחיקה (כדאי עם AlertDialog)
                showDeleteDialog(holder.getAdapterPosition(), holder.itemView.getContext());
            });
        } else {
            // המשתמש אינו הבעלים - הסתר את הפח
            holder.deleteImageView.setVisibility(View.GONE);
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

        ImageView deleteImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_post_title);
            descriptionTextView = itemView.findViewById(R.id.tv_post_description);
            ownerTextView = itemView.findViewById(R.id.tv_post_owner);
            createdTextView = itemView.findViewById(R.id.tv_post_created_at);
            postImageView = itemView.findViewById(R.id.iv_post_image);
            deleteImageView = itemView.findViewById(R.id.delete);
        }
    }

    private void showDeleteDialog(int position, Context context) {
        new android.app.AlertDialog.Builder(context)
                .setTitle("Delete post")
                .setMessage("Are you sure you want to delete the post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    HarmoniaPost post = posts.get(position);
                    String docId = post.getPostId();
                    if (docId != null) {
                        FirebaseFirestore.getInstance().collection("posts")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    posts.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "The post has been deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error deleting the post", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}