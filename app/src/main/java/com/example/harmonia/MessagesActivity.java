package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_messages);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_messages);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_messages); // מסמן את דף ההודעות

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_messages) return true;

            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_community) startActivity(new Intent(this, CommunityActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));

            overridePendingTransition(0, 0);
            return true;
        });

        // הגדרת הכפתור
        Button startconvButton = findViewById(R.id.start_conv_button); // ודאי שזה ה-ID של הכפתור שלך

        startconvButton.setOnClickListener(v -> {
            // השגת ה-ID של המשתמש המחובר
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // שליפת הנתונים של המשתמש הנוכחי כדי להעביר ל-AI
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // חילוץ הרשימות של הז'אנרים (ודאי שהשמות ב-Firestore תואמים לאלו)
                            ArrayList<String> musicGenres = (ArrayList<String>) documentSnapshot.get("musicGenres");
                            ArrayList<String> bookGenres = (ArrayList<String>) documentSnapshot.get("bookGenres");
                            String userName = documentSnapshot.getString("displayName");

                            // מעבר לעמוד החיפוש עם הנתונים בתוך ה-Intent
                            Intent intent = new Intent(MessagesActivity.this, SearchChatActivity.class);
                            intent.putStringArrayListExtra("myMusic", musicGenres);
                            intent.putStringArrayListExtra("myBooks", bookGenres);
                            intent.putExtra("myName", userName);

                            startActivity(intent);
                            // לא חייב לעשות finish() אם את רוצה שהמשתמש יוכל לחזור אחורה בקלות
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MessagesActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}