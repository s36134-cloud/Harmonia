package com.example.harmonia;
import com.google.firebase.firestore.PropertyName;

public class Conv {
    private String senderId;
    private String receiverId;
    private String message;
    private Object timestamp;
    private String type;
    private String songId;
    private String artist;
    private String genre;
    private String bookId;
    private String author;

    public String userProfilepic;



    public Conv() {}

    public Conv(String senderId, String receiverId, String message, Object timestamp, String type, String songId,String userProfilepic) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.songId = songId;
        this.userProfilepic = userProfilepic;

    }

    @PropertyName("text")
    public void setText(String text) { this.message = text; }

    @PropertyName("text")
    public String getText() { return message; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}