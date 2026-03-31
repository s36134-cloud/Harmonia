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
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchSongActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private List<Song> songList;
    private FirebaseFirestore db;
    private String listId; // ID של הרשימה הספציפית אם קיים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_song);

        db = FirebaseFirestore.getInstance();
        songList = new ArrayList<>();

        // בדיקה: האם קיבלנו ID של רשימה ספציפית? (למשל "my_books_list_1")
        listId = getIntent().getStringExtra("LIST_ID");
        boolean isSelectionMode = getIntent().getBooleanExtra("IS_SELECTION_MODE", false);

        final Button btnDone = findViewById(R.id.btnDoneSongs);
        btnDone.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new SongsAdapter(songList, song -> {
            // האדאפטר משנה את ה-isSelected בלחיצה, כאן רק נעדכן את הכפתור
            boolean hasSelection = false;
            for (Song s : songList) {
                if (s.isSelectedsong()) {
                    hasSelection = true;
                    break;
                }
            }
            btnDone.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        }, R.layout.song);

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
                if (newText.isEmpty()) loadAllSongs();
                else searchInFirebase(newText);
                return true;
            }
        });

        findViewById(R.id.imageViewsongsearch).setOnClickListener(v -> finish());

        btnDone.setOnClickListener(v -> {
            List<String> selectedSongIds = new ArrayList<>();
            for (Song s : songList) {
                if (s.isSelectedsong()) {
                    selectedSongIds.add(s.getId());
                }
            }

            if (selectedSongIds.isEmpty()) return;

            // החלטה לאן לשמור:
            if (listId != null) {
                // אם יש ID לרשימה - שומרים בתוך תת-אוסף של המשתמש
                saveToSpecificList(selectedSongIds);
            } else {
                // ברירת מחדל - שמירה לפרופיל (topSongs)
                saveSelectedSongsAndGoToProfile(selectedSongIds);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAllSongs();
    }

    // פונקציה חדשה לשמירה תחת רשימה ספציפית בתוך המשתמש
    private void saveToSpecificList(List<String> songIds) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> listData = new HashMap<>();
        listData.put("itemsIds", songIds); // רשימת ה-IDs של השירים/ספרים
        listData.put("lastUpdated", FieldValue.serverTimestamp());

        // נתיב: users -> {userId} -> my_custom_lists -> {listId}
        db.collection("users").document(userId)
                .collection("my_custom_lists").document(listId)
                .set(listData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הרשימה עודכנה!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show());
    }

    private void saveSelectedSongsAndGoToProfile(List<String> songIds) {
        String userId = FirebaseAuth.getInstance().getUid();
        db.collection("users").document(userId)
                .update("topSongs", FieldValue.arrayUnion(songIds.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "התווסף למועדפים!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("topSongs", songIds);
                    db.collection("users").document(userId).set(data, SetOptions.merge());
                    finish();
                });
    }

    // ... (loadAllSongs ו-searchInFirebase נשארים ללא שינוי)
    private void searchInFirebase(String searchText) {
        db.collection("songs")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        songList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            song.setId(document.getId());
                            songList.add(song);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadAllSongs() {
        db.collection("songs").orderBy("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                songList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                    Song song = document.toObject(Song.class);
                    song.setId(document.getId());
                    songList.add(song);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}