package com.example.harmonia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.ViewHolder> {

    // שימוש במחלקה שיצרת
    private List<ItemModel> itemList;

    public HorizontalAdapter(List<ItemModel> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // כאן אנחנו מחברים את ה-XML עם הפינות המעוגלות
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_books, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // שליפת הנתונים מהמודל שלך
        ItemModel item = itemList.get(position);

        // כאן את מחליטה מה להציג (למשל כותרת)
        holder.textView.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // ודאי שב-item_horizontal.xml נתת ID לטקסט
            textView = itemView.findViewById(R.id.textViewTitle);
        }
    }
}
