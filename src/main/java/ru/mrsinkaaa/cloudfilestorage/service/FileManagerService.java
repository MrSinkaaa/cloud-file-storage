package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.*;
import java.util.stream.Collectors;

import static ru.mrsinkaaa.cloudfilestorage.util.BreadcrumbsUtils.getBreadcrumbLinks;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getParentFolderByPath;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.removeUserParentFolderPrefix;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagerService {

    private final FileService fileService;
    private final FolderService folderService;
    private final MinioService minioService;

    /**
     * Uploads a file to a specified folder for a user.
     *
     * @param owner the owner of the file
     * @param uploadFile the file to be uploaded
     * @param folderName the folder to upload the file to
     * @return the saved file entity
     */
    @Transactional
    public File uploadFile(User owner, MultipartFile uploadFile, String folderName) {
        log.info("Starting upload file {} for user: {}, folder: {}",
                uploadFile.getOriginalFilename(), owner.getUsername(), folderName);
        Folder folder = folderService.findByFolderName(folderName);

        String fileName = removeUserParentFolderPrefix(uploadFile.getOriginalFilename());
        String path = folder.getMinioObjectId() + fileName;

        File file = File.builder()
                .fileName(fileName)
                .fileSize(uploadFile.getSize())
                .fileType(uploadFile.getContentType())
                .folderId(folder)
                .owner(owner)
                .minioObjectId(path)
                .build();

        minioService.uploadFile(uploadFile, path);
        return fileService.save(file);
    }

    /**
     * Uploads multiple files as a folder structure.
     *
     * @param owner the owner of the files
     * @param files the files to be uploaded
     * @param parentFolderName the name of the parent folder
     * @return the parent folder entity
     */
    @Transactional
    public Folder uploadFolder(User owner, MultipartFile[] files, String parentFolderName) {
        log.info("Uploading folder for user: {}", owner.getUsername());
        Folder parentFolder = folderService.findByOwnerAndFolderName(owner, parentFolderName);
        log.info("Found parent folder: {}", parentFolder.getFolderName());

        Set<String> folderPaths = collectFolderPathsFromFiles(files);
        Map<String, Folder> createdFolders = createFolderStructure(owner, parentFolder, folderPaths);

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

    /**
     * Retrieves all files in a specified folder.
     *
     * @param id the folder ID
     * @return a list of file DTOs in the folder
     */
    public List<FileDTO> getFilesByFolder(Long id) {
        log.info("Fetching files for folder ID: {}", id);
        Folder folder = folderService.findByFolderId(id);

        return fileService.findByFolderId(folder);
    }

    /**
     * Renames a folder and updates the paths of all its subfolders and files.
     *
     * @param owner the owner of the folder
     * @param folderId the ID of the folder to be renamed
     * @param newFolderName the new name for the folder
     * @return the renamed folder entity
     */
    public Folder renameFolder(User owner, Long folderId, String newFolderName) {
        log.info("Renaming folder ID: {}, new name: {}", folderId, newFolderName);

        Folder folder = folderService.findByFolderId(folderId);

        Set<FileDTO> filesToRenamePath = new HashSet<>(getFilesByFolder(folderId));
        Set<FolderDTO> subFoldersToRenamePath = new HashSet<>(folderService.findSubFolders(folder.getId()));

        for(FolderDTO subFolder : folderService.findSubFolders(folder.getId())) {
            subFoldersToRenamePath.add(subFolder);
            filesToRenamePath.addAll(getFilesByFolder(subFolder.getId()));
        }
        Folder newFolder = folderService.renameFolder(owner, folderId, newFolderName);

        subFoldersToRenamePath.forEach(subFolder ->
                folderService.replaceFolder(owner, subFolder.getId(), newFolder));

        filesToRenamePath.forEach(file ->
                fileService.replaceFile(owner, file.getId(), newFolder));

        return folder;
    }

    /**
     * Deletes a folder and all its contents (subfolders and files).
     *
     * @param owner the owner of the folder
     * @param folderId the ID of the folder to be deleted
     * @return the deleted folder entity
     */
    @Transactional
    public Folder deleteFolder(User owner, Long folderId) {
        log.info("Deleting folder ID: {}", folderId);
        Folder folder = folderService.findByFolderId(folderId);

        Set<FileDTO> filesToDelete = new HashSet<>(getFilesByFolder(folderId));
        Set<FolderDTO> subFoldersToDelete = new HashSet<>(folderService.findSubFolders(folder.getId()));
        for(FolderDTO subFolder : folderService.findSubFolders(folder.getId())) {
            filesToDelete.addAll(getFilesByFolder(subFolder.getId()));
        }

        filesToDelete.forEach(file ->
                fileService.deleteFile(owner, file.getId()));

        subFoldersToDelete.forEach(subFolder ->
                folderService.deleteFolder(owner, subFolder.getId()));

        folderService.deleteFolder(owner, folder.getId());
        return folder;
    }

    /**
     * Collects folder paths from the given files.
     *
     * @param files the files to extract folder paths from
     * @return a set of folder paths
     */
    private Set<String> collectFolderPathsFromFiles(MultipartFile[] files) {
        Set<String> folderPaths = new HashSet<>();
        for (MultipartFile file : files) {
            List<String> foldersName = getBreadcrumbLinks(file.getOriginalFilename());
            folderPaths.addAll(foldersName);
        }
        log.info("Collected folder paths from files: {}", folderPaths);
        return folderPaths;
    }

    /**
     * Creates the folder structure based on the given folder paths.
     *
     * @param owner the owner of the folders
     * @param parentFolder the parent folder under which new folders will be created
     * @param folderPaths the paths of the folders to be created
     * @return a map of created folder paths to folder entities
     */
    private Map<String, Folder> createFolderStructure(User owner, Folder parentFolder, Set<String> folderPaths) {
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
        return createdFolders;
    }
}
