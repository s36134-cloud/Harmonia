package com.example.harmonia;
public class CheckBox {
    private String id;        // כאן יישמר שם הז'אנר (ה-ID מה-Firestore)
    private boolean isChecked;

    public CheckBox() {} // חובה

    public CheckBox(String id) {
        this.id = id;
        this.isChecked = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    // נוסיף מתודה קטנה שתחזיר את השם של הז'אנר (בשביל האדאפטר)
    public String getGenreName() { return id; }
}