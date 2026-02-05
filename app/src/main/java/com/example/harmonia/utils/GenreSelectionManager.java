package com.example.harmonia.utils;

import java.util.ArrayList;
import java.util.List;

public class GenreSelectionManager {

    private List<String> selectedGenres;

    public GenreSelectionManager() {
        this.selectedGenres = new ArrayList<>();
    }

    // הוספת ז'אנר לרשימה (כשמסמנים V)
    public void addGenre(String genre) {
        if (!selectedGenres.contains(genre)) {
            selectedGenres.add(genre);
        }
    }

    // הסרת ז'אנר מהרשימה (כשמורידים V)
    public void removeGenre(String genre) {
        selectedGenres.remove(genre);
    }

    // קבלת הרשימה המלאה עבור השאילתה לפיירסטור
    public List<String> getSelectedGenres() {
        return selectedGenres;
    }
}

