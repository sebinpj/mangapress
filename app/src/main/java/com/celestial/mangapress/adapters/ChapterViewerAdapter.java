package com.celestial.mangapress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.celestial.mangapress.R;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ChapterViewerAdapter extends RecyclerView.Adapter<ChapterViewerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<String> images;
    Picasso picasso;


    public ChapterViewerAdapter(Context mContext, List<String> images) {
        this.mContext = mContext;
        this.images = images;
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("referer", "https://manganelo.com/")
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();
        picasso = new Picasso.Builder(mContext)
                .downloader(new OkHttp3Downloader(client))
                .build();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.chapter_image, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String image = images.get(position);
        picasso
                .load(image)
                .placeholder(R.drawable.default_image_thumbnail)
                .error(R.drawable.failed_to_load)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addImages(List<String> images){
        this.images.addAll(images);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.chapter_image);
        }
    }
}
