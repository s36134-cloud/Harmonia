package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.harmonia.utils.UserImageSelector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText ;

    private EditText nameEditText;

    private EditText ageEditText;



    private static final String TAG = "RegistrationActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvRegister = findViewById(R.id.already_have_account);

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת אינטנט למעבר מ-RegisterActivityל-LoginActivity
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        emailEditText=findViewById(R.id.email_edit_text);
        passwordEditText=findViewById(R.id.password_edit_text);
        nameEditText=findViewById(R.id.et_name);
        ageEditText=findViewById(R.id.et_age);

        Button registerButton = findViewById(R.id.registar_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButtonClick();

            }
        });


    }

    private void registerButtonClick() {
        Log.d(TAG, "Register button clicked");

        createUser(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                nameEditText.getText().toString(),
                Integer.valueOf(ageEditText.getText().toString())
        );
    }

    public void createUser(String email,
                           String password,
                           String name,
                           int age)
    {
        Log.d(TAG, "createUser: Creating user with Firebase Auth");

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)  ) {
            Log.w(TAG, "Validation failed: missing fields");
            Toast.makeText(RegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();

            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                Log.i(TAG, "Firebase Auth registration successful. UID: " + userId);
                                saveUserToFirestore(userId, name, age);

                            } else {
                                Log.e(TAG, "Firebase Auth registration succeeded but user is null");
                                Toast.makeText(RegistrationActivity.this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Firebase Auth registration failed", task.getException());
                            Toast.makeText(RegistrationActivity.this, task.getException() != null ? task.getException().getMessage() : "Unknown error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, int age) {

        Log.d(TAG, "Saving user to Firestore. UID: " + userId + ", Nickname: " + name + ", Age: " + age);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("age", age);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "User document created in Firestore for UID: " + userId);
                    Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data to Firestore", e);
                    Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }


}