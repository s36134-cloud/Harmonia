package com.example.harmonia;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Conv {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;

    // בנאי ריק חובה עבור Firebase
    public Conv() {
    }

    public Conv(String senderId, String receiverId, String message, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters ו-Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // פונקציה אופציונלית להצגת זמן בפורמט נוח (למשל 12:45)
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}