package com.example.harmonia;

import com.google.gson.annotations.SerializedName;

public class RecommendedUser {

    @SerializedName("ID") // מתאים למה שג'מיני שולח בלוגים
    public String user_id;

    // הוספת האנוטציה הזו מוודא שאם ג'מיני שולח "name", זה ייכנס ל-display_name
    @SerializedName(value = "display_name", alternate = {"name", "userName", "display_name"})
    public String display_name;

    public int score;
    public String reason;

    public RecommendedUser() {}

    public RecommendedUser(String user_id, String display_name, int score, String reason) {
        this.user_id = user_id;
        this.display_name = display_name;
        this.score = score;
        this.reason = reason;
    }
}