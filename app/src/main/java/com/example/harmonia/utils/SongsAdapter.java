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
import com.example.harmonia.R;
import com.example.harmonia.Song;

import android.widget.Toast;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private List<Song> songList;
    private OnSongClickListener listener;
    private static final String TAG = "SongsAdapter";

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongsAdapter(List<Song> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    public void updateList(List<Song> newList) {
        this.songList = newList;
        notifyDataSetChanged();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView namesong, artist, genresong;
        public ImageView songimage;

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

        holder.namesong.setText(song.getName());
        holder.artist.setText(song.getArtist());
        holder.genresong.setText(song.getGenre());

        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + song.getId() + ".jpg";

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

        holder.itemView.setAlpha(song.isSelectedsong() ? 0.5f : 1.0f);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_ID) return;

            // אם יש listener (למשל מ-HomeActivity) — קורא לו ויוצא
            if (listener != null) {
                listener.onSongClick(songList.get(currentPosition));
                return;
            }

            // לוגיקת בחירה (לשימוש במסכים אחרים)
            Song currentSong = songList.get(currentPosition);

            if (currentSong.isSelectedsong()) {
                currentSong.setSelected(false);
            } else {
                int count = 0;
                for (Song s : songList) {
                    if (s.isSelectedsong()) count++;
                }
                if (count < 5) {
                    currentSong.setSelected(true);
                } else {
                    Toast.makeText(v.getContext(), "אפשר לבחור עד 4 שירים בלבד", Toast.LENGTH_SHORT).show();
                }
            }

            notifyItemChanged(currentPosition);

            boolean hasSelection = false;
            for (Song s : songList) {
                if (s.isSelectedsong()) { hasSelection = true; break; }
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