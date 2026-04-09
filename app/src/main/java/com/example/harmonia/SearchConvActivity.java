package com.example.harmonia;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchConvActivity extends AppCompatActivity {
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

        startProcess();
    }

    private void startProcess() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) {
            finish();
            return;
        }

        // 1. קבלת פרטי המשתמש שלי
        FirebaseFirestore.getInstance().collection("users").document(myUid).get()
                .addOnSuccessListener(myDoc -> {
                    String myName = myDoc.getString("nickname") != null ? myDoc.getString("nickname") : "User";
                    ArrayList<String> myMusic = (ArrayList<String>) myDoc.get("selectedSongGenres");
                    ArrayList<String> myBooks = (ArrayList<String>) myDoc.get("selectedBookGenres");

                    // 2. שליפת רשימת המשתמשים שכבר יש איתם צ'אט כדי לסנן אותם
                    fetchExistingChatsAndRunAI(myUid, myName, myMusic, myBooks);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchExistingChatsAndRunAI(String myUid, String myName, ArrayList<String> myMusic, ArrayList<String> myBooks) {
        // 1. חיפוש באוסף "chats" את כל השיחות שהמשתמש הנוכחי חלק מהן
        FirebaseFirestore.getInstance().collection("chats")
                .whereArrayContains("users", myUid)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Set<String> existingContactIds = new HashSet<>();

                    // איסוף כל ה-IDs של האנשים שכבר יש איתם שיחה
                    for (DocumentSnapshot chatDoc : querySnapshots.getDocuments()) {
                        List<String> participants = (List<String>) chatDoc.get("users");
                        if (participants != null) {
                            for (String id : participants) {
                                if (!id.equals(myUid)) {
                                    existingContactIds.add(id);
                                }
                            }
                        }
                    }

                    // 2. שליפת רשימת המשתמשים הפוטנציאליים מה-Firestore
                    FirebaseFirestore.getInstance().collection("users").limit(40).get()
                            .addOnSuccessListener(userDocs -> {
                                List<DocumentSnapshot> filteredUsers = new ArrayList<>();

                                for (DocumentSnapshot doc : userDocs.getDocuments()) {
                                    String userId = doc.getId();

                                    // תנאי הסינון: לא להוסיף את עצמי ולא להוסיף מי שכבר קיים בסט ה-existingContactIds
                                    if (!userId.equals(myUid) && !existingContactIds.contains(userId)) {
                                        filteredUsers.add(doc);
                                    }
                                }

                                // שמירת הרשימה המסוננת לטובת שמות המשתמשים מאוחר יותר
                                lastFetchedUsers = filteredUsers;

                                // 3. שליחת הרשימה ה"נקייה" בלבד לעיבוד של ג'מיני
                                if (!filteredUsers.isEmpty()) {
                                    runAIGenreMatching(myName, myMusic, myBooks, filteredUsers);
                                } else {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "אין משתמשים חדשים להציע כרגע", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Error fetching chats: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    private void processAIResult(String jsonResult, List<DocumentSnapshot> allUsers) {
        if (jsonResult == null) return;
        try {
            String cleanJson = jsonResult.replaceAll("```json", "").replaceAll("```", "").trim();
            JSONArray jsonArray = new JSONArray(cleanJson);
            List<RecommendedUser> recommendedList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // תומך גם ב-ID וגם ב-user_id למקרה שג'מיני יחליט לשנות
                String userId = obj.optString("ID", obj.optString("user_id", ""));
                int score = obj.optInt("score", 0);
                String reason = obj.optString("reason", "");

                Log.d(TAG, "processAIResult: userId: " + userId);
                Log.d(TAG, "processAIResult: score: " + score + ", reason: " + reason);
                if (score > 50) { // הורדתי קצת את הרף כדי שיהיו תוצאות
                    String realName = "User";
                    if (allUsers != null) {
                        for (DocumentSnapshot doc : allUsers) {
                            if (doc.getId().equals(userId)) {
                                realName = doc.getString("nickname") != null ? doc.getString("nickname") : doc.getString("name");
                                break;
                            }
                        }
                    }
                    Log.d(TAG, "processAIResult: real name: " + realName);
                    recommendedList.add(new RecommendedUser(userId, realName, score, reason));
                }
            }

            runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(new ChatAdapter(recommendedList));
            });

        } catch (JSONException e) {
            Log.e("AI_ERROR", "JSON parsing error: " + e.getMessage());
            runOnUiThread(() -> progressBar.setVisibility(View.GONE));
        }
    }

    private void runAIGenreMatching(String myName, ArrayList<String> myMusic, ArrayList<String> myBooks, List<DocumentSnapshot> allUsers) {
        if (allUsers.isEmpty()) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "אין משתמשים חדשים להציע", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        StringBuilder potentialMatchesStr = new StringBuilder();
        for (DocumentSnapshot doc : allUsers) {
            potentialMatchesStr.append("ID: ").append(doc.getId())
                    .append(", Name: ").append(doc.getString("nickname"))
                    .append(", Music: ").append(doc.get("selectedSongGenres"))
                    .append(", Books: ").append(doc.get("selectedBookGenres"))
                    .append("\n");
        }

        StringBuilder myMusicGenres = new StringBuilder();
        for (String genre : myMusic) {
            myMusicGenres.append(genre).append(", ");
        }

        StringBuilder myBooksGenres = new StringBuilder();
        for (String genre : myBooks) {
            myBooksGenres.append(genre).append(", ");
        }


        String prompt = "You are a matchmaking assistant. Rank these users for the user " + myName + ". " +
                myName + " likes these music genres: " + myMusicGenres + "\n" +
                myName + " likes these books genres: " + myBooksGenres + "\n" +
                "The score is (0-100) and based on shared interests. " +
                "if the user music genres list is empty, do not consider music taste for the matching." +
                "if the user books genres list is empty, do not consider books taste for the matching." +
                "Return ONLY a raw JSON array. Use keys: 'ID', 'score', 'reason'. " +
                "The 'reason' must be a very short phrase (max 6 words). " +
                "Users to rank:\n" + potentialMatchesStr;

        Log.d(TAG, "runAIGenreMatching: prompt: " + prompt);

        GeminiManager.getInstance().sendText(prompt, this, new GeminiManager.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                processAIResult(result, allUsers);
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SearchConvActivity.this, "AI Currently Unavailable", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}