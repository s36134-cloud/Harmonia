package com.example.harmonia.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.harmonia.Song;

import java.util.List;

public class CombinedMediaAdapter extends RecyclerView.Adapter<CombinedMediaAdapter.MediaViewHolder> {

    private List<Object> mediaList;
    private OnMediaClickListener listener;
    private static final String TAG = "CombinedMediaAdapter";

    public interface OnMediaClickListener {
        void onMediaClick(String imageUrl);
    }

    public CombinedMediaAdapter(List<Object> mediaList, OnMediaClickListener listener) {
        this.mediaList = mediaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Object item = mediaList.get(position);
        String imageUrl = "";
        String name = "";
        String creator = "";

        if (item instanceof Book) {
            Book book = (Book) item;
            name = book.getName();
            creator = book.getAuthor();
            imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/books/" + book.getId() + ".jpg";

            holder.genreText.setText(book.getGenre());
            holder.minageText.setText(String.valueOf(book.getMinage()) + "+");
            holder.genreText.setVisibility(View.VISIBLE);
            holder.minageText.setVisibility(View.VISIBLE);

        } else if (item instanceof Song) {
            Song song = (Song) item;
            name = song.getName();
            creator = song.getArtist();
            imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + song.getId() + ".jpg";

            holder.genreText.setVisibility(View.GONE);
            holder.minageText.setVisibility(View.GONE);
        }

        holder.nameText.setText(name);
        holder.creatorText.setText(creator);

        Log.d(TAG, "Loading image: " + imageUrl);

        // הסתר את התמונה תחילה ונקה כל תמונה קודמת
        holder.imageView.setVisibility(View.INVISIBLE);
        holder.imageView.setImageDrawable(null);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        // במקרה של שגיאה - השאר מוסתר!
                        holder.imageView.setVisibility(View.INVISIBLE);
                        return true; // אל תציג כלום
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        // הצג רק כשהתמונה מוכנה!
                        holder.imageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(holder.imageView);

        String finalImageUrl = imageUrl;
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMediaClick(finalImageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList != null ? mediaList.size() : 0;
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView creatorText;
        ImageView imageView;
        TextView genreText;
        TextView minageText;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.namebook);
            creatorText = itemView.findViewById(R.id.author);
            imageView = itemView.findViewById(R.id.bookimage);
            genreText = itemView.findViewById(R.id.genrebook);
            minageText = itemView.findViewById(R.id.minage);
        }
    }
}