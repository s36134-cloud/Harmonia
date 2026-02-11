package com.example.harmonia;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.BooksAdapter;
import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchBookSongPicActivity extends AppCompatActivity {

    FirebaseAuth auth;
    private RecyclerView recyclerView;
    private BooksAdapter booksAdapter;
    private List<Book> bookList;
    private FirebaseFirestore db;

    private SongsAdapter songsAdapter;
    private List<Song> songList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_book_song_pic);

        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();
        songList = new ArrayList<>();


        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        booksAdapter = new BooksAdapter(bookList);
        recyclerView.setAdapter(booksAdapter);

        songsAdapter = new  SongsAdapter(songList);
        recyclerView.setAdapter(songsAdapter);

        SearchView searchViewbook = findViewById(R.id.searchViewbooksong);
        searchViewbook.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchInFirebasebook(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // אם המשתמש מחק הכל - תציג שוב את כל השירים
                    loadAllBooks();
                } else if (newText.length() > 0) {
                    // אם יש טקסט - תבצע חיפוש
                    searchInFirebasebook(newText);
                }
                return true;
            }
        });

        SearchView searchViewsong = findViewById(R.id.searchViewbooksong);
        searchViewsong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchInFirebasesong(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // אם המשתמש מחק הכל - תציג שוב את כל השירים
                    loadAllSongs();
                } else if (newText.length() > 0) {
                    // אם יש טקסט - תבצע חיפוש
                    searchInFirebasesong(newText);
                }
                return true;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAllBooks();
        loadAllSongs();

    }
    private void searchInFirebasebook(String searchText) {


        db.collection("books")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        if (task.getResult().isEmpty()) {
                            android.util.Log.d("SEARCH_DEBUG", "No books found for: " + searchText);
                        } else {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                book.setId(document.getId());
                                bookList.add(book);
                            }
                            android.util.Log.d("SEARCH_DEBUG", "Found " + bookList.size() + " books!");
                        }
                        booksAdapter.notifyDataSetChanged();
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error getting documents: ", task.getException());
                    }
                });
    }


    private void searchInFirebasesong(String searchText) {


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
                        songsAdapter.notifyDataSetChanged();
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error getting documents: ", task.getException());
                    }
                });
    }


    private void loadAllBooks() {
        db.collection("books")
                .orderBy("name") // מסדר אותם לפי א'-ב'
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        if (task.getResult() != null) {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                book.setId(document.getId());
                                bookList.add(book);
                            }
                        }
                        booksAdapter.notifyDataSetChanged();
                        android.util.Log.d("SEARCH_DEBUG", "Loaded all " + bookList.size() + " books");
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error loading books: ", task.getException());
                    }
                });
    }

    private void loadAllSongs() {
        db.collection("songs")
                .orderBy("name") // מסדר אותם לפי א'-ב'
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        songList.clear();
                        if (task.getResult() != null) {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                Song song = document.toObject(Song.class);
                                song.setId(document.getId());
                                songList.add(song);
                            }
                        }
                        songsAdapter.notifyDataSetChanged();
                        android.util.Log.d("SEARCH_DEBUG", "Loaded all " + songList.size() + " songs");
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error loading songs: ", task.getException());
                    }
                });
    }
}