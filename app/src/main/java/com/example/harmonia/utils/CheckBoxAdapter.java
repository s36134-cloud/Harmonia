package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.R;
// ייבוא של המודל שלך (אם הוא בתיקייה הראשית com.example.harmonia)
import com.example.harmonia.CheckBox;

import java.util.List;

public class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.CheckBoxViewHolder> {

    // עכשיו משתמשים בשם הקלאס ישירות
    private List<CheckBox> items;

    public CheckBoxAdapter(List<CheckBox> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CheckBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // וודאי ששם ה-XML כאן (genre_item) תואם לשם ה-layout של הכרטיס שבנית
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox, parent, false);
        return new CheckBoxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckBoxViewHolder holder, int position) {
        CheckBox currentItem = items.get(position);

        // הגדרת הטקסט מתוך השדה genre במחלקה שלך
        holder.myCheckBox.setText(currentItem.getId());

        // מניעת באגים בזמן גלילה (Recycling)
        holder.myCheckBox.setOnCheckedChangeListener(null);
        holder.myCheckBox.setChecked(currentItem.isChecked());

        // עדכון המודל כשהמשתמש לוחץ
        holder.myCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentItem.setChecked(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class CheckBoxViewHolder extends RecyclerView.ViewHolder {
        // שימוש בווידג'ט המובנה של אנדרואיד
        android.widget.CheckBox myCheckBox;

        public CheckBoxViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור ל-ID של הצ'קבוקס ב-XML
            myCheckBox = itemView.findViewById(R.id.genreCheckBox);
        }
    }
}
