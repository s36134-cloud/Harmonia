package com.example.harmonia;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.utils.ConvAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ConvActivity extends AppCompatActivity {

    private TextView tvPartnerName;
    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private RecyclerView recyclerView;
    private ConvAdapter adapter;
    private List<Conv> messageList = new ArrayList<>();
    private String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conv);

        // אתחול מזהה המשתמש שלי מראש
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvPartnerName = findViewById(R.id.tv_partner_name);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.recyclerViewMessages);

        // אתחול ה-RecyclerView
        adapter = new ConvAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // גורם להודעות להתחיל מלמטה (נוח יותר לצ'אט)
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // קבלת הנתונים מה-Intent
        String partnerId = getIntent().getStringExtra("userId");
        String partnerName = getIntent().getStringExtra("userName");

        Log.d("CHAT_DEBUG", "Partner ID received: " + partnerId);

        if (partnerId != null && !partnerId.isEmpty()) {
            if (tvPartnerName != null) {
                tvPartnerName.setText(partnerName != null ? partnerName : "צ'אט");
            }

            listenForMessages(partnerId);

            if (btnSend != null) {
                btnSend.setOnClickListener(v -> {
                    String msgText = etMessage.getText().toString().trim();
                    if (!msgText.isEmpty()) {
                        sendMessage(partnerId, msgText);
                        etMessage.setText(""); // ניקוי השדה מיד לאחר השליחה
                    }
                });
            }
        } else {
            Log.e("CHAT_DEBUG", "No partnerId found! Closing activity.");
            Toast.makeText(this, "שגיאה בטעינת השיחה", Toast.LENGTH_SHORT).show();
            finish();
        }

        // סידור ה-Padding של הסטטוס בר (תיקון למזהה ה-Root)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void listenForMessages(String partnerId) {
        String chatId = getChatId(myId, partnerId);
        Log.d("CHAT_DEBUG", "Listening to Chat ID: " + chatId);

        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CHAT_ERROR", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Conv msg = doc.toObject(Conv.class);
                            if (msg != null) {
                                messageList.add(msg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // גלילה אוטומטית להודעה האחרונה רק אם יש הודעות
                        if (!messageList.isEmpty()) {
                            recyclerView.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage(String partnerId, String text) {
        String chatId = getChatId(myId, partnerId);

        // יצירת אובייקט הודעה
        Conv newMessage = new Conv(myId, partnerId, text, System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .add(newMessage)
                .addOnFailureListener(e -> {
                    Log.e("CHAT_ERROR", "Failed to send message", e);
                    Toast.makeText(this, "שליחה נכשלה", Toast.LENGTH_SHORT).show();
                });
    }

    private String getChatId(String uid1, String uid2) {
        if (uid1 == null || uid2 == null || uid1.isEmpty() || uid2.isEmpty()) return "invalid_id";
        // אלגוריתם ליצירת ID ייחודי וקבוע לשני המשתמשים
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }
}