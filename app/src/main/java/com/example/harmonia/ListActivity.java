package com.example.harmonia;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harmonia.utils.BooksAdapter;
import com.example.harmonia.utils.SongsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewsongbooklist;
    private SearchView searchViewbookorsong;
    private FirebaseFirestore db;
    private String currentUserId, listId, listType;

    // רשימה לתוצאות החיפוש בלבד
    private List<Song> songResults = new ArrayList<>();
    private List<Book> bookResults = new ArrayList<>();

    // רשימה של מה שכבר קיים ברשימה (כדי שלא ייעלם)
    private List<Song> existingSongs = new ArrayList<>();
    private List<Book> existingBooks = new ArrayList<>();

    private SongsAdapter songsAdapter;
    private BooksAdapter booksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        listId = getIntent().getStringExtra("listId");
        listType = getIntent().getStringExtra("listType");
        String listName = getIntent().getStringExtra("listName");

        TextView yourList = findViewById(R.id.Your_list);
        yourList.setText(listName);

        findViewById(R.id.Back_to_community).setOnClickListener(v -> finish());

        recyclerViewsongbooklist = findViewById(R.id.recyclerViewsongbooklist);
        recyclerViewsongbooklist.setLayoutManager(new LinearLayoutManager(this));

        searchViewbookorsong = findViewById(R.id.searchViewbookorsong);

        setupAdapterAndSearch();
        loadExistingItems(); // טעינת הפריטים שכבר ברשימה בהתחלה
    }

    private void setupAdapterAndSearch() {
        if ("songs".equals(listType)) {
            // האדאפטר מציג את songResults (שמתחילה ריקה בחיפוש)
            songsAdapter = new SongsAdapter(songResults, song -> {
                addItemToList(song.getId());
                Toast.makeText(this, song.getName() + " נוסף!", Toast.LENGTH_SHORT).show();
            }, R.layout.song_list);
            recyclerViewsongbooklist.setAdapter(songsAdapter);

            searchViewbookorsong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        // אם החיפוש ריק - מציגים את מה שכבר היה ברשימה
                        songsAdapter.updateList(existingSongs);
                    } else {
                        searchSongs(newText);
                    }
                    return true;
                }
            });

        } else {
            booksAdapter = new BooksAdapter(bookResults, book -> {
                addItemToList(book.getId());
                Toast.makeText(this, book.getName() + " נוסף!", Toast.LENGTH_SHORT).show();
            }, R.layout.book_list);
            recyclerViewsongbooklist.setAdapter(booksAdapter);

            searchViewbookorsong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        booksAdapter.updateList(existingBooks);
                    } else {
                        searchBooks(newText);
                    }
                    return true;
                }
            });
        }
    }

    private void searchSongs(String query) {
        db.collection("songs")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshot -> {
                    songResults.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            song.setId(doc.getId());
                            songResults.add(song);
                        }
                    }
                    songsAdapter.updateList(songResults);
                });
    }

    private void searchBooks(String query) {
        db.collection("books")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshot -> {
                    bookResults.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Book book = doc.toObject(Book.class);
                        if (book != null) {
                            book.setId(doc.getId());
                            bookResults.add(book);
                        }
                    }
                    booksAdapter.updateList(bookResults);
                });
    }

    // טעינת הפריטים שכבר קיימים ברשימה ב-Firestore
    private void loadExistingItems() {
        db.collection("users").document(currentUserId)
                .collection("lists").document(listId) // וודאי שזה הנתיב הנכון אצלך
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        List<String> ids = (List<String>) value.get("itemIds");
                        if (ids != null && !ids.isEmpty()) {
                            fetchDetailsForExistingItems(ids);
                        }
                    }
                });
    }

    private void fetchDetailsForExistingItems(List<String> ids) {
        String collection = "songs".equals(listType) ? "songs" : "books";
        db.collection(collection).whereIn(FieldPath.documentId(), ids).get()
                .addOnSuccessListener(snapshot -> {
                    if ("songs".equals(listType)) {
                        existingSongs.clear();
                        for (DocumentSnapshot doc : snapshot) {
                            Song s = doc.toObject(Song.class);
                            s.setId(doc.getId());
                            existingSongs.add(s);
                        }
                        // אם החיפוש כרגע ריק, תציג את הקיימים
                        if (searchViewbookorsong.getQuery().toString().isEmpty()) {
                            songsAdapter.updateList(existingSongs);
                        }
                    } else {
                        existingBooks.clear();
                        for (DocumentSnapshot doc : snapshot) {
                            Book b = doc.toObject(Book.class);
                            b.setId(doc.getId());
                            existingBooks.add(b);
                        }
                        if (searchViewbookorsong.getQuery().toString().isEmpty()) {
                            booksAdapter.updateList(existingBooks);
                        }
                    }
                });
    }

    private void addItemToList(String itemId) {
        db.collection("users").document(currentUserId)
                .collection("lists").document(listId)
                .update("itemIds", FieldValue.arrayUnion(itemId));
    }
}