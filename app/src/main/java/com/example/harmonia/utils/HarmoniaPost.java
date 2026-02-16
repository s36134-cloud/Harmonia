package com.example.harmonia.utils;
import com.google.firebase.Timestamp;

public class HarmoniaPost {
    private String title;
    private String description;
    private String ownerUid;
    private String ownerNickname;
    private Timestamp createdAt;
    private String imageUrl;


    public HarmoniaPost() {}

    public HarmoniaPost(String title, String description, String ownerUid, String ownerNickname, Timestamp createdAt, String imageUrl) {
        this.title = title;
        this.description = description;
        this.ownerUid = ownerUid;
        this.ownerNickname = ownerNickname;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }

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


    public boolean containsSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true; // אם אין שאילתה, להציג הכל
        }

        String lowerQuery = query.toLowerCase().trim();

        // חיפוש בכותרת, תיאור ושם הבעלים
        boolean inTitle = title != null && title.toLowerCase().contains(lowerQuery);
        boolean inDescription = description != null && description.toLowerCase().contains(lowerQuery);
        boolean inOwner = ownerNickname != null && ownerNickname.toLowerCase().contains(lowerQuery);

        return inTitle || inDescription || inOwner;
    }
}
