package com.example.harmonia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harmonia.utils.ListsAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListsActivity extends AppCompatActivity {

    private static final String supabaseUrl = "https://nbliklmpfsjemwizicuh.supabase.co";
    private static final String supabaseKey = "sb_secret_xk7mI10aGweDiA3TXm8Qgw_FrJAsMd-";
    private static final String SUPABASE_BUCKET = "Harmonia-bucket";

    private RecyclerView recyclerViewlists;
    private ListsAdapter listsAdapter;
    private List<UserList> userLists = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    private Uri selectedImageUri = null;
    private ImageView ivListImagePreview;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        recyclerViewlists = findViewById(R.id.recyclerViewlists);
        recyclerViewlists.setLayoutManager(new LinearLayoutManager(this));
        listsAdapter = new ListsAdapter(userLists, userList -> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("listId", userList.getId());
            intent.putExtra("listType", userList.getType());
            intent.putExtra("listName", userList.getName());
            startActivity(intent);
        });
        recyclerViewlists.setAdapter(listsAdapter);

        ImageView BacktoprofilefromlistsImageView = findViewById(R.id.Back_to_profilefromlists);
        BacktoprofilefromlistsImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ListsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.create_list).setOnClickListener(v -> showCreateListDialog());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (ivListImagePreview != null)
                            ivListImagePreview.setImageURI(selectedImageUri);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (ivListImagePreview != null && selectedImageUri != null)
                            ivListImagePreview.setImageURI(selectedImageUri);
                    }
                });

        loadLists();
    }

    private void showCreateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_list, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextInputEditText etName = dialogView.findViewById(R.id.list_name);
        TextInputEditText etDescription = dialogView.findViewById(R.id.list_description);
        RadioGroup radioGroupType = dialogView.findViewById(R.id.radioGroupType);
        ivListImagePreview = dialogView.findViewById(R.id.ivListImagePreview);
        Button btnGallery = dialogView.findViewById(R.id.btnPickGallery);
        Button btnCamera = dialogView.findViewById(R.id.btnPickCamera);
        Button btnCreate = dialogView.findViewById(R.id.Create_list);

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnCamera.setOnClickListener(v -> {
            selectedImageUri = createTempImageUri();
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImageUri);
            cameraLauncher.launch(intent);
        });

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Please enter name");
                return;
            }

            int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
            String type = (selectedTypeId == R.id.radioSongs) ? "songs" : "books";

            if (selectedImageUri != null) {
                uploadImageToSupabase(name, description, type, dialog);
            } else {
                createListInFirestore(name, description, type, "", dialog);
            }
        });

        dialog.show();
    }

    private Uri createTempImageUri() {
        java.io.File tempFile = new java.io.File(getCacheDir(), "temp_list_image.jpg");
        return androidx.core.content.FileProvider.getUriForFile(
                this, getPackageName() + ".provider", tempFile);
    }

    private void uploadImageToSupabase(String name, String description, String type, AlertDialog dialog) {
        try {
            String fileName = "images/lists/" + currentUserId + "/" + UUID.randomUUID() + ".jpg";
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + fileName;

            java.io.InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            new Thread(() -> {
                try {
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                            new java.net.URL(uploadUrl).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "image/jpeg");
                    connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);
                    connection.setDoOutput(true);
                    connection.getOutputStream().write(imageBytes);

                    int responseCode = connection.getResponseCode();
                    String publicUrl = supabaseUrl + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + fileName;

                    runOnUiThread(() -> {
                        if (responseCode == 200 || responseCode == 201) {
                            createListInFirestore(name, description, type, publicUrl, dialog);
                        } else {
                            Toast.makeText(this, "שגיאה בהעלאת תמונה: " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בקריאת התמונה", Toast.LENGTH_SHORT).show();
        }
    }

    private void createListInFirestore(String name, String description, String type, String imageUrl, AlertDialog dialog) {
        UserList newList = new UserList(name, description, type, imageUrl);

        db.collection("users").document(currentUserId)
                .collection("lists")
                .add(newList)
                .addOnSuccessListener(ref -> {
                    dialog.dismiss();
                    selectedImageUri = null;
                    Toast.makeText(this, "הרשימה נוצרה!", Toast.LENGTH_SHORT).show();
                    loadLists();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה ביצירת רשימה", Toast.LENGTH_SHORT).show());
    }

    private void loadLists() {
        db.collection("users").document(currentUserId)
                .collection("lists")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userLists.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserList userList = doc.toObject(UserList.class);
                        if (userList != null) {
                            userList.setId(doc.getId());
                            userLists.add(userList);
                        }
                    }
                    listsAdapter.updateList(userLists);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת רשימות", Toast.LENGTH_SHORT).show());
    }
}