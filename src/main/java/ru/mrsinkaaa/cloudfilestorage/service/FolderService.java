package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.FolderNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.FolderRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final MinioService minioService;


    @Transactional
    public Folder uploadFolder(User owner, String folderName, String parentFolderName) {

        return null;
    }

    public FolderDTO findByFolderName(User owner, String folderName) {
        log.info("Finding folder by name: {} for user: {}", folderName, owner.getUsername());
        Folder folder = folderRepository.findByOwnerAndFolderName(owner, folderName)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        return FolderDTO.builder()
                .id(folder.getId())
                .name(folder.getFolderName())
                .minioObjectId(folder.getMinioObjectId())
                .parentFolderId(folder.getParentFolder() == null ? folder.getId() : folder.getParentFolder().getId())
                .build();
    }

    public List<FolderDTO> findSubFolders(Long id) {
        log.info("Finding sub folders by parent folder id: {}", id);
        Folder parent = folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        return folderRepository.findByParentFolder(parent).stream()
                .map(folder -> FolderDTO.builder()
                        .id(folder.getId())
                        .name(folder.getFolderName())
                        .minioObjectId(folder.getMinioObjectId())
                        .parentFolderId(folder.getParentFolder().getId())
                        .build())
                .toList();
    }

    @Transactional
    public void createFolder(User owner, String folderName, String parentFolderName) {
        log.info("Creating folder: {} for user: {}", folderName, owner.getUsername());

        Folder folder = new Folder();
        folder.setFolderName(folderName);
        folder.setOwner(owner);

        //Create root folder
        if (parentFolderName == null) {
            folder.setMinioObjectId(folderName);
            folder.setParentFolder(null);
            log.info("Creating root folder: {}", folderName);
            // Create sub folder
        } else {
            Folder parentFolder = folderRepository.findByOwnerAndFolderName(owner, parentFolderName)
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));

            folder.setMinioObjectId(parentFolder.getMinioObjectId() + folderName);
            folder.setParentFolder(parentFolder);
        }

        minioService.createFolder(folder.getMinioObjectId());
        log.info("Creating subfolder: {} under parent folder: {}", folderName, parentFolderName);
        folderRepository.save(folder);
    }
}
