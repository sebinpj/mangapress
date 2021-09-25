package com.celestial.mangapress;

import android.content.Context;

import androidx.room.Room;

import com.celestial.mangapress.dao.ChapterEntityDao;
import com.celestial.mangapress.dao.MangaTileEntityDao;

public class Database {
    private static Database INSTANCE;
    private final AppDatabase appDatabase;

    private Database(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    public static synchronized Database initialize(Context application) {
        if (INSTANCE == null) {
            INSTANCE = new Database(Room.databaseBuilder(application,
                    AppDatabase.class, "celestial_mangas").build());
        }
        return INSTANCE;
    }

    public static ChapterEntityDao getChapterEntityDao() {
        return INSTANCE.appDatabase.chapterEntityDao();
    }

    public static MangaTileEntityDao getMangaTileEntityDao() {
        return INSTANCE.appDatabase.mangaTileEntityDao();
    }
}
