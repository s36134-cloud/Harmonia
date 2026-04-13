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

    public String gettext() { return genre; }
    public void settext(String genre) { this.text = text; }

    public String getgenre() { return genre; }
    public void setgenre(String genre) { this.genre = genre; }

    public String getsourceType() { return sourceType; }
    public void setsourceType(String sourceType) { this.sourceType = sourceType; }

    public String getsourceName() { return sourceName; }
    public void setsourceName(String sourceName) { this.sourceName = sourceName; }

    public String getartist() { return artist; }
    public void setartist(String artist) { this.artist = artist; }

    public List<String> getsongmood() { return songmood; }
    public void setsongmood(List<String> songmood) { this.songmood = songmood; }
}
