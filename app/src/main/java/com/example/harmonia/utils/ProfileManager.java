package com.example.harmonia.utils;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class ProfileManager {
    private static final String TAG = "RegistrationManager";


    String userId;
    File imageFile;

    FirebaseAuth auth;


    Activity activity;


    public ProfileManager(Activity activity) {
        Log.d(TAG, "RegistrationManager: started");
        this.activity = activity;


        auth = FirebaseAuth.getInstance();

    }



    public interface OnResultCallback {
        void onResult(boolean success, String message);
    }


    private void uploadProfilePictureToSupabase() {
        if (imageFile == null) {
            Log.d(TAG, "uploadProfilePictureToSupabase: no image file provided");
            //phaseDone();
            return;
        }

        String filename = "images/profile-pics/" + userId + ".jpg";
        Log.i(TAG, "Uploading file to Supabase: " + filename);

        SupabaseStorageHelper.uploadPicture(imageFile, filename, new SupabaseStorageHelper.OnResultCallback() {
            @Override
            public void onResult(boolean success, String url, String error) {
                if (success) {
                    Log.i(TAG, "Profile picture uploaded successfully to Supabase. Public URL: " + url);
                    //phaseDone();
                } else {
                    Log.e(TAG, "Supabase upload failed: " + error);
                    //phaseFailed("Failed to upload profile picture (Supabase): " + error);
                }
            }
        });

    }


    private void saveUserToFirestore() {

        //phaseDone();
    }

}
