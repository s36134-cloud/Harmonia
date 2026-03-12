package com.example.harmonia.utils;

import android.app.Activity;
import android.content.Intent;
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
import com.example.harmonia.Playlist;
import com.example.harmonia.PlaylistActivity;
import com.example.harmonia.R;

import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>  {
    private List<Playlist> playlists;

    private Activity activity;
    private static final String TAG = "SongsAdapter";

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public PlaylistsAdapter(List<Playlist> playlists, Activity activity, PlaylistsAdapter.OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.activity = activity;
    }

    public void updateList(List<Playlist> newList) {
        this.playlists = newList;
        notifyDataSetChanged();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView img1, img2, img3, img4;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewPlaylistNameItem);
            img1 = itemView.findViewById(R.id.img1);
            img2 = itemView.findViewById(R.id.img2);
            img3 = itemView.findViewById(R.id.img3);
            img4 = itemView.findViewById(R.id.img4);
        }
    }

    @NonNull
    @Override
    public PlaylistsAdapter.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist, parent, false);
        return new PlaylistsAdapter.PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistsAdapter.PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        holder.name.setText(playlist.getName());

        List<String> songIds = playlist.getSongs();

        ImageView[] imageViews = {holder.img1, holder.img2, holder.img3, holder.img4};

        for (int i = 0; i < 4; i++) {
            if (songIds != null && i < songIds.size()) {
                String songId = songIds.get(i).trim();
                String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + songId + ".jpg";

                Log.d("PlaylistCover", "Position " + i + " | songId: [" + songId + "] | url: " + imageUrl);

                imageViews[i].setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_close_clear_cancel)
                        .into(imageViews[i]);
            } else {
                imageViews[i].setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            String id = playlist.getName();
            Intent intent = new Intent(activity, PlaylistActivity.class);
            intent.putExtra("playlistId", id);
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return playlists != null ? playlists.size() : 0;
    }
        }