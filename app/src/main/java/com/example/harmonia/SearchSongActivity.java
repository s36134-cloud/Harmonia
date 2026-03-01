package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new  SongsAdapter(songList, null);
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
                if (newText.isEmpty()) {
                    // אם המשתמש מחק הכל - תציג שוב את כל השירים
                    loadAllSongs();
                } else if (newText.length() > 0) {
                    // אם יש טקסט - תבצע חיפוש
                    searchInFirebase(newText);
                }
                return true;
            }
        });

        ImageView backsongImageView = findViewById(R.id.imageViewsongsearch);
        backsongImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SearchSongActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        Button btnDone = findViewById(R.id.btnDoneSongs);
        btnDone.setVisibility(android.view.View.GONE);
        btnDone.setOnClickListener(v -> {
            // 1. יצירת רשימה של ה-IDs של השירים שנבחרו
            List<String> selectedSongIds = new ArrayList<>();
            for (Song s : songList) {
                if (s.isSelectedsong()) {
                    selectedSongIds.add(s.getId());
                }
            }

            // 2. בדיקה אם המשתמש בחר שירים
            if (selectedSongIds.isEmpty()) {
                Toast.makeText(this, "אנא בחרי לפחות שיר אחד", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. שמירה ל-Firebase ומעבר מסך
            saveSelectedSongsAndGoToProfile(selectedSongIds);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAllSongs();
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

    private void saveSelectedSongsAndGoToProfile(List<String> songIds) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        // במקום ליצור Map חדש ולדרוס, אנחנו משתמשים ב-arrayUnion
        db.collection("users").document(userId)
                .update("topSongs", com.google.firebase.firestore.FieldValue.arrayUnion(songIds.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "השירים נוספו בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // חוזר לפרופיל
                })
                .addOnFailureListener(e -> {
                    // אם המסמך לא קיים בכלל, update עלול להיכשל, אז נשתמש ב-set כגיבוי
                    db.collection("users").document(userId)
                            .set(new java.util.HashMap<String, Object>() {{
                                put("topSongs", songIds);
                            }}, com.google.firebase.firestore.SetOptions.merge());
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
                        adapter.notifyDataSetChanged();
                        android.util.Log.d("SEARCH_DEBUG", "Loaded all " + songList.size() + " songs");
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error loading songs: ", task.getException());
                    }
                });
    }
}