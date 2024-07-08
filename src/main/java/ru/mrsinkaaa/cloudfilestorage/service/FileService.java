package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.FileNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.FileRepository;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFileService;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFolderService;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileService implements IFileService {

    private final FileRepository fileRepository;
    private final MinioService minioService;

    public File findFileByFileName(String fileName) {
        return fileRepository.findByFileName(fileName)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));
    }

    public FileDTO findByFileName(String fileName) {
        log.info("Finding file by name: {}", fileName);
        File file = findFileByFileName(fileName);

        return FileDTO.builder()
                .id(file.getId())
                .name(file.getFileName())
                .minioObjectId(file.getMinioObjectId())
                .parentFolderId(file.getFolderId().getId())
                .build();
    }

    public List<FileDTO> findByFolderId(Folder folderId) {
        return fileRepository.findByFolderId(folderId).stream()
                .map(file -> FileDTO.builder()
                        .name(file.getFileName())
                        .id(file.getId())
                        .build()).toList();
    }

    public File save(File file) {
        log.info("Saving file: {}", file.getFileName());
        return fileRepository.save(file);
    }

    @Transactional
    public File renameFile(String oldFileName, String newFileName) {
        log.info("Renaming file from {} to {}", oldFileName, newFileName);
        File file = findFileByFileName(oldFileName);

        minioService.renameFile(oldFileName, newFileName);

        file.setMinioObjectId(newFileName);
        file.setFileName(newFileName);
        log.info("File {} renamed to {}", oldFileName, newFileName);
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public File deleteFile(User owner, Long id) {
        log.info("Deleting file ID: {} for user: {}", id, owner.getUsername());

        File file = fileRepository.findByOwnerIdAndId(owner.getId(), id)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));

        minioService.deleteFile(file.getMinioObjectId());
        fileRepository.deleteById(file.getId());
        log.info("File {} deleted by user {}", file.getFileName(), owner.getUsername());
        return file;
    }

}
