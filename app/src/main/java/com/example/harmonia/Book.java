package com.example.harmonia;

import android.widget.ImageView;

public class Book {
    private String id;
    private String name;
    private String author;
    private String genre;
    private int minage;




    public Book() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return name; }
    public void setTitle(String title) { this.name = name; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author;}
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getMinage() {
        return minage;
    }

    public void setMinage(int minage) {
        this.minage = minage;
    }

}