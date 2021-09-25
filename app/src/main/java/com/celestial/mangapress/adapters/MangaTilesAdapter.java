package com.celestial.mangapress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.celestial.mangapress.R;
import com.celestial.mangapress.models.MangaTile;

import java.util.List;

public class MangaTilesAdapter extends RecyclerView.Adapter<MangaTilesAdapter.MyViewHolder> {

    private final Context mContext;
    private List<MangaTile> mangaTiles;
    private final OnClickListener onClickListener;

    public MangaTilesAdapter(Context mContext, List<MangaTile> mangaTiles, OnClickListener onClickListener) {
        this.mContext = mContext;
        this.mangaTiles = mangaTiles;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.manga_tile_home, parent, false);
        return new MyViewHolder(view, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MangaTile currentManga = mangaTiles.get(position);
        Glide.with(mContext)
                .asBitmap()
                .dontTransform()
                .load(currentManga.getImage())
                .into(holder.mangaImg);
        holder.mangaTitle.setText(currentManga.getTitle());
    }

    @Override
    public int getItemCount() {
        if (mangaTiles == null) {
            return 0;
        }
        return mangaTiles.size();
    }

    public void addMangaTiles(List<MangaTile> mangaTiles) {
        this.mangaTiles.addAll(mangaTiles);
        notifyDataSetChanged();
    }

    public void setMangaTiles(List<MangaTile> mangaTiles) {
        this.mangaTiles = mangaTiles;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mangaTitle;
        ImageView mangaImg;
        OnClickListener onClickListener;

        public MyViewHolder(@NonNull View itemView, OnClickListener onClickListener) {
            super(itemView);
            mangaImg = itemView.findViewById(R.id.manga_img);
            mangaTitle = itemView.findViewById(R.id.manga_title);
            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClickListener.onClick(mangaTiles.get(getAdapterPosition()));
        }
    }

    public interface OnClickListener {
        void onClick(MangaTile mangaTile);
    }
}
