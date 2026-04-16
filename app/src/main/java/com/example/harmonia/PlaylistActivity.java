package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private String spotifyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        String playlistId = getIntent().getStringExtra("playlistId");
        spotifyId = getIntent().getStringExtra("SPOTIFY_ID");

        recyclerView = findViewById(R.id.recyclerViewSongsPlaylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        android.view.View btnSpotify = findViewById(R.id.btnOpenSpotify);
        btnSpotify.setOnClickListener(v -> {
            if (spotifyId != null && !spotifyId.isEmpty()) {
                openSpotifyPlaylist(spotifyId);
            } else {
                Toast.makeText(this, "פלייליסט זה לא זמין בספוטיפיי", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openSpotifyPlaylist(String id) {
        // 1. הכתובת לפתיחה ישירה באפליקציה (URI)
        android.net.Uri uri = android.net.Uri.parse("spotify:playlist:" + id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Intent.EXTRA_REFERRER, android.net.Uri.parse("android-app://" + getPackageName()));

        try {
            startActivity(intent);
        } catch (Exception e) {
            // 2. הכתובת לגיבוי בדפדפן - שים לב למבנה הלינק המדויק
            String webUrl = "https://open.spotify.com/playlist/" + id;
            android.net.Uri webUri = android.net.Uri.parse(webUrl);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
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