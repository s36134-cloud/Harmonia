package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
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
    private RecyclerView recyclerViewMyList;
    private SearchView searchViewbookorsong;
    private FirebaseFirestore db;
    private String currentUserId, listId, listType;

    private List<Song> songResults = new ArrayList<>();
    private List<Book> bookResults = new ArrayList<>();
    private List<Song> songsInList = new ArrayList<>();
    private List<Book> booksInList = new ArrayList<>();

    private SongsAdapter songsSearchAdapter;
    private SongsAdapter songsListAdapter;
    private BooksAdapter booksSearchAdapter;
    private BooksAdapter booksListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        listId = getIntent().getStringExtra("listId");
        listType = getIntent().getStringExtra("listType");
        String listName = getIntent().getStringExtra("listName");

        TextView yourList = findViewById(R.id.Your_list);
        yourList.setText(listName);

        ImageView BacktolistsImageView = findViewById(R.id.Back_to_lists);
        BacktolistsImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity.this, ListsActivity.class);
            startActivity(intent);
        });

        recyclerViewsongbooklist = findViewById(R.id.recyclerViewsongbooklist);
        recyclerViewMyList = findViewById(R.id.recyclerViewMyList);
        recyclerViewsongbooklist.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyList.setLayoutManager(new LinearLayoutManager(this));

        searchViewbookorsong = findViewById(R.id.searchViewbookorsong);

        setupAdapters();
        loadListItems();
    }

    private void setupAdapters() {
        if (listType.equals("songs")) {
            songsSearchAdapter = new SongsAdapter(songResults, song -> {
                addItemToList(song.getId());
                songsInList.add(song);
                songsListAdapter.updateList(songsInList);
                Toast.makeText(this, song.getName() + " נוסף לרשימה!", Toast.LENGTH_SHORT).show();
            }, R.layout.song_list);
            recyclerViewsongbooklist.setAdapter(songsSearchAdapter);

            songsListAdapter = new SongsAdapter(songsInList, song -> {}, R.layout.song_list);
            recyclerViewMyList.setAdapter(songsListAdapter);

            searchViewbookorsong.setQueryHint("search song...");
            searchViewbookorsong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!newText.isEmpty()) searchSongs(newText);
                    else {
                        songResults.clear();
                        songsSearchAdapter.updateList(songResults);
                    }
                    return true;
                }
            });

        } else {
            booksSearchAdapter = new BooksAdapter(bookResults, book -> {
                addItemToList(book.getId());
                booksInList.add(book);
                booksListAdapter.updateList(booksInList);
                Toast.makeText(this, book.getName() + " נוסף לרשימה!", Toast.LENGTH_SHORT).show();
            }, R.layout.book_list);
            recyclerViewsongbooklist.setAdapter(booksSearchAdapter);

            booksListAdapter = new BooksAdapter(booksInList, book -> {}, R.layout.book_list);
            recyclerViewMyList.setAdapter(booksListAdapter);

            searchViewbookorsong.setQueryHint("search book...");
            searchViewbookorsong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!newText.isEmpty()) searchBooks(newText);
                    else {
                        bookResults.clear();
                        booksSearchAdapter.updateList(bookResults);
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
                    songsSearchAdapter.updateList(songResults);
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
                    booksSearchAdapter.updateList(bookResults);
                });
    }

    private void addItemToList(String itemId) {
        db.collection("users").document(currentUserId)
                .collection("lists").document(listId)
                .update("itemIds", FieldValue.arrayUnion(itemId))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בהוספה לרשימה", Toast.LENGTH_SHORT).show());
    }

    private void loadListItems() {
        db.collection("users").document(currentUserId)
                .collection("lists").document(listId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> itemIds = (List<String>) doc.get("itemIds");
                    if (itemIds == null || itemIds.isEmpty()) return;

                    String collection = listType.equals("songs") ? "songs" : "books";
                    for (String itemId : itemIds) {
                        db.collection(collection).document(itemId).get()
                                .addOnSuccessListener(itemDoc -> {
                                    if (listType.equals("songs")) {
                                        Song song = itemDoc.toObject(Song.class);
                                        if (song != null) {
                                            song.setId(itemDoc.getId());
                                            songsInList.add(song);
                                            songsListAdapter.updateList(songsInList);
                                        }
                                    } else {
                                        Book book = itemDoc.toObject(Book.class);
                                        if (book != null) {
                                            book.setId(itemDoc.getId());
                                            booksInList.add(book);
                                            booksListAdapter.updateList(booksInList);
                                        }
                                    }
                                });
                    }
                });
    }
}