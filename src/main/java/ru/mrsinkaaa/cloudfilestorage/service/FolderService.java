package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.FolderNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.FolderRepository;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFileService;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFolderService;

import java.util.*;

import static ru.mrsinkaaa.cloudfilestorage.util.BreadcrumbsUtils.getBreadcrumbLinks;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getParentFolderByPath;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService implements IFolderService {

    private final FolderRepository folderRepository;
    private final MinioService minioService;

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
    public void deleteFolder(User owner, Folder folder) {
        log.info("Deleting folder: {} for user: {}", folder.getFolderName(), owner.getUsername());

        folderRepository.delete(folder);
        minioService.deleteFile(folder.getMinioObjectId());
        log.info("Folder deleted: {}", folder.getFolderName());
    }

    @Override
    @Transactional
    public Folder createFolder(User owner, String folderName, String parentFolderName) {
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
