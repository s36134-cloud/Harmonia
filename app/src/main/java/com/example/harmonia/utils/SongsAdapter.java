package com.example.harmonia.utils;

import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.harmonia.ChatSummary;
import com.example.harmonia.R;
import com.example.harmonia.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private List<Song> songList;
    private OnSongClickListener listener;
    private int layoutResId; // המשתנה שקובע איזה XML להציג

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongsAdapter(List<Song> songList, OnSongClickListener listener, int layoutResId) {
        this.songList = songList;
        this.listener = listener;
        this.layoutResId = layoutResId;
    }

    public void updateList(List<Song> newList) {
        this.songList = newList;
        notifyDataSetChanged();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView namesong, artist, genresong;
        public ImageView songimage, sharesong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            namesong = itemView.findViewById(R.id.namesong);
            artist = itemView.findViewById(R.id.artist);
            songimage = itemView.findViewById(R.id.songimage);
            // עשויים להיות חסרים ב-XML המצומצם
            genresong = itemView.findViewById(R.id.genresong);
            sharesong = itemView.findViewById(R.id.sharesong);
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.namesong.setText(song.getName());
        holder.artist.setText(song.getArtist());

        // בדיקות Null לרכיבים אופציונליים
        if (holder.genresong != null) {
            holder.genresong.setText(song.getGenre());
        }

        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + song.getId() + ".jpg";

        if (holder.songimage != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.songimage);

            holder.songimage.setAlpha(song.isSelectedsong() ? 0.5f : 1.0f);
        }

        // לחיצה על כפתור השיתוף (אם קיים ב-Layout)
        if (holder.sharesong != null) {
            holder.sharesong.setOnClickListener(v -> showShareDialog(v, song));
        }

        // --- הוספת אפשרות הלחיצה על כל השורה ---
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            // עדכון מצב בחירה (ויזואלי)
            song.setSelected(!song.isSelectedsong());
            if (holder.songimage != null) {
                holder.songimage.setAlpha(song.isSelectedsong() ? 0.5f : 1.0f);
            }

            // הפעלת הליסנר שקיבלנו מה-Activity
            if (listener != null) {
                listener.onSongClick(song);
            }
        });
    }

    private void showShareDialog(View v, Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_select_chat, null);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        RecyclerView rvChats = dialogView.findViewById(R.id.select_chat);
        rvChats.setLayoutManager(new LinearLayoutManager(v.getContext()));
        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("chats").whereArrayContains("users", currentUserId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<ChatSummary> chatList = new ArrayList<>();
            ChatSummaryAdapter adapter = new ChatSummaryAdapter(chatList);
            rvChats.setAdapter(adapter);
            adapter.setOnItemClickListener(chat -> {
                sendSongToChat(chat.chatId, song, v.getContext());
                alertDialog.dismiss();
            });
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                ChatSummary chat = doc.toObject(ChatSummary.class);
                if (chat != null) {
                    chat.chatId = doc.getId();
                    chatList.add(chat);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        alertDialog.show();
    }

    private void sendSongToChat(String chatId, Song song, android.content.Context context) {
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", FirebaseAuth.getInstance().getUid());
        message.put("text", song.getName());
        message.put("timestamp", FieldValue.serverTimestamp());
        message.put("songId", song.getId());
        message.put("type", "song");
        message.put("artist", song.getArtist());
        message.put("genre", song.getGenre());

        FirebaseFirestore.getInstance().collection("chats").document(chatId).collection("messages").add(message)
                .addOnSuccessListener(ref -> Toast.makeText(context, "Song shared!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() { return songList != null ? songList.size() : 0; }
}