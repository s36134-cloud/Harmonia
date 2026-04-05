package com.example.harmonia;

import java.util.Date;

public class Comment {
    private String commentId;    // מזהה ייחודי של התגובה
    private String userId;       // מי כתב את התגובה
    private String userName;     // השם שיוצג (כדי לא לשלוף מה-DB כל פעם)
    private String userProfileUrl; // תמונת הפרופיל של המגיב
    private String text;         // תוכן התגובה
    private long timestamp;      // זמן כתיבת התגובה

    // חובה: בנאי ריק עבור Firebase Firestore
    public Comment() {
    }

    // בנאי נוח לשימוש בקוד
    public Comment(String commentId, String userId, String userName, String userProfileUrl, String text) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;
        this.text = text;
        this.timestamp = new Date().getTime(); // קובע את הזמן הנוכחי
    }

    // Getters ו-Setters (חובה כדי ש-Firebase יוכל לקרוא ולכתוב)
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserProfileUrl() { return userProfileUrl; }
    public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = userProfileUrl; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
