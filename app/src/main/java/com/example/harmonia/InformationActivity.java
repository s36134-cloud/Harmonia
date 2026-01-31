package com.example.harmonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.harmonia.utils.InformationManager;
import com.example.harmonia.utils.ProfileManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InformationActivity extends AppCompatActivity {
    FirebaseAuth auth;


    private  EditText nameEditText;
    private EditText ageEditText;


    private static final String TAG = "InformationActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        nameEditText=findViewById(R.id.et_name);
        ageEditText=findViewById(R.id.et_age);

        Button BackButton = findViewById(R.id.save_button);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InformationButtonClick ();

            }

        });
    } // <- Closing brace for onCreate was here

    private void InformationButtonClick() {
        Log.d(TAG, "Register button clicked");

        InformationManager registrationManager = new InformationManager();
        registrationManager.startRegistration(

                nameEditText.getText().toString(),
                Integer.parseInt(ageEditText.getText().toString()),

                new InformationManager.OnResultCallback() {
                    @Override
                    public void onResult(boolean success, String message) {
                        if (success) {
                            Toast.makeText(InformationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(InformationActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(InformationActivity.this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

    } // <--- **ADD THIS CLOSING BRACE**

    }// This is the closing brace for the InformationActivity class
