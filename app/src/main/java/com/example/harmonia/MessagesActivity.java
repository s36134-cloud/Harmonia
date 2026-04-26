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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.ChatSummaryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatSummaryAdapter adapter;
    private List<ChatSummary> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_messages);

        recyclerView = findViewById(R.id.recyclerViewchats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_messages);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_messages);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_messages) return true;

            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_community) startActivity(new Intent(this, CommunityActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            else if (id == R.id.nav_quotes) startActivity(new Intent(this, QuotesActivity.class));

            overridePendingTransition(0, 0);
            return true;
        });

        Button startconvButton = findViewById(R.id.start_conv_button);

        startconvButton.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> musicGenres = (ArrayList<String>) documentSnapshot.get("selectedSongGenres"); // תוקן
                            ArrayList<String> bookGenres = (ArrayList<String>) documentSnapshot.get("selectedBookGenres");  // תוקן
                            String userName = documentSnapshot.getString("nickname");                                        // תוקן

                            Intent intent = new Intent(MessagesActivity.this, SearchConvActivity.class);
                            intent.putStringArrayListExtra("myMusic", musicGenres);
                            intent.putStringArrayListExtra("myBooks", bookGenres);
                            intent.putExtra("myName", userName);

                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MessagesActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                    });
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        // כשהמסך מתעורר (גם בפעם הראשונה וגם כשחוזרים אליו), נפעיל את המאזין!
        loadUserChats();
    }


    private void loadUserChats() {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) return;

        FirebaseFirestore.getInstance().collection("chats")
                .whereArrayContains("users", myUid)
                // התוספת החשובה - מיון מובנה של פיירסטור
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener(this,(value, error) -> {
                    if (error != null) {
                        // אם חסר אינדקס (Index), השגיאה עם הקישור הכחול תודפס פה ב-Logcat!
                        android.util.Log.e("CHAT_ERROR", "Error loading chats", error);
                        return;
                    }
                    if (value == null) return;

                    chatList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        List<String> users = (List<String>) doc.get("users");
                        if (users == null || users.size() < 2) continue;

                        final String partnerId = users.get(0).equals(myUid) ? users.get(1) : users.get(0);
                        final String lastMsg = doc.getString("lastMessage") != null ? doc.getString("lastMessage") : "No messages yet";

                        // שליפת הזמן מהמסמך כדי שנוכל לסדר את הרשימה בסוף
                        final Object timestamp = doc.get("timestamp");

                        FirebaseFirestore.getInstance().collection("users").document(partnerId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String partnerName = userDoc.getString("nickname");
                                    if (partnerName == null || partnerName.isEmpty()) {
                                        partnerName = userDoc.getString("displayName"); // fallback
                                    }
                                    if (partnerName == null) partnerName = "Unknown User";

                                    // יצירת האובייקט והשמת הזמן (השדות שלך מוגדרים כ-public אז זה אפשרי)
                                    ChatSummary chatSummary = new ChatSummary(partnerId, partnerName, lastMsg);
                                    chatSummary.timestamp = timestamp;

                                    chatList.add(chatSummary);

                                    // הפתרון לאסינכרוניות: מיון הרשימה שלנו מהחדש לישן לפני שמציגים אותה
                                    chatList.sort((c1, c2) -> Long.compare(c2.getTimestampAsLong(), c1.getTimestampAsLong()));

                                    if (adapter == null) {
                                        adapter = new ChatSummaryAdapter(chatList);
                                        recyclerView.setAdapter(adapter);
                                    } else {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}