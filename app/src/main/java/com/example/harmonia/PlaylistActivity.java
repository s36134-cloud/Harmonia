package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private SongsAdapter songsAdapter;
    private List<Song> songsList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        db = FirebaseFirestore.getInstance();

        // 1. קבלת ה-ID (למשל "Love Songs") שנשלח מה-Adapter
        String playlistId = getIntent().getStringExtra("playlistId");

        // 2. הגדרת ה-RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSongsPlaylist);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // אתחול האדפטר
        songsAdapter = new SongsAdapter(songsList, null);
        recyclerView.setAdapter(songsAdapter);

        // 3. שליפת השירים במידה ויש ID
        if (playlistId != null) {
            fetchSongsFromPlaylist(playlistId);
        } else {
            Toast.makeText(this, "שגיאה: לא נמצא מזהה פלייליסט", Toast.LENGTH_SHORT).show();
        }

        TextView playlistTitle = findViewById(R.id.title);
        String playlistName = getIntent().getStringExtra("playlistId");

        if (playlistName != null) {
            playlistTitle.setText(playlistName);
            // עכשיו אפשר להמשיך לטעינת השירים
            fetchSongsFromPlaylist(playlistName);
        }


        ImageView backhomeImageView = findViewById(R.id.backhome);
        backhomeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(PlaylistActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchSongsFromPlaylist(String playlistId) {
        db.collection("playlists").document(playlistId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> rawSongIds = (List<String>) documentSnapshot.get("songs");

                        if (rawSongIds != null && !rawSongIds.isEmpty()) {
                            // --- התיקון: סינון IDs ריקים כדי למנוע קריסה ---
                            List<String> cleanSongIds = new ArrayList<>();
                            for (String id : rawSongIds) {
                                if (id != null && !id.trim().isEmpty()) {
                                    cleanSongIds.add(id);
                                }
                            }

                            // בדיקה נוספת שהרשימה לא התרוקנה אחרי הסינון
                            if (!cleanSongIds.isEmpty()) {
                                db.collection("songs")
                                        .whereIn(FieldPath.documentId(), cleanSongIds)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            songsList.clear();
                                            for (DocumentSnapshot doc : querySnapshot) {
                                                Song song = doc.toObject(Song.class);
                                                if (song != null) {
                                                    song.setId(doc.getId());
                                                    songsList.add(song);
                                                }
                                            }
                                            songsAdapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> Log.e("PlaylistActivity", "Error loading songs", e));
                            } else {
                                Log.d("PlaylistActivity", "No valid song IDs found after cleaning");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("PlaylistActivity", "Error loading playlist", e));
    }


}