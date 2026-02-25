package com.example.harmonia;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsActivity extends AppCompatActivity implements SongsAdapter.OnSongClickListener{
    private TextView textViewPlaylistName;
    private RecyclerView recyclerViewSongsPlaylist;

    // הנתונים וה-Database
    private FirebaseFirestore db;
    private SongsAdapter adapter;
    private List<Song> songList;
    private RecyclerView recyclerView;
    private SongsAdapter songsAdapter; // ודאי שיש לך אדפטר לשירים
    private List<Song> songsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        textViewPlaylistName = findViewById(R.id.textViewPlaylistName);
        recyclerViewSongsPlaylist = findViewById(R.id.recyclerViewSongsPlaylist);

        db = FirebaseFirestore.getInstance();
        songList = new ArrayList<>();

        // כאן את מחברת את האדאפטר שלך לרשימה
        adapter = new SongsAdapter(songList, this);

        recyclerViewSongsPlaylist.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSongsPlaylist.setAdapter(adapter);
    }

    @Override
    public void onSongClick(Song song) {

    }
}