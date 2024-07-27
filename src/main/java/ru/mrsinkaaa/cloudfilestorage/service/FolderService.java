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
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFolderService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService implements IFolderService {

    private final FolderRepository folderRepository;
    private final MinioService minioService;

    @Override
    public Folder findByFolderId(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));
    }

    @Override
    public Folder findByFolderName(String folderName) {
        log.info("Finding folder by name: {}", folderName);
        return folderRepository.findByFolderName(folderName)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));
    }
    @Override
    public Folder findByOwnerAndFolderName(User owner, String folderName) {
        log.info("Finding folder with name: {} for user: {}", folderName, owner.getUsername());
        return folderRepository.findByOwnerAndFolderName(owner, folderName)
                .orElseThrow(() -> {
                    log.error("Folder with name: {} not found for user: {}", folderName, owner.getUsername());
                    return new FolderNotFoundException("Folder not found");
                });
    }

    @Override
    public Folder findByOwnerAndFolderId(User owner, Long folderId) {
        log.info("Finding folder by ID: {} for user: {}", folderId, owner.getUsername());

        return folderRepository.findByIdAndOwner(folderId, owner)
               .orElseThrow(() -> new FolderNotFoundException("Folder not found"));
    }

    @Override
    public FolderDTO findByOwnerAndMinioObjectId(User owner, String minioObjectId) {
        log.info("Finding folder by Minio Object ID: {} for user: {}", minioObjectId, owner.getUsername());

        Folder folder = folderRepository.findByOwnerAndMinioObjectId(owner, minioObjectId)
               .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        return FolderDTO.builder()
               .id(folder.getId())
               .name(folder.getFolderName())
               .minioObjectId(folder.getMinioObjectId())
               .parentFolderId(folder.getParentFolder() == null ? folder.getId() : folder.getParentFolder().getId())
               .build();
    }

    @Override
    public FolderDTO findByFolderName(User owner, String folderName) {
        log.info("Finding folder by name: {} for user: {}", folderName, owner.getUsername());
        Folder folder = findByOwnerAndFolderName(owner, folderName);

        return FolderDTO.builder()
                .id(folder.getId())
                .name(folder.getFolderName())
                .minioObjectId(folder.getMinioObjectId())
                .parentFolderId(folder.getParentFolder() == null ? folder.getId() : folder.getParentFolder().getId())
                .build();
    }

    @Override
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

    @Override
    public List<FolderDTO> findFoldersByOwnerAndFolderNameContainingIgnoreCase(User owner, String folderName) {
        log.info("Finding folders by name: {} for user: {}", folderName, owner.getUsername());
        return folderRepository.findFoldersByOwnerAndFolderNameContainingIgnoreCase(owner, folderName).stream()
               .map(folder -> FolderDTO.builder()
                       .id(folder.getId())
                       .name(folder.getFolderName())
                       .minioObjectId(folder.getMinioObjectId())
                       .parentFolderId(folder.getParentFolder() == null? folder.getId() : folder.getParentFolder().getId())
                       .build())
               .toList();
    }

    @Override
    @Transactional
    public Folder renameFolder(User owner, Long folderId, String newFolderName) {
        Folder folder = findByOwnerAndFolderId(owner, folderId);
        String oldFolderName = folder.getFolderName();

        log.info("Renaming folder from {} to {}", oldFolderName, newFolderName);

        String newMinioObjectId = folder.getParentFolder().getMinioObjectId() + newFolderName;
        minioService.renameFile(folder.getMinioObjectId(), newMinioObjectId);

        folder.setMinioObjectId(newMinioObjectId);
        folder.setFolderName(newFolderName);
        folderRepository.save(folder);
        return folder;
    }

    @Override
    public Folder replaceFolder(User owner, Long id, Folder newParentFolder) {
        Folder folder = findByOwnerAndFolderId(owner, id);
        String oldMinioObjectId = folder.getMinioObjectId();

        log.info("Replacing folder: {} in folder: {} with new folder: {}",
                folder.getFolderName(),
                folder.getParentFolder().getMinioObjectId(),
                newParentFolder.getMinioObjectId());

        String newMinioObjectId = newParentFolder.getMinioObjectId() + folder.getFolderName();
        minioService.renameFile(oldMinioObjectId, newMinioObjectId);

        folder.setMinioObjectId(newMinioObjectId);
        folder.setParentFolder(newParentFolder);
        folderRepository.save(folder);
        return folder;
    }

    @Override
    public void deleteFolder(User owner, Long folderId) {
        Folder folder = findByOwnerAndFolderId(owner, folderId);
        log.info("Deleting folder: {} for user: {}", folder.getFolderName(), owner.getUsername());

        folderRepository.delete(folder);
        minioService.deleteFile(folder.getMinioObjectId());
        log.info("Folder deleted: {}", folder.getFolderName());
    }

    @Override
    @Transactional
    public Folder createFolder(User owner, String folderName, String parentFolderName) {
        log.info("Creating folder: {} for user: {}", folderName, owner.getUsername());

        if(!folderName.contains("/")) {
            folderName += "/";
        }

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
            Folder parentFolder = findByOwnerAndFolderName(owner, parentFolderName);

            folder.setMinioObjectId(parentFolder.getMinioObjectId() + folderName);
            log.info("Creating subfolder: {} under parent folder: {}", folderName, parentFolderName);
            folder.setParentFolder(parentFolder);
        }

        minioService.createFolder(folder.getMinioObjectId());
        folderRepository.save(folder);
        return folder;
    }

}
