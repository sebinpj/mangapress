package com.celestial.mangapress.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class MangaDetails extends MangaTile {
    String description;
}
