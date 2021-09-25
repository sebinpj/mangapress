package com.celestial.mangapress.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.celestial.mangapress.entities.ChapterEntity;

import java.util.List;

@Dao
public interface ChapterEntityDao {

    @Query("SELECT * FROM chapterentity WHERE manga_url = :mangaUrl")
    List<ChapterEntity> getByMangaUrl(String mangaUrl);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ChapterEntity... chapterEntities);

    @Delete
    void delete(ChapterEntity chapterEntity);
}