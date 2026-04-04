package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.harmonia.utils.BooksAdapter;
import com.example.harmonia.utils.OnResultCallback;
import com.example.harmonia.utils.SongsAdapter;
import com.example.harmonia.utils.SupabaseStorageHelper;
import com.example.harmonia.utils.UserImageSelector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements OnResultCallback {
    FirebaseAuth auth;

    private UserImageSelector userImageSelector;

    private RecyclerView recyclerSongs;
    private SongsAdapter songsAdapter;
    private List<Song> topSongsList;
    private FirebaseFirestore db;

    private RecyclerView recyclerBooks;
    private BooksAdapter booksAdapter;
    private List<Book> topBooksList;

    // משתנה גלובלי לתמונת הפרופיל
    private ImageView profilePictureImageView;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // אתחול ה-ImageView וטעינת התמונה מהשרת מיד בכניסה
        profilePictureImageView = findViewById(R.id.imageView);
        loadProfilePicture();

        recyclerSongs = findViewById(R.id.recycler_songs);
        recyclerBooks = findViewById(R.id.recycler_books);

        topSongsList = new ArrayList<>();
        songsAdapter = new SongsAdapter(topSongsList,null, R.layout.song);
        topBooksList = new ArrayList<>();
        booksAdapter = new BooksAdapter(topBooksList, null, R.layout.book);

        recyclerSongs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recyclerSongs.setAdapter(songsAdapter);
        recyclerBooks.setAdapter(booksAdapter);

        // סרגל ניווט
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_Profile);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;
            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_messages) startActivity(new Intent(this, MessagesActivity.class));
            else if (id == R.id.nav_community) startActivity(new Intent(this, CommunityActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });

        // כפתורים
        findViewById(R.id.Genres_button).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, GenresActivity.class));
            finish();
        });

        findViewById(R.id.Your_listss).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ListsActivity.class));
            finish();
        });

        findViewById(R.id.signout_button).setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btn_opensearchbooks).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SearchBookActivity.class)));

        findViewById(R.id.btn_opensearchsongs).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SearchSongActivity.class)));

        // הגדרת בחירת תמונה
        userImageSelector = new UserImageSelector(this, profilePictureImageView, this);
        Button choosePictureButton = findViewById(R.id.btn_choose_picture);
        choosePictureButton.setOnClickListener(v -> userImageSelector.showImageSourceDialog());

        loadUserTopSongs();
        loadUserTopBooks();
    }

    // הפונקציה המרכזית שטוענת את התמונה מה-Storage
    private void loadProfilePicture() {
        if (auth.getCurrentUser() != null) {
            String filePath = "images/profiles/" + auth.getUid() + ".jpg";
            String url = SupabaseStorageHelper.getFileSupabaseUrl(filePath);

            Log.d(TAG, "Loading image from: " + url);

            Glide.with(this)
                    .load(url)
                    .signature(new ObjectKey(System.currentTimeMillis())) // חשוב: מבטיח שהתמונה תתרענן ולא תיקלח מהזיכרון הישן
                    .circleCrop()
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.ic_menu_gallery) // תמונה שתוצג אם אין עדיין תמונה בשרת
                    .into(profilePictureImageView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfilePicture(); // טעינה מחדש בכל פעם שחוזרים למסך
        loadUserTopSongs();
        loadUserTopBooks();
    }

    @Override
    public void onResult(boolean success, String message) {
        if(success) {
            File imageFile = userImageSelector.createImageFile();
            String filePath = "images/profiles/" + auth.getUid() + ".jpg";

            SupabaseStorageHelper.uploadPicture(imageFile, filePath, new SupabaseStorageHelper.OnResultCallback() {
                @Override
                public void onResult(boolean success, String url, String error) {
                    if (success) {
                        Log.d(TAG, "Upload success! Refreshing UI...");
                        loadProfilePicture(); // רענון התמונה ב-UI מיד אחרי ההעלאה
                    } else {
                        Log.e(TAG, "Upload failed: " + error);
                    }
                }
            });
        }
    }

    // --- שאר הפונקציות של Firebase (Songs/Books) נשארות ללא שינוי ---
    private void loadUserTopSongs() {
        String userId = auth.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> songIds = (List<String>) documentSnapshot.get("topSongs");
                if (songIds != null && !songIds.isEmpty()) {
                    db.collection("songs").whereIn(FieldPath.documentId(), songIds).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        topSongsList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Song song = doc.toObject(Song.class);
                            song.setId(doc.getId());
                            topSongsList.add(song);
                        }
                        songsAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }

    private void loadUserTopBooks() {
        String userId = auth.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> bookIds = (List<String>) documentSnapshot.get("topBooks");
                if (bookIds != null && !bookIds.isEmpty()) {
                    db.collection("books").whereIn(FieldPath.documentId(), bookIds).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        topBooksList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Book book = doc.toObject(Book.class);
                            book.setId(doc.getId());
                            topBooksList.add(book);
                        }
                        booksAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}