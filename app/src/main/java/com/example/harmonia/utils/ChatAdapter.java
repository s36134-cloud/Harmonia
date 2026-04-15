package com.example.harmonia.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.harmonia.ConvActivity;
import com.example.harmonia.R;
import com.example.harmonia.RecommendedUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<RecommendedUser> matches;

    public ChatAdapter(List<RecommendedUser> matches) {
        this.matches = matches;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedUser user = matches.get(position);

        String displayName = (user.display_name != null && !user.display_name.isEmpty()) ? user.display_name : "User";
        holder.tvName.setText(displayName);
        holder.tvReason.setText(user.reason);

        if (holder.tvScore != null) {
            holder.tvScore.setVisibility(View.GONE);
        }
        if (holder.btnStartChat != null) {
            holder.btnStartChat.setVisibility(View.GONE);
        }

        // לחיצה על כל הכרטיס
        holder.itemView.setOnClickListener(v -> {
            String finalId = user.user_id;

            if (finalId == null || finalId.isEmpty()) {
                Log.e("NAV_DEBUG", "Error: user_id is null for " + displayName);
                Toast.makeText(v.getContext(), "שגיאה: לא ניתן ליצור קשר", Toast.LENGTH_SHORT).show();
                return;
            }

            // יצירת השיחה ב-Firestore ואז מעבר לדף ההודעות
            createChatIfNeeded(v.getContext(), finalId, displayName);
        });
        String profileUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/profiles/"
                + user.user_id + ".jpg";

        Glide.with(holder.itemView.getContext())
                .load(profileUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.UserProfile);

    }

    /**
     * יוצרת מסמך שיחה ב-Firestore אם לא קיים,
     * ואז מנווטת ל-ConvActivity ומסגרת את SearchChatActivity.
     */
    private void createChatIfNeeded(android.content.Context context, String otherUserId, String displayName) {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) {
            Toast.makeText(context, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        // ID ייחודי ועקבי לכל זוג – לא תלוי בסדר
        String chatId = myUid.compareTo(otherUserId) < 0
                ? myUid + "_" + otherUserId
                : otherUserId + "_" + myUid;

        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        // יצירת מסמך שיחה חדש
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("users", Arrays.asList(myUid, otherUserId));
                        chatData.put("lastMessage", "");
                        chatData.put("timestamp", FieldValue.serverTimestamp());

                        FirebaseFirestore.getInstance()
                                .collection("chats")
                                .document(chatId)
                                .set(chatData)
                                .addOnSuccessListener(unused -> {
                                    Log.d("NAV_DEBUG", "Chat created: " + chatId);
                                    navigateToConv(context, otherUserId, displayName);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("NAV_DEBUG", "Failed to create chat: " + e.getMessage());
                                    Toast.makeText(context, "שגיאה ביצירת השיחה", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // שיחה כבר קיימת – רק נווט
                        Log.d("NAV_DEBUG", "Chat already exists: " + chatId);
                        navigateToConv(context, otherUserId, displayName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NAV_DEBUG", "Error checking chat: " + e.getMessage());
                    Toast.makeText(context, "שגיאה בבדיקת השיחה", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * מנווטת ל-ConvActivity וסוגרת את SearchChatActivity.
     */
    private void navigateToConv(android.content.Context context, String otherUserId, String displayName) {
        Log.d("NAV_DEBUG", "Navigating to ConvActivity with: " + displayName);

        Intent intent = new Intent(context, ConvActivity.class);
        intent.putExtra("userId", otherUserId);
        intent.putExtra("userName", displayName);
        context.startActivity(intent);

        // סגירת SearchChatActivity – המשתמש לא יוכל לחזור אליו בלחיצת Back
        ((Activity) context).finish();
    }

    @Override
    public int getItemCount() {
        return matches != null ? matches.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScore, tvReason;
        Button btnStartChat;

        ImageView UserProfile;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_partner_name);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvReason = itemView.findViewById(R.id.tv_reason);
            btnStartChat = itemView.findViewById(R.id.btn_start_chat);
            UserProfile = itemView.findViewById(R.id.profile_image_chat);

        }
    }
}