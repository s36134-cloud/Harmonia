package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.GenresAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenresActivity extends AppCompatActivity {

    private RecyclerView recyclerSongsGenres, recyclerBooksGenres;
    private GenresAdapter songsAdapter, booksAdapter;
    private List<String> songGenreNames;
    private List<String> bookGenreNames;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_genres);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerSongsGenres = findViewById(R.id.recycler_SongsGenres);
        recyclerBooksGenres = findViewById(R.id.recycler_BooksGenres);

        recyclerSongsGenres.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerBooksGenres.setLayoutManager(new GridLayoutManager(this, 3));

        songGenreNames = new ArrayList<>();
        bookGenreNames = new ArrayList<>();

        songsAdapter = new GenresAdapter(songGenreNames);
        booksAdapter = new GenresAdapter(bookGenreNames);

        recyclerSongsGenres.setAdapter(songsAdapter);
        recyclerBooksGenres.setAdapter(booksAdapter);

        loadSongsGenres();
        loadBooksGenres();

        Button saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(v -> {
            List<String> newSelectedSongs = songsAdapter.getCheckedGenres();
            List<String> newSelectedBooks = booksAdapter.getCheckedGenres();

            // טוענים קודם את מה שכבר שמור ומוסיפים
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        List<String> existingSongs = new ArrayList<>();
                        List<String> existingBooks = new ArrayList<>();

                        if (documentSnapshot.exists()) {
                            List<String> s = (List<String>) documentSnapshot.get("selectedSongGenres");
                            List<String> b = (List<String>) documentSnapshot.get("selectedBookGenres");
                            if (s != null) existingSongs.addAll(s);
                            if (b != null) existingBooks.addAll(b);
                        }

                        // מוסיפים רק ז'אנרים שעדיין לא קיימים
                        for (String genre : newSelectedSongs) {
                            if (!existingSongs.contains(genre)) existingSongs.add(genre);
                        }
                        for (String genre : newSelectedBooks) {
                            if (!existingBooks.contains(genre)) existingBooks.add(genre);
                        }

                        Map<String, Object> update = new HashMap<>();
                        update.put("selectedSongGenres", existingSongs);
                        update.put("selectedBookGenres", existingBooks);

                        db.collection("users").document(userId)
                                .set(update, SetOptions.merge())
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Saved successfully✓", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    Log.e("GenresActivity", "Error saving genres", e);
                                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("GenresActivity", "Error loading existing genres", e);
                        Toast.makeText(this, "Failed to load existing genres", Toast.LENGTH_SHORT).show();
                    });
        });

        Button backtoprofileButton = findViewById(R.id.btn_back_to_profile);
        backtoprofileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent=new Intent(GenresActivity.this,ProfileActivity.class);
                startActivity(intent);
                finish();

            }

        });
    }

    private void loadSongsGenres() {
        db.collection("songGenres")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    songGenreNames.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        songGenreNames.add(document.getId());
                    }
                    songsAdapter.notifyDataSetChanged();
                    Log.d("GenresActivity", "Loaded " + songGenreNames.size() + " song genres");
                })
                .addOnFailureListener(e -> {
                    Log.e("GenresActivity", "Error loading songs genres", e);
                    Toast.makeText(this, "שגיאה בטעינת ז'אנרים של שירים", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBooksGenres() {
        db.collection("bookGenres")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookGenreNames.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        bookGenreNames.add(document.getId());
                    }
                    booksAdapter.notifyDataSetChanged();
                    Log.d("GenresActivity", "Loaded " + bookGenreNames.size() + " book genres");
                })
                .addOnFailureListener(e -> {
                    Log.e("GenresActivity", "Error loading books genres", e);
                    Toast.makeText(this, "שגיאה בטעינת ז'אנרים של ספרים", Toast.LENGTH_SHORT).show();
                });
    }
}