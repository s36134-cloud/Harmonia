package com.example.harmonia.utils;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class HarmoniaPost {
    private String title;
    private String description;
    private String ownerUid;
    private String ownerNickname;
    private Timestamp createdAt;
    private String imageUrl;
    private String PostId;

    // --- משתנים חדשים ללייקים ותגובות ---
    private int likesCount = 0;
    private int commentsCount = 0;
    private Map<String, Boolean> likedBy = new HashMap<>();
    // המפה שומרת: "מזהה משתמש" -> true/false

    public HarmoniaPost() {}

    public HarmoniaPost(String title, String description, String ownerUid, String ownerNickname, Timestamp createdAt, String imageUrl) {
        this.title = title;
        this.description = description;
        this.ownerUid = ownerUid;
        this.ownerNickname = ownerNickname;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        // כברירת מחדל הכל מתחיל ב-0
        this.likesCount = 0;
        this.commentsCount = 0;
        this.likedBy = new HashMap<>();
    }

    // --- Getters ו-Setters חדשים ---

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public Map<String, Boolean> getLikedBy() { return likedBy; }
    public void setLikedBy(Map<String, Boolean> likedBy) { this.likedBy = likedBy; }

    // --- שאר ה-Getters וה-Setters הקיימים שלך ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
    public String getOwnerNickname() { return ownerNickname; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPostId() { return PostId; }
    public void setPostId(String PostId) { this.PostId = PostId; }

    public boolean containsSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String lowerQuery = query.toLowerCase().trim();
        boolean inTitle = title != null && title.toLowerCase().contains(lowerQuery);
        boolean inDescription = description != null && description.toLowerCase().contains(lowerQuery);
        boolean inOwner = ownerNickname != null && ownerNickname.toLowerCase().contains(lowerQuery);
        return inTitle || inDescription || inOwner;
    }
}