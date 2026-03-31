package com.example.harmonia.utils;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.harmonia.Book;
import com.example.harmonia.ChatSummary;
import com.example.harmonia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;
    private int layoutResId; // הפרמטר שקובע איזה XML להציג
    private static final String TAG = "BooksAdapter";

    // ה-Interface המעודכן שמחזיר אובייקט ספר
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    // בנאי מעודכן עם layoutResId
    public BooksAdapter(List<Book> bookList, OnBookClickListener listener, int layoutResId) {
        this.bookList = bookList;
        this.listener = listener;
        this.layoutResId = layoutResId;
    }

    public void updateList(List<Book> newList) {
        this.bookList = newList;
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView namebook, author, genrebook, minage;
        public ImageView bookimage, sharebook;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            namebook = itemView.findViewById(R.id.namebook);
            author = itemView.findViewById(R.id.author);
            bookimage = itemView.findViewById(R.id.bookimage);
            genrebook = itemView.findViewById(R.id.genrebook);
            minage = itemView.findViewById(R.id.minage);
            sharebook = itemView.findViewById(R.id.sharebook);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // שימוש ב-Layout שהועבר בבנאי
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.namebook.setText(book.getName());
        holder.author.setText(book.getAuthor());

        // בדיקות Null לרכיבים שלא קיימים ב-Compact Mode
        if (holder.genrebook != null) holder.genrebook.setText(book.getGenre());
        if (holder.minage != null) holder.minage.setText(book.getMinage() + "+");

        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/books/" + book.getId() + ".jpg";

        if (holder.bookimage != null) {
            holder.bookimage.setVisibility(View.INVISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.bookimage.setVisibility(View.INVISIBLE);
                            return true;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.bookimage.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(holder.bookimage);
        }

        holder.itemView.setAlpha(book.isSelectedbook() ? 0.5f : 1.0f);

        if (holder.sharebook != null) {
            holder.sharebook.setOnClickListener(v -> showShareDialog(v, book));
        }

        // טיפול בלחיצה על השורה כולה
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Book currentBook = bookList.get(currentPosition);

            // 1. תמיד נהפוך את מצב הבחירה (זה מה שהיה חסר!)
            currentBook.setSelected(!currentBook.isSelectedbook());

            // 2. תמיד נעדכן את הנראות של השורה (שקיפות)
            notifyItemChanged(currentPosition);

            if (listener != null) {
                // 3. אם יש ליסנר, נפעיל אותו כדי שה-Activity יעדכן את כפתור ה-Done
                listener.onBookClick(currentBook);
            } else {
                // 4. אם אין ליסנר, נעדכן את הכפתור ישירות
                updateDoneButtonVisibility(v);
            }
        });
    }

    private void showShareDialog(View v, Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_select_chat, null);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        RecyclerView rvChats = dialogView.findViewById(R.id.select_chat);
        rvChats.setLayoutManager(new LinearLayoutManager(v.getContext()));
        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chats").whereArrayContains("users", currentUserId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<ChatSummary> chatList = new ArrayList<>();
            ChatSummaryAdapter adapter = new ChatSummaryAdapter(chatList);
            rvChats.setAdapter(adapter);
            adapter.setOnItemClickListener(chat -> {
                sendBookToChat(chat.chatId, book, v.getContext());
                alertDialog.dismiss();
            });
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                ChatSummary chat = doc.toObject(ChatSummary.class);
                if (chat != null) {
                    chat.chatId = doc.getId();
                    chatList.add(chat);
                    String partnerId = "";
                    if (chat.users != null) {
                        for (String uid : chat.users) {
                            if (!uid.equals(currentUserId)) { partnerId = uid; break; }
                        }
                    }
                    if (!partnerId.isEmpty()) {
                        db.collection("users").document(partnerId).get().addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                chat.partnerName = userDoc.getString("nickname");
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
        alertDialog.show();
    }

    private void sendBookToChat(String chatId, Book book, android.content.Context context) {
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", FirebaseAuth.getInstance().getUid());
        message.put("text", book.getName());
        message.put("timestamp", FieldValue.serverTimestamp());
        message.put("bookId", book.getId());
        message.put("type", "book");
        message.put("author", book.getAuthor());
        message.put("genre", book.getGenre());
        FirebaseFirestore.getInstance().collection("chats").document(chatId).collection("messages").add(message)
                .addOnSuccessListener(ref -> Toast.makeText(context, "Book shared!", Toast.LENGTH_SHORT).show());
    }

    private void updateDoneButtonVisibility(View v) {
        boolean hasSelection = false;
        if (bookList != null) {
            for (Book b : bookList) { if (b.isSelectedbook()) { hasSelection = true; break; } }
        }
        View button = v.getRootView().findViewById(R.id.btnDoneBooks);
        if (button != null) button.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() { return bookList != null ? bookList.size() : 0; }
}