package com.example.harmonia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.harmonia.utils.HarmoniaPost;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class AddPostActivity extends AppCompatActivity {
    String name;
    private TextInputEditText titleEditText;
    private TextInputEditText descEditText;
    private ImageView imageView;
    private String selectedImageUrl = null;

    private static final String TAG = "AddPostActivity";

    private final ActivityResultLauncher<Intent> searchBookSongLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUrl = result.getData().getStringExtra("POST_IMAGE_URL");
                    if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
                        imageView.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(selectedImageUrl)
                                .placeholder(R.drawable.transparent_placeholder)
                                .error(R.drawable.pic_image)
                                .into(imageView);
                        Log.d(TAG, "Image URL received: " + selectedImageUrl);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleEditText = findViewById(R.id.et_title);
        descEditText = findViewById(R.id.et_description);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);

        // ניקוי nickname ישן כדי לטעון מחדש מ-Firestore
        getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().remove("nickname").apply();

        Button sendButton = findViewById(R.id.btn_send);
        sendButton.setOnClickListener(v -> sendpost());

        Button booksongButton = findViewById(R.id.btn_picture);
        booksongButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddPostActivity.this, SearchBookSongPicActivity.class);
            searchBookSongLauncher.launch(intent);
        });

        Button BacktocommunityButton = findViewById(R.id.Back_to_community);
        BacktocommunityButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddPostActivity.this, CommunityActivity.class);
            startActivity(intent);
        });
    }

    public void sendpost() {
        Log.d(TAG, "sendPost: start");

        String title = titleEditText.getText().toString().trim();
        String description = descEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        Log.d(TAG, "ownerUid: " + ownerUid);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String cachedNickname = sharedPref.getString("nickname", null);

        Log.d(TAG, "cachedNickname: " + cachedNickname);

        if (cachedNickname != null) {
            savePost(title, description, ownerUid, cachedNickname);
        } else {
            Log.d(TAG, "Loading nickname from Firestore...");
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(ownerUid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String nickname = doc.getString("name");
                        Log.d(TAG, "Nickname from Firestore: " + nickname);
                        if (nickname == null) nickname = "Anonymous";
                        sharedPref.edit().putString("nickname", nickname).apply();
                        savePost(title, description, ownerUid, nickname);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load nickname: " + e.getMessage());
                        Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void savePost(String title, String description, String ownerUid, String nickname) {
        Timestamp creationTimestamp = new Timestamp(new Date());
        HarmoniaPost newPost = new HarmoniaPost(title, description, ownerUid, nickname, creationTimestamp, selectedImageUrl);

        if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
            newPost.setImageUrl(selectedImageUrl);
        }

        FirebaseFirestore.getInstance().collection("posts")
                .add(newPost)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Post saved with ID: " + documentReference.getId());
                    Toast.makeText(AddPostActivity.this, "Post saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddPostActivity.this, CommunityActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(AddPostActivity.this, "Error saving post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}