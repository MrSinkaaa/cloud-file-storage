package ru.mrsinkaaa.cloudfilestorage.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FolderDTO {

    private Long id;
    private String name;
    private String minioObjectId;
    private Long parentFolderId;

}
