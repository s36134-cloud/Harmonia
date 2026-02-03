package com.example.harmonia;

public class Song {


    private String id;
    private String name;
    private String artist;
    private String genre;

    private boolean isSelectedsong = false;



    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String title) { this.name = name; }
    public String getArtist() { return artist; }
    public void setArtist(String author) { this.artist = author;}
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public boolean isSelectedsong() {
        return isSelectedsong;
    }


    public void setSelected(boolean selectedsong) {
        isSelectedsong = selectedsong;
    }
}
