package com.example.harmonia;


import java.util.List;

public class Playlist {
    private String id;
    private String name; // ודאי שב-Firestore זה בדיוק name
    private List<String> songs; // ה-Log מראה שב-Firestore זה נקרא songs

    private String spotifyId;

    public Playlist() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public List<String> getSongs() { return songs; }
    public void setSongs(List<String> songs) { this.songs = songs; }

    public String getSpotifyId() {
        return spotifyId;
    }


}
