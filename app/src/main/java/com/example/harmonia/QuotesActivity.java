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



        // 3. אתחול Firestore
        db = FirebaseFirestore.getInstance();

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

}