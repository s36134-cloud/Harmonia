package com.example.harmonia;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;  // 👈 שימוש ב-SearchView
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harmonia.utils.HarmoniaPost;
import com.example.harmonia.utils.PostsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchCommActivity extends AppCompatActivity {

    private SearchView searchView;  // 👈 שונה מ-EditText
    private RecyclerView rvSearchResults;
    private TextView tvNoResults;

    private PostsAdapter adapter;
    private List<HarmoniaPost> allPosts;
    private List<HarmoniaPost> filteredPosts;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_comm);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        db = FirebaseFirestore.getInstance();
        allPosts = new ArrayList<>();
        filteredPosts = new ArrayList<>();
        setupRecyclerView();
        setupSearch();
        loadPosts();
    }

    private void initViews() {
        searchView = findViewById(R.id.et_search);  // 👈 SearchView
        rvSearchResults = findViewById(R.id.rv_search_results);
        tvNoResults = findViewById(R.id.tv_no_results);
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostsAdapter(filteredPosts);
        rvSearchResults.setAdapter(adapter);
    }

    private void setupSearch() {
        // 👇 שימוש ב-SearchView במקום TextWatcher
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);
                return false;
            }
        });
    }

    private void loadPosts() {
        tvNoResults.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);

        db.collection("posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allPosts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HarmoniaPost post = document.toObject(HarmoniaPost.class);
                        allPosts.add(post);
                    }

                    filteredPosts.clear();
                    filteredPosts.addAll(allPosts);
                    adapter.notifyDataSetChanged();

                    updateNoResultsVisibility();
                })
                .addOnFailureListener(e -> {
                    tvNoResults.setText("שגיאה בטעינת פוסטים: " + e.getMessage());
                    tvNoResults.setVisibility(View.VISIBLE);
                });
    }

    private void filterPosts(String query) {
        filteredPosts.clear();

        if (query.trim().isEmpty()) {
            filteredPosts.addAll(allPosts);
        } else {
            for (HarmoniaPost post : allPosts) {
                if (post.containsSearchQuery(query)) {
                    filteredPosts.add(post);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateNoResultsVisibility();
    }

    private void updateNoResultsVisibility() {
        if (filteredPosts.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);

            String query = searchView.getQuery().toString();  // 👈 שונה
            if (query.trim().isEmpty()) {
                tvNoResults.setText("אין פוסטים להצגה");
            } else {
                tvNoResults.setText("לא נמצאו פוסטים התואמים לחיפוש");
            }
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }
}