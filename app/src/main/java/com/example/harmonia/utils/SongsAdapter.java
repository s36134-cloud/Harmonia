package com.example.harmonia.utils;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harmonia.Song;
import com.example.harmonia.R;


import  android.view.View;
import android.widget.Toast;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    // הרשימה שתכיל את הספרים
    private List<Song> songList;

    //   ככה  מקבלים את הרשימה מה-Activity
    public SongsAdapter(List<Song> songList) {
        this.songList = songList;
    }

    // ה-ViewHolder: כאן  תופסים את הרכיבים מה-XML
    public static class SongViewHolder extends RecyclerView.ViewHolder {


        public TextView namesong;
        public TextView artist;
        public ImageView songimage;
        public TextView genresong;



        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            namesong = itemView.findViewById(R.id.namebook);
            artist = itemView.findViewById(R.id.author);
            songimage = itemView.findViewById(R.id.bookimage);
            genresong = itemView.findViewById(R.id.genrebook);

        }
    }
    // 1. קובע איזה XML משמש לכל שורה
    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book, parent, false);
        return new SongViewHolder(view);
    }

    // 2. מחבר את הנתונים מהספר הספציפי ל-ViewHolder
    // 2. מחבר את הנתונים מהספר הספציפי ל-ViewHolder
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        // 1. מילוי הטקסטים (שם הספר והסופר)
        holder.namesong.setText(song.getName());
        holder.artist.setText(song.getArtist());

        // 2. מילוי שאר השדות (ז'אנר וגיל)
        holder.genresong.setText(song.getGenre());


        // 3. בניית הכתובת המדויקת לסופאבייס
        String imageUrl = "https://nbliklmpfsjemwizicuh.supabase.co/storage/v1/object/public/Harmonia-bucket/" + song.getId() + ".jpg";

        // 4. טעינת התמונה
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(imageUrl)

                .into(holder.songimage);

        // 1. פידבק ויזואלי: אם השיר נבחר - נשנה לו את השקיפות או נוסיף רקע
        if (song.isSelectedsong()) {
            holder.itemView.setAlpha(0.5f); // הופך את הכרטיסייה לקצת שקופה
            holder.itemView.setBackgroundResource(android.R.color.holo_blue_light); // סתם דוגמה לרקע כחול
        } else {
            holder.itemView.setAlpha(1.0f); // מצב רגיל
            holder.itemView.setBackgroundResource(android.R.color.white);
        }

        // 2. טיפול בלחיצה
        holder.itemView.setOnClickListener(v -> {
            if (song.isSelectedsong()) {
                // אם הוא כבר נבחר - נבטל את הבחירה
                song.setSelected(false);
            } else {
                // אם הוא לא נבחר - נבדוק אם כבר הגענו ל-4
                int count = 0;
                for (Song s : songList) {
                    if (s.isSelectedsong()) count++;
                }

                if (count < 4) {
                    song.setSelected(true);
                } else {
                    Toast.makeText(v.getContext(), "אפשר לבחור עד 4 שירים בלבד", Toast.LENGTH_SHORT).show();
                }
            }

            // חשוב מאוד: מעדכן את הרשימה כדי שהעיצוב ישתנה מיד
            notifyItemChanged(position);

            boolean hasSelection = false;
            for (Song s : songList) {
                if (s.isSelectedsong()) {
                    hasSelection = true;
                    break;
                }
            }

            View button = v.getRootView().findViewById(R.id.btnDoneSongs);
            if (button != null) {
                button.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
            }
        });


    }


    // אומרת לאדאפטר כמה ספרים יש ברשימה
    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }
}


