package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.harmonia.R;
import com.example.harmonia.Comment;
import com.google.android.material.imageview.ShapeableImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentsAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // וודאי ששם ה-XML של התגובה הבודדת הוא comment.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvUserName.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());

        // המרת ה-timestamp לפורמט זמן קריא (למשל 14:30)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(comment.getTimestamp()));
        holder.tvTime.setText(dateString);

        // טעינת תמונת הפרופיל של המגיב עם Glide
        if (comment.getUserProfileUrl() != null && !comment.getUserProfileUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(comment.getUserProfileUrl())
                    .placeholder(R.drawable.ic_person) // תמונת ברירת מחדל
                    .into(holder.ivUserProfile);
        } else {
            holder.ivUserProfile.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    // ViewHolder שמחזיק את הרכיבים של כל שורה
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivUserProfile;
        TextView tvUserName, tvCommentText, tvTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.commentUserProfile);
            tvUserName = itemView.findViewById(R.id.commentUserName);
            tvCommentText = itemView.findViewById(R.id.commentText);
            tvTime = itemView.findViewById(R.id.commentTime); // אם הוספת שדה זמן ב-XML
        }
    }
}
