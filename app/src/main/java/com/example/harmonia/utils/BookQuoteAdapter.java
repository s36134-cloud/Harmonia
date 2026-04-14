package com.example.harmonia.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.BookQuote;
import com.example.harmonia.R;

import java.util.List;

public class BookQuoteAdapter extends RecyclerView.Adapter<BookQuoteAdapter.QuoteViewHolder> {

    private List<BookQuote> quoteList;

    // קונסטרקטור מתוקן
    public BookQuoteAdapter(List<BookQuote> mood) {
        // כאן התיקון: אנחנו לוקחים את הנתונים מהפרמטר 'mood' ושומרים ב-'quoteList'
        this.quoteList = mood;
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookquote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        // הגנה קטנה למקרה שהרשימה ריקה
        if (quoteList == null || quoteList.isEmpty()) return;

        BookQuote quote = quoteList.get(position);

        holder.tvText.setText(quote.gettext());
        holder.tvAuthor.setText(quote.getauthor());
        holder.tvBookName.setText(quote.getsourceName());
    }

    @Override
    public int getItemCount() {
        // תיקון למניעת קריסה: אם הרשימה null, נחזיר 0
        if (quoteList == null) {
            return 0;
        }
        return quoteList.size();
    }

    public static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvAuthor, tvBookName;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.QuoteTextbook);
            tvAuthor = itemView.findViewById(R.id.author);
            tvBookName = itemView.findViewById(R.id.sourceNamebook);
        }
    }
}