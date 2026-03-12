package com.example.harmonia.utils;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // הוספתי לטובת התראות למשתמש

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.ConvActivity;
import com.example.harmonia.R;
import com.example.harmonia.RecommendedUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<RecommendedUser> matches;

    public ChatAdapter(List<RecommendedUser> matches) {
        this.matches = matches;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // וודאי שקובץ ה-layout אכן נקרא chat.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedUser user = matches.get(position);

        holder.tvName.setText(user.display_name != null ? user.display_name : "משתמש ללא שם");
        holder.tvReason.setText(user.reason);

        // הסתרת שדות שלא תמיד בשימוש בעיצוב הזה
        if (holder.tvScore != null) holder.tvScore.setVisibility(View.GONE);
        if (holder.btnStartChat != null) holder.btnStartChat.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            // ה-ID שמגיע מה-RecommendedUser המתוקן (עם ה-SerializedName)
            String finalId = user.user_id;

            // בדיקת בטיחות: אם ה-ID ריק, לא עוברים מסך
            if (finalId == null || finalId.isEmpty()) {
                Log.e("NAV_DEBUG", "Error: user_id is null for " + user.display_name);
                Toast.makeText(v.getContext(), "שגיאה: לא ניתן ליצור קשר עם משתמש זה", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(v.getContext(), ConvActivity.class);
            intent.putExtra("userId", finalId);
            intent.putExtra("userName", user.display_name);

            Log.d("NAV_DEBUG", "Successfully starting chat with: " + user.display_name + " ID: " + finalId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return matches != null ? matches.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScore, tvReason;
        Button btnStartChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // וודאי שה-IDs האלו קיימים בתוך chat.xml
            tvName = itemView.findViewById(R.id.tv_partner_name);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvReason = itemView.findViewById(R.id.tv_reason);
            btnStartChat = itemView.findViewById(R.id.btn_start_chat);
        }
    }
}