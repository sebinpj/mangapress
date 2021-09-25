package com.celestial.mangapress;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.celestial.mangapress.dao.ChapterEntityDao;
import com.celestial.mangapress.dao.MangaTileEntityDao;
import com.celestial.mangapress.entities.ChapterEntity;
import com.celestial.mangapress.entities.MangaTileEntity;

@Database(entities = {ChapterEntity.class, MangaTileEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ChapterEntityDao chapterEntityDao();

    public abstract MangaTileEntityDao mangaTileEntityDao();

}