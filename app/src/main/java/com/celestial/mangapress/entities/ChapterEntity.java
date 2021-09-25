package com.celestial.mangapress.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ChapterEntity {

    @PrimaryKey
    @NonNull
    public String chapterUrl;

    @ColumnInfo(name = "manga_url")
    public String mangaUrl;
}