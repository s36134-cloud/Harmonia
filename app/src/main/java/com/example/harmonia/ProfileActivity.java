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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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



        // 1. אתחול ה-Database
        db = FirebaseFirestore.getInstance();

// 2. חיבור ה-RecyclerView לפי ה-ID שנתת ב-XML
        recyclerSongs = findViewById(R.id.recycler_songs);
        recyclerBooks = findViewById(R.id.recycler_books);

// 3. אתחול הרשימה והאדאפטר
        topSongsList = new ArrayList<>();
        songsAdapter = new SongsAdapter(topSongsList,null);

        topBooksList = new ArrayList<>();
        booksAdapter = new BooksAdapter(topBooksList, null);


// 4. הגדרת התצוגה לאופקית (Horizontal)
        LinearLayoutManager layoutManagerSongs = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerSongs.setLayoutManager(layoutManagerSongs);

        LinearLayoutManager layoutManagerBooks = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerBooks.setLayoutManager(layoutManagerBooks);


// 5. חיבור האדאפטר ל-RecyclerView
        recyclerSongs.setAdapter(songsAdapter);
        recyclerBooks.setAdapter(booksAdapter);




        // סרגל הניווט  התחלה
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_Profile);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_profile); // מסמן את דף הפרופיל

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;

            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_messages) startActivity(new Intent(this, MessagesActivity.class));
            else if (id == R.id.nav_community) startActivity(new Intent(this, CommunityActivity.class));

            overridePendingTransition(0, 0);
            return true;
        });
        // סרגל הניווט סוף

        //כפתור המידע התחלה
        Button infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(ProfileActivity.this, CommunityActivity.class);
                startActivity(intent);
                finish();

            }

        });
        // כפתור המידע סוף


        // כפתור היציאה התחלה
        Button signoutButton = findViewById(R.id.signout_button);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent=new Intent(ProfileActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();

            }

        });
        //כפתור היציאה סוף

        //כפתור פתיחת החיפוש ספר התחלה
        Button opensearchbooksButton = findViewById(R.id.btn_opensearchbooks);
        opensearchbooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(ProfileActivity.this, SearchBookActivity.class);
                startActivity(intent);


            }

        });
        //כפתור פתיחת החיפוש ספר סוף



        //כפתור פתיחת החיפוש שיר התחלה
        Button opensearchsongsButton = findViewById(R.id.btn_opensearchsongs);
        opensearchsongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(ProfileActivity.this, SearchSongActivity.class);
                startActivity(intent);


            }

        });
        //כפתור פתיחת החיפוש שיר סוף


        ImageView profilePictureImageView = findViewById(R.id.imageView);
        userImageSelector = new UserImageSelector(this, profilePictureImageView, this);
        Button choosePictureButton = findViewById(R.id.btn_choose_picture);
        choosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImageSelector.showImageSourceDialog();
            }
        });

        loadUserTopSongs();


    }@
            Override
    protected void onResume() {
        super.onResume();
        loadUserTopSongs();
        loadUserTopBooks();
    }


    private void loadUserTopSongs() {
        // 1. קבלת ה-ID של המשתמש המחובר
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 2. פנייה למסמך של המשתמש ב-Firebase
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 3. שליפת רשימת ה-IDs ששמרנו (ה-topSongs)
                List<String> songIds = (List<String>) documentSnapshot.get("topSongs");

                if (songIds != null && !songIds.isEmpty()) {
                    // 4. פנייה לטבלת השירים כדי להביא את הפרטים המלאים של השירים האלו
                    db.collection("songs")
                            .whereIn(FieldPath.documentId(), songIds) // "תביא לי רק את השירים שה-ID שלהם ברשימה"
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                topSongsList.clear(); // מנקים את הרשימה הישנה
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Song song = doc.toObject(Song.class);
                                    song.setId(doc.getId());
                                    topSongsList.add(song); // מוסיפים לרשימה שלנו
                                }
                                // 5. אומרים לאדאפטר: "יש נתונים חדשים! תצייר אותם על המסך"
                                songsAdapter.notifyDataSetChanged();
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בטעינת השירים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserTopBooks() {
        // 1. קבלת ה-ID של המשתמש המחובר
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 2. פנייה למסמך של המשתמש ב-Firebase
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 3. שליפת רשימת ה-IDs ששמרנו (ה-topBooks)
                List<String> bookIds = (List<String>) documentSnapshot.get("topBooks");

                if (bookIds != null && !bookIds.isEmpty()) {
                    // 4. פנייה לטבלת השירים כדי להביא את הפרטים המלאים של השירים האלו
                    db.collection("books")
                            .whereIn(FieldPath.documentId(), bookIds) // "תביא לי רק את השירים שה-ID שלהם ברשימה"
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                topBooksList.clear(); // מנקים את הרשימה הישנה
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Book book = doc.toObject(Book.class);
                                    book.setId(doc.getId());
                                    topBooksList.add(book); // מוסיפים לרשימה שלנו
                                }
                                // 5. אומרים לאדאפטר: "יש נתונים חדשים! תצייר אותם על המסך"
                                booksAdapter.notifyDataSetChanged();
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בטעינת השירים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResult(boolean success, String message) {
        if(success)
        {
            File imageFile = userImageSelector.createImageFile();
            String filePath = "images/profiles/" + auth.getUid() + ".jpg";

            SupabaseStorageHelper.uploadPicture(imageFile, filePath, new SupabaseStorageHelper.OnResultCallback() {
                @Override
                public void onResult(boolean success, String url, String error) {
                    Log.d(TAG, "onResult: success: " + success);
                    Log.d(TAG, "onResult: url: " + url);
                    Log.d(TAG, "onResult: error: " + error);
                }
            });
        }
    }
}