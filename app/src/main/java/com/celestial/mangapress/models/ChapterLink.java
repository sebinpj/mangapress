package com.celestial.mangapress.models;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class ChapterLink implements Parcelable {
    @NonNull
    String url;
    @NonNull
    String chapterTitle;

    boolean viewed;

    protected ChapterLink(Parcel in) {
        url = in.readString();
        chapterTitle = in.readString();
    }

    public static final Creator<ChapterLink> CREATOR = new Creator<ChapterLink>() {
        @Override
        public ChapterLink createFromParcel(Parcel in) {
            return new ChapterLink(in);
        }

        @Override
        public ChapterLink[] newArray(int size) {
            return new ChapterLink[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(chapterTitle);
    }
}
