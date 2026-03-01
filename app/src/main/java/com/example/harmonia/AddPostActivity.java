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
    private String selectedImageUrl = null; // שמירת ה-URL של התמונה שנבחרה

    private static final String TAG = "AddPostActivity";

    // ActivityResultLauncher לקבלת התוצאה מ-SearchBookSongPicActivity
    private final ActivityResultLauncher<Intent> searchBookSongLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUrl = result.getData().getStringExtra("POST_IMAGE_URL");
                    if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
                        imageView.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(selectedImageUrl)
                                .placeholder(R.drawable.transparent_placeholder) // 👈 שקוף!
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

        // בהתחלה, הסתר את ה-ImageView
        imageView.setVisibility(View.GONE);

        Button sendButton = findViewById(R.id.btn_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendpost();
            }
        });

        Button booksongButton = findViewById(R.id.btn_picture);
        booksongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddPostActivity.this, SearchBookSongPicActivity.class);
                searchBookSongLauncher.launch(intent); // שימוש ב-launcher במקום startActivity
            }
        });

        Button BacktocommunityButton = findViewById(R.id.Back_to_community);
        BacktocommunityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddPostActivity.this, CommunityActivity.class);
                startActivity(intent);
            }
        });
    }

    public void sendpost() {
        Log.d(TAG, "sendPost: start");

        // בדיקה שהשדות לא ריקים
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

        HarmoniaPost post = createHarmoniaPost();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    Toast.makeText(AddPostActivity.this, "Post saved successfully!", Toast.LENGTH_SHORT).show();

                    // מעבר ל-CommunityActivity
                    Intent intent = new Intent(AddPostActivity.this, CommunityActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(AddPostActivity.this, "Error saving post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        Log.d(TAG, "sendPost: done");
    }

    public HarmoniaPost createHarmoniaPost() {
        String title = titleEditText.getText().toString().trim();
        String description = descEditText.getText().toString().trim();

        String ownerUid = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String ownerNickname = sharedPref.getString("nickname", "Anonymous");

        Timestamp creationTimestamp = new Timestamp(new Date());

        // יצירת פוסט עם התמונה
        HarmoniaPost newPost = new HarmoniaPost(title, description, ownerUid, ownerNickname, creationTimestamp, selectedImageUrl);

        // הוספת ה-imageUrl לפוסט (אם קיים)
        if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
            newPost.setImageUrl(selectedImageUrl);
            Log.d(TAG, "Post created with image: " + selectedImageUrl);
        }

        Log.d(TAG, "Post created: " + title + " at " + creationTimestamp.toDate());

        return newPost;
    }
}