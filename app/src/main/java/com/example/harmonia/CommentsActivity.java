package com.example.harmonia;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.CommentsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentsAdapter adapter;
    private List<Comment> commentList;
    private EditText etComment;
    private ImageButton btnSend;

    private String postId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comments);

        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // קבלת ה-ID של הפוסט מה-Intent
        postId = getIntent().getStringExtra("POST_ID");

        // חיבור רכיבי ה-UI
        recyclerView = findViewById(R.id.recyclercomments);
        etComment = findViewById(R.id.comment);
        btnSend = findViewById(R.id.btn_sendcomment);

        // הגדרת ה-RecyclerView
        commentList = new ArrayList<>();
        adapter = new CommentsAdapter(commentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // כפתור חזור
        findViewById(R.id.backtoposts).setOnClickListener(v -> finish());

        // טעינת תגובות מה-Firestore
        listenForComments();

        // הגדרת לחיצה על שליחה
        btnSend.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (!text.isEmpty()) {
                sendComment(text);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void listenForComments() {
        if (postId == null) return;

        db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            commentList.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                        if (commentList.size() > 0) {
                            recyclerView.smoothScrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    private void sendComment(String text) {
        String currentUserId = mAuth.getCurrentUser().getUid();

        // 1. שליפת פרטי המשתמש כדי להצמיד לתגובה
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String userName = doc.getString("nickname"); // ודאי שזה השם ב-DB שלך
                String profileUrl = doc.getString("String profileUrl = null;");

                // 2. יצירת ה-ID והאובייקט לתגובה
                String commentId = db.collection("posts").document(postId)
                        .collection("comments").document().getId();

                Comment newComment = new Comment(commentId, currentUserId, userName, profileUrl, text);

                // 3. שמירת התגובה בתוך תת-האוסף (Sub-collection)
                db.collection("posts").document(postId).collection("comments").document(commentId)
                        .set(newComment)
                        .addOnSuccessListener(aVoid -> {
                            etComment.setText(""); // ניקוי השדה

                            // --- הנה הקסם שמעדכן את ה-Counter בפוסט עצמו ---
                            db.collection("posts").document(postId)
                                    .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
                                    .addOnSuccessListener(v -> {
                                        // כאן זה התעדכן ב-DB!
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }
}