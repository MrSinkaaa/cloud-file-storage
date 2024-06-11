package ru.mrsinkaaa.cloudfilestorage.dto;

import lombok.Getter;

@Getter
public enum FileType {

    FILE("file.svg"),
    FOLDER("folder.svg"),
    MUSIC("music.svg"),
    PDF("pdf.svg"),
    IMAGE("image.svg"),
    VIDEO("video.svg");


    private final String icon;

    FileType(String icon) {
        this.icon = icon;
    }
}
