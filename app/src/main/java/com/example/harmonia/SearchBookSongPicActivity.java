package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.CombinedMediaAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchBookSongPicActivity extends AppCompatActivity {

    FirebaseAuth auth;
    private RecyclerView recyclerView;
    private CombinedMediaAdapter adapter;
    private List<Object> mediaList; // רשימה משותפת לספרים ושירים
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_book_song_pic);

        db = FirebaseFirestore.getInstance();
        mediaList = new ArrayList<>();

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // אדפטר אחד לספרים ושירים
        adapter = new CombinedMediaAdapter(mediaList, imageUrl -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("POST_IMAGE_URL", imageUrl);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        recyclerView.setAdapter(adapter);

        // SearchView אחד
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBooksAndSongs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllBooksAndSongs();
                } else {
                    searchBooksAndSongs(newText);
                }
                return true;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAllBooksAndSongs();
    }

    private void searchBooksAndSongs(String searchText) {
        mediaList.clear();

        // חיפוש ספרים
        db.collection("books")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            mediaList.add(book);
                        }
                        android.util.Log.d("SEARCH_DEBUG", "Found " + task.getResult().size() + " books");

                        // חיפוש שירים אחרי הספרים
                        searchSongs(searchText);
                    }
                });
    }

    private void searchSongs(String searchText) {
        db.collection("songs")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            song.setId(document.getId());
                            mediaList.add(song);
                        }
                        android.util.Log.d("SEARCH_DEBUG", "Found " + task.getResult().size() + " songs");
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadAllBooksAndSongs() {
        mediaList.clear();

        // טעינת ספרים
        db.collection("books")
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            mediaList.add(book);
                        }
                        android.util.Log.d("SEARCH_DEBUG", "Loaded " + task.getResult().size() + " books");

                        // טעינת שירים אחרי הספרים
                        loadAllSongs();
                    }
                });
    }

    private void loadAllSongs() {
        db.collection("songs")
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            song.setId(document.getId());
                            mediaList.add(song);
                        }
                        android.util.Log.d("SEARCH_DEBUG", "Loaded " + task.getResult().size() + " songs");
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}