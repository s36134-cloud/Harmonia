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
import com.example.harmonia.Song;
import com.example.harmonia.R;

import android.view.View;
import android.widget.Toast;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private List<Song> songList;
    private OnSongClickListener listener;
    private static final String TAG = "SongsAdapter";

    public interface OnSongClickListener {
        void onSongClick(String imageUrl);
    }

    public SongsAdapter(List<Song> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView namesong;
        public TextView artist;
        public ImageView songimage;
        public TextView genresong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            namesong = itemView.findViewById(R.id.namesong);
            artist = itemView.findViewById(R.id.artist);
            songimage = itemView.findViewById(R.id.songimage);
            genresong = itemView.findViewById(R.id.genresong);
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        Log.d(TAG, "onBindViewHolder: song: " + song.getName());

        holder.namesong.setText(song.getName());
        holder.artist.setText(song.getArtist());
        holder.genresong.setText(song.getGenre());

        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + song.getId() + ".jpg";

        // הסתר ונקה
        holder.songimage.setVisibility(View.INVISIBLE);
        holder.songimage.setImageDrawable(null);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.songimage.setVisibility(View.INVISIBLE);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.songimage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(holder.songimage);

        if (song.isSelectedsong()) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setBackgroundResource(android.R.color.white);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setBackgroundResource(android.R.color.white);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(imageUrl);
                return;
            }

            if (song.isSelectedsong()) {
                song.setSelected(false);
            } else {
                int count = 0;
                for (Song s : songList) {
                    if (s.isSelectedsong()) count++;
                }

                if (count < 5) {
                    song.setSelected(true);
                } else {
                    Toast.makeText(v.getContext(), "אפשר לבחור עד 4 שירים בלבד", Toast.LENGTH_SHORT).show();
                }
            }

            notifyItemChanged(position);

            boolean hasSelection = false;
            for (Song s : songList) {
                if (s.isSelectedsong()) {
                    hasSelection = true;
                    break;
                }
            }

            View button = v.getRootView().findViewById(R.id.btnDoneSongs);
            if (button != null) {
                button.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }
}