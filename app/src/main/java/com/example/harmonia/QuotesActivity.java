package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.BookQuoteAdapter;
import com.example.harmonia.utils.SongQuoteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class QuotesActivity extends AppCompatActivity {


    RecyclerView recyclerViewbook;
    BookQuoteAdapter BookQuoteadapter;
    List<BookQuote> Bookmood;
    FirebaseFirestore db;

    RecyclerView recyclerViewsong;
    SongQuoteAdapter SongQuoteadapter;
    List<SongQuote> Songmood;

    private MaterialButton btnInLove, btnSad, btnHappy, btnInspirational, btnHeartbroken,btnHope, btnAll;
    private BookQuoteAdapter bookAdapter;
    private SongQuoteAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quotes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_Quotes);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_quotes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_quotes) return true;
            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_messages) startActivity(new Intent(this, MessagesActivity.class));
            else if (id == R.id.nav_community) startActivity(new Intent(this, CommunityActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });

        recyclerViewbook = findViewById(R.id.recycler_Quotesbooks); // ודאי שיש לך RecyclerView עם ID כזה ב-XML
        recyclerViewbook.setLayoutManager(new LinearLayoutManager(this));

        // 2. אתחול הרשימה והאדאפטר
        Bookmood = new ArrayList<>();
        BookQuoteadapter = new BookQuoteAdapter(Bookmood);
        recyclerViewbook.setAdapter(BookQuoteadapter);


        recyclerViewsong = findViewById(R.id.recycler_Quotessongs); // ודאי שיש לך RecyclerView עם ID כזה ב-XML
        recyclerViewsong.setLayoutManager(new LinearLayoutManager(this));

        // 2. אתחול הרשימה והאדאפטר
        Songmood = new ArrayList<>();
        SongQuoteadapter = new SongQuoteAdapter(Songmood);
        recyclerViewsong.setAdapter(SongQuoteadapter);
        btnInLove = findViewById(R.id.btnInLove);
        btnSad = findViewById(R.id.btnSad);
        btnHappy = findViewById(R.id.btnHappy);
        btnInspirational = findViewById(R.id.btnInspirational);
        btnHeartbroken = findViewById(R.id.btnHeartbroken);
        btnHope = findViewById(R.id.btnHope);
        btnAll = findViewById(R.id.btnAll);


        // 3. אתחול Firestore
        db = FirebaseFirestore.getInstance();


        btnInLove.setOnClickListener(v -> filterAllByMood("In Love"));
        btnSad.setOnClickListener(v -> filterAllByMood("Sad"));
        btnHappy.setOnClickListener(v -> filterAllByMood("Happy"));
        btnInspirational.setOnClickListener(v -> filterAllByMood("Inspirational"));
        btnHeartbroken.setOnClickListener(v -> filterAllByMood("Heartbroken"));
        btnHope.setOnClickListener(v -> filterAllByMood("Hope"));


// כפתור הכל - פשוט קורא לפונקציות הטעינה המקוריות שלך (בלי where)
        btnAll.setOnClickListener(v -> {
            // קריאה לפונקציות הקיימות שלך
            loadBooksQuotesFromFirestore();
            loadSongsQuotesFromFirestore();

            // חיבור מחדש של האדפטר לרשימה המקורית (זה מה שמשחרר את ה"תקיעה" מהסינון)
            BookQuoteadapter.setList(Bookmood);
            SongQuoteadapter.setList(Songmood);
        });

        // 4. משיכת הנתונים
        loadBooksQuotesFromFirestore();
        loadSongsQuotesFromFirestore();

    }
    private void loadBooksQuotesFromFirestore() {
        db.collection("BookQuotes") // השם המדויק של ה-Collection מהתמונה הראשונה
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ניקוי הרשימה כדי שלא יהיו כפילויות
                        Bookmood.clear();

                        // מעבר על כל המסמכים שהגיעו מהדאטאבייס
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // המרת המסמך לאובייקט BookQuote
                            BookQuote quote = document.toObject(BookQuote.class);
                            if (quote != null) {
                                Bookmood.add(quote);
                            }
                        }

                        // עדכון האדאפטר שהנתונים הגיעו
                        BookQuoteadapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // כאן אפשר להוסיף הודעת שגיאה (Toast) למקרה שמשהו השתבש
                    Toast.makeText(this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSongsQuotesFromFirestore() {
        db.collection("SongQuotes") // השם המדויק של ה-Collection מהתמונה הראשונה
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ניקוי הרשימה כדי שלא יהיו כפילויות
                        Songmood.clear();

                        // מעבר על כל המסמכים שהגיעו מהדאטאבייס
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            SongQuote quote = document.toObject(SongQuote.class);
                            if (quote != null) {
                                Songmood.add(quote);
                            }
                        }

                        // עדכון האדאפטר שהנתונים הגיעו
                        SongQuoteadapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // כאן אפשר להוסיף הודעת שגיאה (Toast) למקרה שמשהו השתבש
                    Toast.makeText(this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                });
    }


    private void filterAllByMood(String moodValue) {
        // 1. סינון ספרים
        db.collection("BookQuotes")
                .whereArrayContains("mood", moodValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // המרת המסמכים ישירות לרשימה
                    List<BookQuote> filteredBooks = queryDocumentSnapshots.toObjects(BookQuote.class);

                    // עדכון הרשימה המקורית והאדפטר
                    Bookmood.clear();
                    Bookmood.addAll(filteredBooks);
                    BookQuoteadapter.notifyDataSetChanged();

                    // במידה והאדפטר איבד רפרנס, נעדכן אותו ישירות
                    BookQuoteadapter.setList(Bookmood);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בסינון ספרים", Toast.LENGTH_SHORT).show());

        // 2. סינון שירים
        db.collection("SongQuotes")
                .whereArrayContains("mood", moodValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SongQuote> filteredSongs = queryDocumentSnapshots.toObjects(SongQuote.class);

                    Songmood.clear();
                    Songmood.addAll(filteredSongs);
                    SongQuoteadapter.notifyDataSetChanged();

                    // עדכון ישיר של האדפטר
                    SongQuoteadapter.setList(Songmood);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בסינון שירים", Toast.LENGTH_SHORT).show());
    }

}