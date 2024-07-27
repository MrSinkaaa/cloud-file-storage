package ru.mrsinkaaa.cloudfilestorage.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class FileDTO {

    private Long id;
    private String name;
    private String minioObjectId;
    private Long parentFolderId;
    private Long fileSize;
}
