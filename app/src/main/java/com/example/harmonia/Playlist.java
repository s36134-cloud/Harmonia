package com.example.harmonia;

import java.util.List;

public class Playlist {

        private String name;
        private List<String> songIds;

        public Playlist() {} // חובה ל-Firestore

        public String getName() { return name; }
        public List<String> getSongIds() { return songIds; }

}
