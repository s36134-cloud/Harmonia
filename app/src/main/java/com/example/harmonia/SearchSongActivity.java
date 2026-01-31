package com.example.harmonia;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchSongActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private List<Song> songList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_song);

        db = FirebaseFirestore.getInstance();
        songList = new ArrayList<>();

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new  SongsAdapter(songList);
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchViewsong);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchInFirebase(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1) {
                    searchInFirebase(newText);
                }
                return true;
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void searchInFirebase(String searchText) {


        db.collection("songs")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        songList.clear();
                        if (task.getResult().isEmpty()) {
                            android.util.Log.d("SEARCH_DEBUG", "No songs found for: " + searchText);
                        } else {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                Song song = document.toObject(Song.class);
                                song.setId(document.getId());
                                songList.add(song);
                            }
                            android.util.Log.d("SEARCH_DEBUG", "Found " + songList.size() + " songs!");
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error getting documents: ", task.getException());
                    }
                });
    }
}