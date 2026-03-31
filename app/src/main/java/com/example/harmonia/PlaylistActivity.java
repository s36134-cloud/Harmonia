package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongsAdapter songsAdapter;
    private List<Song> songsList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        db = FirebaseFirestore.getInstance();

        String playlistId = getIntent().getStringExtra("playlistId");

        recyclerView = findViewById(R.id.recyclerViewSongsPlaylist);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        songsAdapter = new SongsAdapter(songsList, null, R.layout.song_list);
        recyclerView.setAdapter(songsAdapter);

        TextView playlistTitle = findViewById(R.id.title);

        if (playlistId != null) {
            playlistTitle.setText(playlistId);
            fetchSongsFromPlaylist(playlistId);
        } else {
            Toast.makeText(this, "שגיאה: לא נמצא מזהה פלייליסט", Toast.LENGTH_SHORT).show();
        }

        ImageView backhomeImageView = findViewById(R.id.backhome);
        backhomeImageView.setOnClickListener(v -> {
            Intent intent = new Intent(PlaylistActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void fetchSongsFromPlaylist(String playlistId) {
        db.collection("playlists").document(playlistId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> rawSongIds = (List<String>) documentSnapshot.get("songs");

                        if (rawSongIds != null && !rawSongIds.isEmpty()) {
                            List<String> cleanSongIds = new ArrayList<>();
                            for (String id : rawSongIds) {
                                if (id != null && !id.trim().isEmpty()) {
                                    cleanSongIds.add(id.trim());
                                }
                            }

                            if (!cleanSongIds.isEmpty()) {
                                fetchSongsInBatches(cleanSongIds);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("PlaylistActivity", "Error loading playlist", e));
    }

    // פיצול לחלקים של 30 בגלל מגבלת Firestore
    private void fetchSongsInBatches(List<String> songIds) {
        int batchSize = 30;
        int totalBatches = (int) Math.ceil((double) songIds.size() / batchSize);
        final int[] completedBatches = {0};

        songsList.clear();

        for (int i = 0; i < songIds.size(); i += batchSize) {
            List<String> batch = songIds.subList(i, Math.min(i + batchSize, songIds.size()));

            db.collection("songs")
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot) {
                            Song song = doc.toObject(Song.class);
                            if (song != null) {
                                song.setId(doc.getId());
                                songsList.add(song);
                            }
                        }
                        completedBatches[0]++;
                        if (completedBatches[0] == totalBatches) {
                            songsAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("PlaylistActivity", "Error loading songs batch", e));
        }
    }
}