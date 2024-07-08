package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.*;

import static ru.mrsinkaaa.cloudfilestorage.util.BreadcrumbsUtils.getBreadcrumbLinks;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getParentFolderByPath;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagerService {

    private final FileService fileService;
    private final FolderService folderService;
    private final MinioService minioService;

    @Transactional
    public File uploadFile(User owner,
                           MultipartFile uploadFile,
                           String folderName) {
        log.info("Starting file upload for user: {}, folder: {}", owner.getUsername(), folderName);
        var folder = folderService.findByFolderName(folderName);

        String path = folder.getMinioObjectId() + uploadFile.getOriginalFilename();

        File file = File.builder()
                .fileName(uploadFile.getOriginalFilename())
                .fileSize(uploadFile.getSize())
                .fileType(uploadFile.getContentType())
                .folderId(folder)
                .owner(owner)
                .minioObjectId(path)
                .build();

        minioService.uploadFile(uploadFile, path);
        log.info("File {} uploaded to folder {}", uploadFile.getOriginalFilename(), folderName);
        return fileService.save(file);
    }

    @Transactional
    public Folder uploadFolder(User owner, MultipartFile[] files, String parentFolderName) {
        log.info("Uploading folder for user: {}", owner.getUsername());
        Folder parentFolder = folderService.findByOwnerAndFolderName(owner, parentFolderName);
        log.info("Found parent folder: {}", parentFolder.getFolderName());

        Set<String> folderPaths = new HashSet<>();
        for (MultipartFile file : files) {
            List<String> foldersName = getBreadcrumbLinks(file.getOriginalFilename());
            folderPaths.addAll(foldersName);
        }
        log.info("Collected folder paths from files: {}", folderPaths);

        Map<String, Folder> createdFolders = new HashMap<>();
        for (String path : folderPaths) {
            String[] parts = path.split("/");

            Folder currentFolder = parentFolder;
            for (String part : parts) {
                String currentFolderPath = part + "/";
                if (!createdFolders.containsKey(currentFolderPath)) {
                    Folder created = folderService.createFolder(owner, currentFolderPath, currentFolder.getFolderName());
                    createdFolders.put(currentFolderPath, created);
                    currentFolder = created;
                } else {
                    currentFolder = createdFolders.get(currentFolderPath);
                    log.info("Folder already exists: {}", currentFolderPath);
                }
            }
        }

        for (MultipartFile file : files) {
            String filePath = file.getOriginalFilename();
            String folderPath = getParentFolderByPath(filePath);
            Folder parent = createdFolders.get(folderPath + "/");

            if (parent != null) {
                uploadFile(owner, file, parent.getFolderName());
            }
        }
        return parentFolder;
    }

    public List<FileDTO> getFilesByFolder(Long id) {
        log.info("Fetching files for folder ID: {}", id);
        Folder folder = folderService.findByFolderId(id);

        return fileService.findByFolderId(folder);
    }
}
