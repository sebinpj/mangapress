package com.celestial.mangapress.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MangaTile implements Parcelable {
    String title;
    String image;
    Bitmap imageBitmap;
    String url;
    List<ChapterLink> chapterLinks;
    String rating;

    public MangaTile(Parcel in) {
        title = in.readString();
        image = in.readString();
        imageBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        url = in.readString();
        rating = in.readString();
    }

    public static final Creator<MangaTile> CREATOR = new Creator<MangaTile>() {
        @Override
        public MangaTile createFromParcel(Parcel in) {
            return new MangaTile(in);
        }

        @Override
        public MangaTile[] newArray(int size) {
            return new MangaTile[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(image);
        parcel.writeParcelable(imageBitmap, i);
        parcel.writeString(url);
        parcel.writeString(rating);
    }
}
