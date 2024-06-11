package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.exception.FileNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.exception.FolderNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.FileRepository;
import ru.mrsinkaaa.cloudfilestorage.repository.FolderRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserService userService;
    private final MinioService minioService;


    @Transactional
    public File uploadFile(@AuthenticationPrincipal User user,
                           MultipartFile uploadFile,
                           String folderName) {
        log.info("Starting file upload for user: {}, folder: {}", user.getUsername(), folderName);
        var owner = userService.findByUsername(user.getUsername());
        var folder = folderRepository.findByFolderName(folderName)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

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
        return fileRepository.save(file);
    }

    public List<FileDTO> getFilesByFolder(Long id) {
        log.info("Fetching files for folder ID: {}", id);
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        return fileRepository.findByFolderId(folder).stream()
                .map(file -> FileDTO.builder()
                        .name(file.getFileName())
                        .id(file.getId())
                        .build()).toList();
    }

    public FileDTO findByFileName(String fileName) {
        log.info("Finding file by name: {}", fileName);
        File file = fileRepository.findByFileName(fileName)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));

        return FileDTO.builder()
                .id(file.getId())
                .name(file.getFileName())
                .minioObjectId(file.getMinioObjectId())
                .parentFolderId(file.getFolderId().getId())
                .build();
    }

    @Transactional
    public File renameFile(String oldFileName, String newFileName) {
        log.info("Renaming file from {} to {}", oldFileName, newFileName);
        File file = fileRepository.findByFileName(oldFileName)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));

        minioService.renameFile(oldFileName, newFileName);

        file.setMinioObjectId(newFileName);
        file.setFileName(newFileName);
        log.info("File {} renamed to {}", oldFileName, newFileName);
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public File deleteFile(@AuthenticationPrincipal User user, Long id) {
        log.info("Deleting file ID: {} for user: {}", id, user.getUsername());
        var owner = userService.findByUsername(user.getUsername());

        File file = fileRepository.findByOwnerIdAndId(owner.getId(), id)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));

        minioService.deleteFile(file.getMinioObjectId());
        fileRepository.deleteById(file.getId());
        log.info("File {} deleted by user {}", file.getFileName(), user.getUsername());
        return file;
    }

}
