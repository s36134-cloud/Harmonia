package com.example.harmonia;

import java.util.List;

public class ChatSummary {
    public String chatId;
    public String partnerId; // ה-ID של האדם שאיתו מדברים
    public String partnerName; // השם שיוצג ברשימה
    public String lastMessage;
    public List<String> users;
    public long timestamp;

    public ChatSummary() {} // חובה עבור Firebase

    // הוסיפי את הבנאי הזה - הוא יפתור את השגיאה ב-MessagesActivity
    public ChatSummary(String partnerId, String partnerName, String lastMessage) {
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.lastMessage = lastMessage;
    }

    // הבנאי המלא (היה קיים כבר)
    public ChatSummary(String chatId, List<String> users, String lastMessage, long timestamp) {
        this.chatId = chatId;
        this.users = users;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }
}