package com.example.harmonia.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.harmonia.Book;
import com.example.harmonia.R;

import android.view.View;
import android.widget.Toast;

import java.util.List;

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

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView namebook;
        public TextView author;
        public ImageView bookimage;
        public TextView genrebook;
        public TextView minage;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            namebook = itemView.findViewById(R.id.namebook);
            author = itemView.findViewById(R.id.author);
            bookimage = itemView.findViewById(R.id.bookimage);
            genrebook = itemView.findViewById(R.id.genrebook);
            minage = itemView.findViewById(R.id.minage);
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
        holder.minage.setText(String.valueOf(book.getMinage()) + "+");

        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/books/" + book.getId() + ".jpg";

        Log.d(TAG, "onBindViewHolder: image url: " + imageUrl);

        // הסתר ונקה
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

        if (book.isSelectedbook()) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setBackgroundResource(android.R.color.white);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setBackgroundResource(android.R.color.white);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(imageUrl);
                return;
            }

            if (book.isSelectedbook()) {
                book.setSelected(false);
            } else {
                int count = 0;
                for (Book b : bookList) {
                    if (b.isSelectedbook()) count++;
                }

                if (count < 5) {
                    book.setSelected(true);
                } else {
                    Toast.makeText(v.getContext(), "אפשר לבחור עד 4 ספרים בלבד", Toast.LENGTH_SHORT).show();
                }
            }

            notifyItemChanged(position);

            boolean hasSelection = false;
            for (Book b : bookList) {
                if (b.isSelectedbook()) {
                    hasSelection = true;
                    break;
                }
            }

            View button = v.getRootView().findViewById(R.id.btnDoneBooks);
            if (button != null) {
                button.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }
}