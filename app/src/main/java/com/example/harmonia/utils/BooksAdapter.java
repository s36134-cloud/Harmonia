package com.example.harmonia.utils;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private static final String TAG = "BooksAdapter";

    public interface OnBookClickListener {
        void onBookClick(String imageUrl);
    }

    public BooksAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    public void updateList(List<Book> newList) {
        this.bookList = newList;
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView namebook;
        public TextView author;
        public ImageView bookimage;
        public TextView genrebook;
        public TextView minage;
        public ImageView sharebook;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.namebook.setText(book.getName());
        holder.author.setText(book.getAuthor());
        holder.genrebook.setText(book.getGenre());
        holder.minage.setText(book.getMinage() + "+");

        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/books/" + book.getId() + ".jpg";

        holder.bookimage.setVisibility(View.INVISIBLE);
        holder.bookimage.setImageDrawable(null);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.bookimage.setVisibility(View.INVISIBLE);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.bookimage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(holder.bookimage);

        holder.itemView.setAlpha(book.isSelectedbook() ? 0.5f : 1.0f);

        // כפתור שיתוף
        holder.sharebook.setOnClickListener(v -> showShareDialog(v, book));

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (listener != null) {
                listener.onBookClick(imageUrl);
                return;
            }

            Book currentBook = bookList.get(currentPosition);
            currentBook.setSelected(!currentBook.isSelectedbook());
            notifyItemChanged(currentPosition);
            updateDoneButtonVisibility(v);
        });
    }

    private void showShareDialog(View v, Book book) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_select_chat, null);
        bottomSheetDialog.setContentView(dialogView);

        RecyclerView rvChats = dialogView.findViewById(R.id.select_chat);
        rvChats.setLayoutManager(new LinearLayoutManager(v.getContext()));

        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("chats")
                .whereArrayContains("users", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ChatSummary> chatList = new ArrayList<>();
                    ChatSummaryAdapter adapter = new ChatSummaryAdapter(chatList);
                    rvChats.setAdapter(adapter);

                    adapter.setOnItemClickListener(chat -> {
                        sendBookToChat(chat.chatId, book, v.getContext());
                        bottomSheetDialog.dismiss();
                    });

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatSummary chat = doc.toObject(ChatSummary.class);
                        if (chat != null) {
                            chat.chatId = doc.getId();
                            chatList.add(chat);

                            String partnerId = "";
                            if (chat.users != null) {
                                for (String uid : chat.users) {
                                    if (!uid.equals(currentUserId)) {
                                        partnerId = uid;
                                        break;
                                    }
                                }
                            }

                            if (!partnerId.isEmpty()) {
                                db.collection("users").document(partnerId).get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String nameFromDB = userDoc.getString("nickname");
                                                chat.partnerName = (nameFromDB != null) ? nameFromDB : "Unknown";
                                                chat.partnerId = userDoc.getId();
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                });

        bottomSheetDialog.show();
    }

    private void sendBookToChat(String chatId, Book book, android.content.Context context) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("text", book.getName());
        message.put("timestamp", FieldValue.serverTimestamp());
        message.put("bookId", book.getId());
        message.put("type", "book");
        message.put("author", book.getAuthor());
        message.put("genre", book.getGenre());

        db.collection("chats").document(chatId).collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Book shared!", Toast.LENGTH_SHORT).show();

                    db.collection("chats").document(chatId)
                            .update("lastMessage", "Shared a book: " + book.getName(),
                                    "timestamp", FieldValue.serverTimestamp());
                })
                .addOnFailureListener(e -> Log.e("BooksAdapter", "Error sharing book", e));
    }

    private void updateDoneButtonVisibility(View v) {
        boolean hasSelection = false;
        if (bookList != null) {
            for (Book b : bookList) {
                if (b.isSelectedbook()) {
                    hasSelection = true;
                    break;
                }
            }
        }

        View button = v.getRootView().findViewById(R.id.btnDoneBooks);
        if (button != null) {
            button.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }
}