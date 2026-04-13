package com.example.harmonia;

import java.util.List;

public class BookQuote {

    private String text;
    private String genre;
    private String sourceType;
    private String sourceName;
    private String author;

    private List<String> Bookmood;

    public BookQuote()
    {}

    public String gettext() { return genre; }
    public void settext(String genre) { this.text = text; }

    public String getgenre() { return genre; }
    public void setgenre(String genre) { this.genre = genre; }

    public String getsourceType() { return sourceType; }
    public void setsourceType(String sourceType) { this.sourceType = sourceType; }

    public String getsourceName() { return sourceName; }
    public void setsourceName(String sourceName) { this.sourceName = sourceName; }

    public String getauthor() { return author; }
    public void setauthor(String author) { this.author = author; }

    public List<String> getBookmood() { return Bookmood; }
    public void setBookmood(List<String> Bookmood) { this.Bookmood = Bookmood; }

}
