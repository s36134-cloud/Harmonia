package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.UserImageSelector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth auth;

    private UserImageSelector userImageSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // 1. קישור ל-RecyclerView (ודאי שה-ID ב-XML הוא recyclerView)
        RecyclerView recyclerView = findViewById(R.id.my_horizontal_recycler);

// 2. יצירת רשימת הנתונים
        List<ItemModel> itemList = new ArrayList<>();
        itemList.add(new ItemModel("כרטיס 1"));
        itemList.add(new ItemModel("כרטיס 2"));
        itemList.add(new ItemModel("כרטיס 3"));

// 3. הגדרה שהתצוגה תהיה אופקית (מצד לצד)
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

// 4. חיבור האדפטר שיצרת לרשימה
        HorizontalAdapter adapter = new HorizontalAdapter(itemList);
        recyclerView.setAdapter(adapter);







        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_Profile);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_profile); // מסמן את דף הפרופיל

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;

            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_messages) startActivity(new Intent(this, MessagesActivity.class));
            else if (id == R.id.nav_community) startActivity(new Intent(this, CommunityActivity.class));

            overridePendingTransition(0, 0);
            return true;
        });


        Button signoutButton = findViewById(R.id.signout_button);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent=new Intent(ProfileActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();

            }

        });

        ImageView profilePictureImageView = findViewById(R.id.imageView);
        userImageSelector = new UserImageSelector(this, profilePictureImageView);
        Button choosePictureButton = findViewById(R.id.btn_choose_picture);
        choosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImageSelector.showImageSourceDialog();
            }
        });
    }
}