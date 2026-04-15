package com.example.harmonia;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class ChatSummary {
    public String chatId;
    public String partnerId; // ה-ID של האדם שאיתו מדברים
    public String partnerName; // השם שיוצג ברשימה
    public String lastMessage;
    public List<String> users;
    public String userProfilepic;

    public Object timestamp; // שינינו ל-Object כדי לקבל גם Timestamp וגם Long בלי קריסה

    public ChatSummary() {} // חובה עבור Firebase

    // בנאי עבור MessagesActivity
    public ChatSummary(String partnerId, String partnerName, String lastMessage) {
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.lastMessage = lastMessage;

    }

    // הבנאי המלא
    public ChatSummary(String chatId, List<String> users, String lastMessage, Object timestamp, String userProfilepic ) {
        this.chatId = chatId;
        this.users = users;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.userProfilepic = userProfilepic;

    }

    // פונקציית עזר למקרה שתצטרכי את הזמן כ-long (למשל למיון)
    @Exclude
    public long getTimestampAsLong() {
        if (timestamp instanceof Long) {
            return (long) timestamp;
        } else if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).getSeconds() * 1000;
        }
        return 0;
    }
}