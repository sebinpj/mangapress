package com.celestial.mangapress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.celestial.mangapress.R;
import com.celestial.mangapress.models.ChapterLink;

import java.util.List;

public class ChapterLinksAdapter extends RecyclerView.Adapter<ChapterLinksAdapter.MyViewHolder> {
    private final Context mContext;
    private List<ChapterLink> chapterLinks;
    private final ChapterClickListener chapterClickListener;

    public ChapterLinksAdapter(Context mContext, List<ChapterLink> chapterLinks, ChapterClickListener chapterClickListener) {
        this.mContext = mContext;
        this.chapterLinks = chapterLinks;
        this.chapterClickListener = chapterClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.manga_details_chapter_cards, parent, false);
        return new MyViewHolder(view, chapterClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChapterLink chapterLink = chapterLinks.get(position);
        holder.chapterLink.setText(chapterLink.getChapterTitle());
        if (chapterLink.isViewed()) {
            holder.viewed.setVisibility(View.VISIBLE);
        } else {
            holder.viewed.setVisibility(View.GONE);
        }
    }

    public void updateChapterLinks(List<ChapterLink> chapterLinks) {
        this.chapterLinks = chapterLinks;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return chapterLinks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView chapterLink;
        ImageView viewed;
        ChapterClickListener chapterClickListener;

        public MyViewHolder(@NonNull View itemView, ChapterClickListener chapterClickListener) {
            super(itemView);
            chapterLink = itemView.findViewById(R.id.chapter_link);
            viewed = itemView.findViewById(R.id.viewed_icon);
            this.chapterClickListener = chapterClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            chapterClickListener.onClick(chapterLinks.get(getAdapterPosition()));
        }
    }

    public interface ChapterClickListener {
        void onClick(ChapterLink chapterLink);
    }
}
