package com.example.harmonia.utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.harmonia.Conv;
import com.example.harmonia.R;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ConvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_SONG = 3;
    private static final int TYPE_BOOK = 4;

    private List<Conv> messageList;

    public ConvAdapter(List<Conv> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Conv conv = messageList.get(position);
        if ("song".equals(conv.getType())) return TYPE_SONG;
        if ("book".equals(conv.getType())) return TYPE_BOOK;
        if (conv.getSenderId().equals(FirebaseAuth.getInstance().getUid())) return TYPE_SENT;
        return TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SONG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
            return new SongViewHolder(view);
        } else if (viewType == TYPE_BOOK) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book, parent, false);
            return new BookViewHolder(view);
        } else if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Conv message = messageList.get(position);
        boolean isMine = message.getSenderId().equals(FirebaseAuth.getInstance().getUid());

        if (holder instanceof SongViewHolder) {
            SongViewHolder songHolder = (SongViewHolder) holder;

            songHolder.name.setText(message.getMessage());
            songHolder.artist.setText(message.getArtist());
            songHolder.genre.setText(message.getGenre());
            if (songHolder.share != null) songHolder.share.setVisibility(View.GONE);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) songHolder.card.getLayoutParams();
            params.gravity = isMine ? Gravity.END : Gravity.START;
            songHolder.card.setLayoutParams(params);

            String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/"
                    + message.getSongId() + ".jpg";
            Glide.with(songHolder.itemView.getContext())
                    .load(imageUrl).centerCrop().into(songHolder.img);

        } else if (holder instanceof BookViewHolder) {
            BookViewHolder bookHolder = (BookViewHolder) holder;

            bookHolder.name.setText(message.getMessage());
            bookHolder.author.setText(message.getAuthor());
            bookHolder.genre.setText(message.getGenre());
            if (bookHolder.share != null) bookHolder.share.setVisibility(View.GONE);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bookHolder.card.getLayoutParams();
            params.gravity = isMine ? Gravity.END : Gravity.START;
            bookHolder.card.setLayoutParams(params);

            String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/books/"
                    + message.getBookId() + ".jpg";
            Glide.with(bookHolder.itemView.getContext())
                    .load(imageUrl).centerCrop().into(bookHolder.img);

        } else {
            MessageViewHolder msgHolder = (MessageViewHolder) holder;
            msgHolder.messageText.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
        }
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView name, artist, genre;
        ImageView img, share;
        androidx.cardview.widget.CardView card;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.namesong);
            artist = itemView.findViewById(R.id.artist);
            genre = itemView.findViewById(R.id.genresong);
            img = itemView.findViewById(R.id.songimage);
            share = itemView.findViewById(R.id.sharesong);
            card = itemView.findViewById(R.id.songcard);
        }
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView name, author, genre;
        ImageView img, share;
        androidx.cardview.widget.CardView card;
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.namebook);
            author = itemView.findViewById(R.id.author);
            genre = itemView.findViewById(R.id.genrebook);
            img = itemView.findViewById(R.id.bookimage);
            share = itemView.findViewById(R.id.sharebook);
            card = itemView.findViewById(R.id.bookcard);
        }
    }
}