package com.celestial.mangapress.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.celestial.mangapress.entities.MangaTileEntity;

import java.util.List;

@Dao
public interface MangaTileEntityDao {

    @Query("SELECT * FROM mangatileentity")
    List<MangaTileEntity> getAll();

    @Query("SELECT * FROM mangatileentity where url = :url limit 1 ")
    MangaTileEntity getByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MangaTileEntity... chapterEntities);

    @Delete
    void delete(MangaTileEntity chapterEntity);
}
