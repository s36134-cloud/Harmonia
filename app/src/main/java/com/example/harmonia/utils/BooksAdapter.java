package com.example.harmonia.utils;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.Book;
import com.example.harmonia.R;
import  android.view.View;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

        // הרשימה שתכיל את הספרים
        private List<Book> bookList;

        //   ככה  מקבלים את הרשימה מה-Activity
        public BooksAdapter(List<Book> bookList) {
            this.bookList = bookList;
        }

        // ה-ViewHolder: כאן  תופסים את הרכיבים מה-XML
        public static class BookViewHolder extends RecyclerView.ViewHolder {


            public TextView namebook;
            public TextView author;
            public ImageView bookimage;
            public TextView genrebook;
            public TextView minage;


            public BookViewHolder(@NonNull View itemView) {
                super(itemView);

                namebook = itemView.findViewById(R.id.namebook);
                author = itemView.findViewById(R.id.author);
                bookimage = itemView.findViewById(R.id.bookimage);
                genrebook = itemView.findViewById(R.id.genrebook);
                minage = itemView.findViewById(R.id.minage);
            }
        }
    // 1. קובע איזה XML משמש לכל שורה
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book, parent, false);
        return new BookViewHolder(view);
    }

    // 2. מחבר את הנתונים מהספר הספציפי ל-ViewHolder
    // 2. מחבר את הנתונים מהספר הספציפי ל-ViewHolder
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // 1. מילוי הטקסטים (שם הספר והסופר)
        holder.namebook.setText(book.getName());
        holder.author.setText(book.getAuthor());

        // 2. מילוי שאר השדות (ז'אנר וגיל)
        holder.genrebook.setText(book.getGenre());
        holder.minage.setText(String.valueOf(book.getMinage()) + "+");

        // 3. בניית הכתובת המדויקת לסופאבייס
        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/" + book.getId() + ".jpg";

        // 4. טעינת התמונה
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(imageUrl)

                .into(holder.bookimage);
    }


// אומרת לאדאפטר כמה ספרים יש ברשימה
    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }
    }


