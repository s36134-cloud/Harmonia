package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.BookQuote;
import com.example.harmonia.R;
import com.example.harmonia.SongQuote;

import java.util.List;

public class SongQuoteAdapter extends RecyclerView.Adapter<SongQuoteAdapter.QuoteViewHolder> {

    private List<SongQuote> quoteList;

    // קונסטרקטור
    public SongQuoteAdapter(List<SongQuote> Songmood) {
        this.quoteList = Songmood;    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // קישור לקובץ ה-XML של השורה הבודדת (תוודאי שזה השם של ה-Layout שלך)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songquote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        if (quoteList == null || quoteList.isEmpty()) return;

        SongQuote quote = quoteList.get(position);

        // הצגת הטקסט של הציטוט
        holder.QuoteTextasong.setText(quote.getText());

        // הצגת שם הסופר ושם הספר
        holder.artist.setText(quote.getArtist());
        holder.sourceNamesong.setText(quote.getSourceName());


    }

    @Override
    public int getItemCount() {
        // תיקון למניעת קריסה: אם הרשימה null, נחזיר 0
        if (quoteList == null) {
            return 0;
        }
        return quoteList.size();
    }
    // ViewHolder פנימי
    public static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView QuoteTextasong, artist, sourceNamesong;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // כאן את עושה findViewById לכל ה-ID שיצרת בעיצוב (XML)
            QuoteTextasong = itemView.findViewById(R.id.QuoteTextasong);
            artist = itemView.findViewById(R.id.artist);
            sourceNamesong = itemView.findViewById(R.id.sourceNamesong);
        }
    }

    public void setList(List<SongQuote> newList) {
        this.quoteList = newList; // 'list' זה השם של הרשימה בתוך ה-Adapter שלך
        notifyDataSetChanged(); // פקודה שאומרת ל-RecyclerView להתרענן ולהציג את הנתונים החדשים
    }

}
