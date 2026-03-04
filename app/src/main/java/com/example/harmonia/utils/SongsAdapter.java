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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.harmonia.R;
import com.example.harmonia.Song;

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

        // בניית כתובת התמונה מה-Supabase
        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + song.getId() + ".jpg";

        // אתחול התמונה לפני טעינה מחדש (מונע תמונות קופצות ב-Recycle)
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

        // שינוי שקיפות השורה אם השיר נבחר
        holder.itemView.setAlpha(song.isSelectedsong() ? 0.5f : 1.0f);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Song currentSong = songList.get(currentPosition);

            // 1. אם קיים Listener חיצוני (כמו ב-HomeActivity), הוא מקבל את האירוע
            if (listener != null) {
                listener.onSongClick(currentSong);
                return;
            }

            // 2. לוגיקת בחירה מרובה (ללא הגבלת כמות)
            // פשוט הופכים את מצב הבחירה (Inverting)
            currentSong.setSelected(!currentSong.isSelectedsong());

            // עדכון השורה הספציפית ב-UI
            notifyItemChanged(currentPosition);

            // 3. עדכון כפתור ה-"סיום" (btnDoneSongs) אם הוא קיים במסך
            checkSelectionStatus(v);
        });
    }

    /**
     * פונקציית עזר לבדיקה האם יש לפחות שיר אחד נבחר והצגת כפתור הסיום בהתאם
     */
    private void checkSelectionStatus(View v) {
        boolean hasSelection = false;
        if (songList != null) {
            for (Song s : songList) {
                if (s.isSelectedsong()) {
                    hasSelection = true;
                    break;
                }
            }
        }

        View button = v.getRootView().findViewById(R.id.btnDoneSongs);
        if (button != null) {
            button.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }
}