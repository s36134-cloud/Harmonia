package com.example.harmonia.utils;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harmonia.ChatSummary;
import com.example.harmonia.ConvActivity;
import com.example.harmonia.R;
import java.util.List;

public class ChatSummaryAdapter extends RecyclerView.Adapter<ChatSummaryAdapter.ViewHolder> {
    private List<ChatSummary> chatList;

    // 1. הוספת ה-Interface והמשתנה עבור הלחיצה
    public interface OnItemClickListener {
        void onItemClick(ChatSummary chat);
    }

    private OnItemClickListener listener;

    // 2. הפונקציה שהייתה חסרה בשגיאה שלך
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ChatSummaryAdapter(List<ChatSummary> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSummary chat = chatList.get(position);

        // הצגת ה-nickname מה-Firebase (למשל "lilo")
        holder.tvName.setText(chat.partnerName != null ? chat.partnerName : "User");
        holder.tvLastMsg.setText(chat.lastMessage);

        holder.itemView.setOnClickListener(v -> {
            // אם הגדרנו listener (כמו בדיאלוג השיתוף), נפעיל אותו
            if (listener != null) {
                listener.onItemClick(chat);
            } else {
                // אחרת, נתנהג כרגיל ונפתח את ה-Activity (עבור המסך הראשי)
                Intent intent = new Intent(v.getContext(), ConvActivity.class);
                intent.putExtra("userId", chat.partnerId);
                intent.putExtra("userName", chat.partnerName);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_partner_name_summary);
            tvLastMsg = itemView.findViewById(R.id.tv_last_message);
        }
    }
}