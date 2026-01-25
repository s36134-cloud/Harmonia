package com.example.harmonia;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class InformationActivity extends AppCompatActivity {
    FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText ;

    private EditText nameEditText ;

    private EditText ageEditText ;

    private Spinner musicspinner;

    private Spinner booksSpinner;



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


//

        musicspinner = findViewById(R.id.et_spinnerMusicgenre);
        booksSpinner = findViewById(R.id.et_spinnerBookgenre);



    }

}