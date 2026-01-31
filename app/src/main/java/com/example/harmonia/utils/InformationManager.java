package com.example.harmonia.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InformationManager {
    private String name;
    private int age;

    String userId;

    FirebaseAuth auth;
    OnResultCallback onResultCallback;
    private static final String TAG = "InformationManager";

    public void startRegistration(String name, int age,OnResultCallback callback)
    {
        this.onResultCallback = onResultCallback;
        this.name = name;
        this.age = age;
    }
    public interface OnResultCallback {
        void onResult(boolean success, String message);
    }

    private void saveUserToFirestore() {


        Log.d(TAG, "Saving user to Firestore. UID: " + userId + ", Nickname: " + name + ", Age: " + age);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("age", age);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "User document created in Firestore for UID: " + userId);
                    //phaseDone();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data to Firestore", e);
                    //phaseFailed("Failed to save user data: " + e.getMessage());
                });

    }

}
