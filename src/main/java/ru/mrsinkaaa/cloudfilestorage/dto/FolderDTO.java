package ru.mrsinkaaa.cloudfilestorage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FolderDTO {

    private Long id;
    private String name;
    private String minioObjectId;
    private Long parentFolderId;

}
