package com.example.harmonia;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    private EditText send;

    private static final String TAG = "AddPostActivity";
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


        Button sendButton = findViewById(R.id.btn_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendpost();
            }
        });
    }

    public void sendpost()
    {
        Log.d(TAG, "sendPost: start");
        HarmoniaPost post = createHarmoniaPost();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    Toast.makeText(AddPostActivity.this, "Log saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close this activity and return to FeedActivity
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(AddPostActivity.this, "Error saving log: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        Log.d(TAG, "sendPost: done");}

    public HarmoniaPost createHarmoniaPost() {
        // *** 1. איסוף הנתונים (החלף את ערכי ברירת המחדל בקריאה לשדות הקלט בפועל) ***

        // לדוגמה, קריאה משדות טקסט (צריך להגדיר את השדות לפני כן)
        String title = titleEditText.getText().toString();
        String description = descEditText.getText().toString();


        String ownerUid = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String ownerNickname = sharedPref.getString("nickname", "Anonymous");

        // *** 2. יצירת חותמת זמן עדכנית (Firebase Timestamp) ***
        Timestamp creationTimestamp = new Timestamp(new Date());

        HarmoniaPost newPost = new HarmoniaPost(title, description, ownerUid, ownerNickname, creationTimestamp);

        Log.d(TAG, "Post created: " + title + " at " + creationTimestamp.toDate());

        return newPost;
    }
}