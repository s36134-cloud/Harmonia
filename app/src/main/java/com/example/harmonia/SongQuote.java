package com.example.harmonia;

import java.util.List;

public class SongQuote {
    private String text;
    private String genre;
    private String sourceType;
    private String sourceName;
    private String artist;

    private List<String> songmood;

    public SongQuote()
    {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public List<String> getSongMood() { return songmood; }
    public void setSongMood(List<String> songmood) { this.songmood = songmood; }
}
