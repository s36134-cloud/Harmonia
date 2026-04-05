package com.example.harmonia.utils;

import android.content.Context;
import android.content.Intent;
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
import com.example.harmonia.CommentsActivity;
import com.example.harmonia.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;



import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        Log.d(TAG, "onBindViewHolder: adding post item #" + position);
        holder.titleTextView.setText(post.getTitle());
        holder.descriptionTextView.setText(post.getDescription());
        holder.ownerTextView.setText(post.getOwnerNickname());

        // הצגת מספרי לייקים ותגובות
        holder.likeCountTextView.setText(String.valueOf(post.getLikesCount()));
        holder.commentCountTextView.setText(String.valueOf(post.getCommentsCount()));

        // --- לוגיקת לייק ויזואלית ---
        // בדיקה אם המשתמש הנוכחי נמצא בתוך רשימת הלייקים של הפוסט
        if (post.getLikedBy() != null && post.getLikedBy().containsKey(currentUserId)) {
            holder.likeImageView.setImageResource(R.drawable.ic_heart_filled); // לב אדום/מלא
        } else {
            holder.likeImageView.setImageResource(R.drawable.ic_heart_outline); // לב ריק
        }

        // לחיצה על לייק
        String finalCurrentUserId = currentUserId; // משתנה סופי עבור ה-Lambda
        holder.likeImageView.setOnClickListener(v -> {
            // כאן תקראי לפונקציה שתעדכן את ה-Firebase (נסדר אותה בהמשך)
            toggleLike(post, finalCurrentUserId);
        });

        // --- לחיצה על תגובות ---
        holder.commentImageView.setOnClickListener(v -> {
            // מעבר למסך תגובות ושליחת ה-ID של הפוסט
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("POST_ID", post.getPostId());
            context.startActivity(intent);
        });

        // --- טיפול בתאריך ---
        if (post.getCreatedAt() != null) {
            holder.createdTextView.setText(timestampToString(post.getCreatedAt()));
        } else {
            holder.createdTextView.setText("No date");
        }

        // --- טעינת תמונה (הקוד הקיים שלך) ---
        String imageUrl = post.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.transparent_placeholder)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }

        // --- כפתור מחיקה (הקוד הקיים שלך) ---
        if (post.getOwnerUid() != null && post.getOwnerUid().equals(currentUserId)) {
            holder.deleteImageView.setVisibility(View.VISIBLE);
            holder.deleteImageView.setOnClickListener(v -> {
                showDeleteDialog(holder.getAdapterPosition(), holder.itemView.getContext());
            });
        } else {
            holder.deleteImageView.setVisibility(View.GONE);
        }
    }


    private void toggleLike(HarmoniaPost post, String userId) {
        if (userId == null || userId.isEmpty() || post.getPostId() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(post.getPostId());

        // בדיקה אם המשתמש כבר עשה לייק לפי המפה בתוך האובייקט
        boolean isAlreadyLiked = post.getLikedBy() != null && post.getLikedBy().containsKey(userId);

        if (isAlreadyLiked) {
            // המשתמש כבר עשה לייק -> הסרה
            postRef.update(
                    "likesCount", FieldValue.increment(-1),
                    "likedBy." + userId, FieldValue.delete()
            ).addOnSuccessListener(aVoid -> {
                // עדכון האובייקט המקומי כדי שהמסך יתעדכן מיד
                post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
                if (post.getLikedBy() != null) post.getLikedBy().remove(userId);
                notifyDataSetChanged();
            });
        } else {
            // לייק חדש
            postRef.update(
                    "likesCount", FieldValue.increment(1),
                    "likedBy." + userId, true
            ).addOnSuccessListener(aVoid -> {
                // עדכון האובייקט המקומי
                post.setLikesCount(post.getLikesCount() + 1);
                if (post.getLikedBy() == null) post.setLikedBy(new HashMap<>());
                post.getLikedBy().put(userId, true);
                notifyDataSetChanged();
            });
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
        ImageView likeImageView;
        ImageView commentImageView;
        TextView likeCountTextView;
        TextView commentCountTextView;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_post_title);
            descriptionTextView = itemView.findViewById(R.id.tv_post_description);
            ownerTextView = itemView.findViewById(R.id.tv_post_owner);
            createdTextView = itemView.findViewById(R.id.tv_post_created_at);
            postImageView = itemView.findViewById(R.id.iv_post_image);
            deleteImageView = itemView.findViewById(R.id.delete);
            likeImageView = itemView.findViewById(R.id.like);
            likeCountTextView = itemView.findViewById(R.id.like_count);
            commentImageView = itemView.findViewById(R.id.comment);
            commentCountTextView = itemView.findViewById(R.id.comment_count);
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