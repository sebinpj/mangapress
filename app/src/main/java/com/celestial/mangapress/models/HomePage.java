package com.celestial.mangapress.models;

import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class HomePage {
    String title;
    @NonNull
    List<MangaTile> savedMangas;
    @NonNull
    List<MangaTile> latestReleases;
}
