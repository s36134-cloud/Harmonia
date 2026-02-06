package com.example.harmonia;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.CheckBoxAdapter;

import java.util.ArrayList;
import java.util.List;

public class GenreSelectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CheckBoxAdapter adapter;
    private List<CheckBox> genreList;
    private com.google.firebase.firestore.FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_genre_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        genreList = new ArrayList<>();

        recyclerView = findViewById(R.id.genresRecyclerView); // וודאי שזה ה-ID ב-XML
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        adapter = new CheckBoxAdapter(genreList);
        recyclerView.setAdapter(adapter);

        loadGenresFromFirestore(); // קריאה לפונקציה שתשלוף את הנתונים
    }

    private void loadGenresFromFirestore() {
        db.collection("bookGenres") // השם המעודכן של הקולקשיין
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    genreList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // אנחנו לוקחים את ה-ID של המסמך כי אמרת שהוא שם הז'אנר
                        String genreName = document.getId();
                        CheckBox genre = new CheckBox(genreName);
                        genreList.add(genre);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FirestoreError", "Error: " + e.getMessage());
                });
    }

}