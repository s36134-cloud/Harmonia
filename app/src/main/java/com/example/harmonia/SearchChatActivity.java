package com.example.harmonia;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.ChatAdapter;
import com.example.harmonia.utils.GeminiManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchChatActivity extends AppCompatActivity {
    private List<DocumentSnapshot> lastFetchedUsers;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private static final String TAG = "SearchConvActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_conv);

        recyclerView = findViewById(R.id.recyclerViewMatches);
        progressBar = findViewById(R.id.progressBar);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            final String myUid = currentUser.getUid();

            FirebaseFirestore.getInstance().collection("users").document(myUid).get()
                    .addOnSuccessListener(myDoc -> {
                        final String myName = myDoc.getString("nickname") != null ? myDoc.getString("nickname") : "User";
                        final ArrayList<String> myMusic = (ArrayList<String>) myDoc.get("selectedSongGenres");
                        final ArrayList<String> myBooks = (ArrayList<String>) myDoc.get("selectedBookGenres");

                        FirebaseFirestore.getInstance().collection("users")
                                .limit(20)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<DocumentSnapshot> allUsers = new ArrayList<>();
                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        // לא להראות את עצמי ברשימה
                                        if (!doc.getId().equals(myUid)) {
                                            allUsers.add(doc);
                                        }
                                    }
                                    lastFetchedUsers = allUsers;
                                    runAIGenreMatching(myName, myMusic, myBooks, allUsers);
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error fetching profile", Toast.LENGTH_SHORT).show();
                    });
        } else {
            finish();
        }
    }

    private void processAIResult(String jsonResult) {
        if (jsonResult == null) return;
        try {
            // ניקוי תגיות JSON אם קיימות
            String cleanJson = jsonResult.replaceAll("```json", "").replaceAll("```", "").trim();
            JSONArray jsonArray = new JSONArray(cleanJson);
            List<RecommendedUser> recommendedList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // ה-AI לפעמים שולח ID ולפעמים user_id, אנחנו בודקים את שניהם
                String userId = obj.optString("ID", obj.optString("user_id", ""));
                int score = obj.optInt("score", 0);
                String reason = obj.optString("reason", "Great match based on your interests!");

                // סינון: רק מי שקיבל ציון של 50 ומעלה
                if (score > 0) {
                    String realName = "User";

                    if (lastFetchedUsers != null && !userId.isEmpty()) {
                        for (DocumentSnapshot doc : lastFetchedUsers) {
                            if (doc.getId().equals(userId)) {
                                realName = doc.getString("nickname");
                                if (realName == null || realName.isEmpty()) realName = doc.getString("name");
                                break;
                            }
                        }
                    }
                    recommendedList.add(new RecommendedUser(userId, realName, score, reason));
                }
            }

            runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                ChatAdapter adapter = new ChatAdapter(recommendedList);
                recyclerView.setAdapter(adapter);

                if (recommendedList.isEmpty()) {
                    Toast.makeText(this, "No matches found above 50%", Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e) {
            Log.e("AI_ERROR", "JSON parsing error: " + e.getMessage());
            runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            });
        }
    }

    private void runAIGenreMatching(String myName, ArrayList<String> myMusic, ArrayList<String> myBooks, List<DocumentSnapshot> allUsers) {
        StringBuilder potentialMatchesStr = new StringBuilder();

        for (DocumentSnapshot doc : allUsers) {
            String name = doc.getString("nickname");
            if (name == null || name.isEmpty()) name = doc.getString("name");

            Object music = doc.get("selectedSongGenres");
            Object books = doc.get("selectedBookGenres");

            potentialMatchesStr.append("ID: ").append(doc.getId())
                    .append(", Name: ").append(name)
                    .append(", Music: ").append(music != null ? music.toString() : "None")
                    .append(", Books: ").append(books != null ? books.toString() : "None")
                    .append("\n");
        }

        // הוספתי הנחיות קשיחות ל-AI להיות קצר ולעניין
        String prompt = "Rank these people for " + myName + " (0-100). " +
                "Return ONLY a JSON array: [{ID, score, reason}]. " +
                "CRITICAL: The 'reason' must be VERY short (max 7 words). " +
                "Example reason: 'Both love Rock music and Romance books'. " +
                "No full sentences, no 'Great match'. Just the common ground. " +
                "Potential matches:\n" + potentialMatchesStr;

        GeminiManager.getInstance().sendText(prompt, this, new GeminiManager.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                processAIResult(result);
            }

            // בתוך ה-onError של GeminiManager.sendText:
            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    String errorMessage = error.getMessage();
                    if (errorMessage != null && errorMessage.contains("quota")) {
                        Toast.makeText(SearchChatActivity.this, "המכסה של ה-AI הסתיימה, מציג רשימה ללא דירוג", Toast.LENGTH_LONG).show();

                        // פתרון חירום: הצגת כל המשתמשים שמצאנו ב-Firestore גם בלי הדירוג של ה-AI
                        List<RecommendedUser> fallbackList = new ArrayList<>();
                        for (DocumentSnapshot doc : allUsers) {
                            String name = doc.getString("nickname") != null ? doc.getString("nickname") : "User";
                            fallbackList.add(new RecommendedUser(doc.getId(), name, 0, "No AI ranking available right now"));
                        }
                        recyclerView.setAdapter(new ChatAdapter(fallbackList));

                    } else {
                        Toast.makeText(SearchChatActivity.this, "שגיאה בחיבור ל-AI", Toast.LENGTH_SHORT).show();
                        recyclerView.setAdapter(new ChatAdapter(new ArrayList<>()));
                    }
                });
            }
        });
    }
}