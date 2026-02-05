package com.example.harmonia;

import android.os.Bundle;
import android.widget.Button;
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

import com.example.harmonia.utils.BooksAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchBookActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BooksAdapter adapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_book);

        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new BooksAdapter(bookList);
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
                    // אם המשתמש מחק הכל - תציג שוב את כל השירים
                    loadAllBooks();
                } else if (newText.length() > 0) {
                    // אם יש טקסט - תבצע חיפוש
                    searchInFirebase(newText);
                }
                return true;
            }
        });
            Button btnDone = findViewById(R.id.btnDoneBooks);
        btnDone.setVisibility(android.view.View.GONE);
        btnDone.setOnClickListener(v -> {
                // 1. יצירת רשימה של ה-IDs של השירים שנבחרו
                List<String> selectedBookIds = new ArrayList<>();
                for (Book b : bookList) {
                    if (b.isSelectedbook()) {
                        selectedBookIds.add(b.getId());
                    }
                }

                // 2. בדיקה אם המשתמש בחר שירים
                if (selectedBookIds.isEmpty()) {
                    Toast.makeText(this, "אנא בחרי לפחות שיר אחד", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. שמירה ל-Firebase ומעבר מסך
                saveSelectedBooksAndGoToProfile(selectedBookIds);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAllBooks();
    }
    private void searchInFirebase(String searchText) {


        db.collection("books")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        if (task.getResult().isEmpty()) {
                            android.util.Log.d("SEARCH_DEBUG", "No books found for: " + searchText);
                        } else {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                book.setId(document.getId());
                                bookList.add(book);
                            }
                            android.util.Log.d("SEARCH_DEBUG", "Found " + bookList.size() + " books!");
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void saveSelectedBooksAndGoToProfile(List<String> bookIds) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        // במקום ליצור Map חדש ולדרוס, אנחנו משתמשים ב-arrayUnion
        db.collection("users").document(userId)
                .update("topBooks", com.google.firebase.firestore.FieldValue.arrayUnion(bookIds.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "השירים נוספו בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // חוזר לפרופיל
                })
                .addOnFailureListener(e -> {
                    // אם המסמך לא קיים בכלל, update עלול להיכשל, אז נשתמש ב-set כגיבוי
                    db.collection("users").document(userId)
                            .set(new java.util.HashMap<String, Object>() {{
                                put("topBooks", bookIds);
                            }}, com.google.firebase.firestore.SetOptions.merge());
                });
    }


        private void loadAllBooks() {
        db.collection("books")
                .orderBy("name") // מסדר אותם לפי א'-ב'
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        if (task.getResult() != null) {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                book.setId(document.getId());
                                bookList.add(book);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        android.util.Log.d("SEARCH_DEBUG", "Loaded all " + bookList.size() + " books");
                    } else {
                        android.util.Log.e("SEARCH_DEBUG", "Error loading books: ", task.getException());
                    }
                });
    }
}