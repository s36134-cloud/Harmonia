package com.example.harmonia.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.example.harmonia.PlaylistsActivity;
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
        public ImageView image;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewPlaylistNameItem);
            image = itemView.findViewById(R.id.playlistimage);
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

        //String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/images/songs/" + song.getId() + ".jpg";

        //holder.image.setVisibility(View.INVISIBLE);
        //holder.image.setImageDrawable(null);

        //Glide.with(holder.itemView.getContext())
          //      .load(imageUrl)
            //    .listener(new RequestListener<Drawable>() {
              //      @Override
                //    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                  //                              Target<Drawable> target, boolean isFirstResource) {
                    //    holder.image.setVisibility(View.INVISIBLE);
                      //  return true;
                    //}

                    //@Override
                    //public boolean onResourceReady(Drawable resource, Object model,
                      //                             Target<Drawable> target, DataSource dataSource,
                        //                           boolean isFirstResource) {
                  //      holder.image.setVisibility(View.VISIBLE);
                    //    return false;
               //     }
                //})
               // .into(holder.image);


        holder.itemView.setOnClickListener(v -> {

            TextView name = v.findViewById(R.id.textViewPlaylistNameItem);
            Intent intent = new Intent(activity, PlaylistsActivity.class);
            intent.putExtra("playlistId", name.getText());
            activity.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return playlists != null ? playlists.size() : 0;
    }
}
