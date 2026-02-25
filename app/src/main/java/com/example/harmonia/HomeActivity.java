package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.BooksAdapter;
import com.example.harmonia.utils.PlaylistsAdapter;
import com.example.harmonia.utils.SongsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerBooks, recyclerSongs, recyclerPlaylists;
    private BooksAdapter booksAdapter;
    private SongsAdapter songsAdapter;
    private PlaylistsAdapter playlistsAdapter;

    private List<Book> bookList = new ArrayList<>();
    private List<Song> songList = new ArrayList<>();

    private List<Playlist> playlistsList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // --- RecyclerViews ---
        recyclerBooks = findViewById(R.id.recycler_booksrec);
        recyclerSongs = findViewById(R.id.recycler_songsrec);
        recyclerPlaylists = findViewById(R.id.recycler_playlistrec);


        // אופקי לספרים
        recyclerBooks.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // אופקי לשירים
        recyclerSongs.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recyclerPlaylists.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        booksAdapter = new BooksAdapter(bookList, imageUrl -> {
            // כאן תוכלי לפתוח מסך פרטי ספר אם תרצי
        });
        songsAdapter = new SongsAdapter(songList, song -> {
            // כאן תוכלי לפתוח מסך פרטי שיר אם תרצי
        });

        playlistsAdapter = new PlaylistsAdapter(playlistsList, this ,playlist -> {
        });

        recyclerBooks.setAdapter(booksAdapter);
        recyclerSongs.setAdapter(songsAdapter);
        recyclerPlaylists.setAdapter(playlistsAdapter);


        // --- טוען המלצות לפי ז'אנרים ---
        loadRecommendations();

        // --- ניווט תחתון ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_home);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_messages)
                startActivity(new Intent(this, MessagesActivity.class));
            else if (id == R.id.nav_community)
                startActivity(new Intent(this, CommunityActivity.class));
            else if (id == R.id.nav_profile)
                startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void loadRecommendations() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        // שלב 1: שולפים את הז'אנרים שהמשתמש בחר
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    List<String> selectedBookGenres =
                            (List<String>) documentSnapshot.get("selectedBookGenres");
                    List<String> selectedSongGenres =
                            (List<String>) documentSnapshot.get("selectedSongGenres");


                    // שלב 2: שולפים ספרים לפי הז'אנרים
                    if (selectedBookGenres != null && !selectedBookGenres.isEmpty()) {
                        loadBooksByGenres(selectedBookGenres);
                    }

                    // שלב 3: שולפים שירים לפי הז'אנרים
                    if (selectedSongGenres != null && !selectedSongGenres.isEmpty()) {
                        loadSongsByGenres(selectedSongGenres);
                    }

                    if (selectedSongGenres != null && !selectedSongGenres.isEmpty()) {
                        loadPlaylistsByGenres(selectedSongGenres);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("HomeActivity", "שגיאה בטעינת העדפות", e));
    }

    private void loadBooksByGenres(List<String> genres) {
        db.collection("books")
                .whereIn("genre", genres)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    bookList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Book book = doc.toObject(Book.class);
                        if (book != null) {
                            book.setId(doc.getId());
                            bookList.add(book);
                        }
                    }
                    booksAdapter.updateList(bookList); // ✅ עודכן
                    Log.d("HomeActivity", "נטענו " + bookList.size() + " ספרים");
                })
                .addOnFailureListener(e ->
                        Log.e("HomeActivity", "שגיאה בטעינת ספרים", e));
    }

    private void loadSongsByGenres(List<String> genres) {
        db.collection("songs")
                .whereIn("genre", genres)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    songList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            song.setId(doc.getId());
                            songList.add(song);
                        }
                    }
                    songsAdapter.updateList(songList); // ✅ עודכן
                    Log.d("HomeActivity", "נטענו " + songList.size() + " שירים");
                })
                .addOnFailureListener(e ->
                        Log.e("HomeActivity", "שגיאה בטעינת שירים", e));
    }

    private void loadPlaylistsByGenres(List<String> genres) {
        db.collection("playlists").get().addOnSuccessListener(querySnapshots -> {
            playlistsList.clear();

            for (DocumentSnapshot doc : querySnapshots) {
                // ניסיון לקחת את השדה name, ואם הוא חסר - לקחת את ה-ID של המסמך
                String pName = doc.getString("name");
                if (pName == null) {
                    pName = doc.getId(); // כאן הקסם: הוא יקח את "Rock Songs"
                }

                String playlistNameLower = pName.toLowerCase().trim();
                boolean isMatch = false;

                for (String genre : genres) {
                    if (genre != null && playlistNameLower.contains(genre.toLowerCase().trim())) {
                        isMatch = true;
                        break;
                    }
                }

                if (isMatch) {
                    Playlist playlist = doc.toObject(Playlist.class);
                    if (playlist == null) {
                        playlist = new Playlist(); // יצירת אובייקט חדש אם toObject נכשל
                    }
                    playlist.setId(doc.getId());
                    playlist.setName(pName);
                    playlistsList.add(playlist);
                }
            }

            Log.d("CHECK", "סוף חיפוש. נמצאו " + playlistsList.size() + " פלייליסטים.");
            playlistsAdapter.updateList(playlistsList);
        });
    }

}