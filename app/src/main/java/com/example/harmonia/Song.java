package com.example.harmonia;

public class Song {


    private String id;
    private String name;
    private String artist;
    private String genre;

    private boolean isSelectedsong = false;

    public Song() {
        // חובה להשאיר ריק עבור Firestore
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist;}
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public boolean isSelectedsong() {
        return isSelectedsong;
    }


    public void setSelected(boolean selectedsong) {
        isSelectedsong = selectedsong;
    }
}
