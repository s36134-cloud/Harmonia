package com.example.harmonia;

import java.util.List;

public class BookQuote {

    private String text;
    private String sourceName;
    private String author;

    private List<String> Bookmood;

    public BookQuote()
    {}

    public String gettext() { return text; }
    public void settext(String text) { this.text = text; }





    public String getsourceName() { return sourceName; }
    public void setsourceName(String sourceName) { this.sourceName = sourceName; }

    public String getauthor() { return author; }
    public void setauthor(String author) { this.author = author; }

    public List<String> getBookmood() { return Bookmood; }
    public void setBookmood(List<String> Bookmood) { this.Bookmood = Bookmood; }

}
