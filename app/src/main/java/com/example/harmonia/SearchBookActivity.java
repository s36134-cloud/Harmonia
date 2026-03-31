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

import com.example.harmonia.utils.BooksAdapter;
import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchBookActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BooksAdapter adapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private String listId; // ID של הרשימה הספציפית

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_book);

        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();

        // בדיקה: האם הגענו כדי לעדכן רשימה ספציפית או את הפרופיל?
        listId = getIntent().getStringExtra("LIST_ID");
        boolean isSelectionMode = getIntent().getBooleanExtra("IS_SELECTION_MODE", false);

        final Button btnDone = findViewById(R.id.btnDoneBooks);
        btnDone.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));


        adapter = new BooksAdapter(bookList, book -> {
            // האדאפטר משנה את ה-isSelected בלחיצה, כאן רק נעדכן את הכפתור
            boolean hasSelection = false;
            for (Book b : bookList) {
                if (b.isSelectedbook()) {
                    hasSelection = true;                    break;
                }
            }
            btnDone.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        }, R.layout.book);






        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchViewbook);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchInFirebase(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllBooks();
                } else {
                    searchInFirebase(newText);
                }
                return true;
            }
        });

        btnDone.setOnClickListener(v -> {
            List<String> selectedBookIds = new ArrayList<>();
            for (Book b : bookList) {
                if (b.isSelectedbook()) {
                    selectedBookIds.add(b.getId());
                }
            }

            if (selectedBookIds.isEmpty()) {
                Toast.makeText(this, "אנא בחרי לפחות ספר אחד", Toast.LENGTH_SHORT).show();
                return;
            }

            // החלטה לאן לשמור לפי ה-ID שקיבלנו
            if (listId != null) {
                saveToSpecificList(selectedBookIds);
            } else {
                saveSelectedBooksAndGoToProfile(selectedBookIds);
            }
        });

        ImageView backbookImageView = findViewById(R.id.imageViewbooksearch);
        backbookImageView.setOnClickListener(v -> finish()); // סגירת המסך וחזרה

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAllBooks();
    }

    private void saveToSpecificList(List<String> bookIds) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> listData = new HashMap<>();
        listData.put("itemsIds", bookIds);
        listData.put("lastUpdated", FieldValue.serverTimestamp());

        // שמירה תחת users -> {uid} -> my_custom_lists -> {listId}
        db.collection("users").document(userId)
                .collection("my_custom_lists").document(listId)
                .set(listData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הרשימה עודכנה בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show());
    }

    private void saveSelectedBooksAndGoToProfile(List<String> bookIds) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("users").document(userId)
                .update("topBooks", FieldValue.arrayUnion(bookIds.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הספרים נוספו לפרופיל!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("topBooks", bookIds);
                    db.collection("users").document(userId).set(data, SetOptions.merge())
                            .addOnSuccessListener(v -> finish());
                });
    }

    private void searchInFirebase(String searchText) {
        db.collection("books")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        bookList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            bookList.add(book);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadAllBooks() {
        db.collection("books")
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        bookList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            bookList.add(book);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}