package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.RamUsageDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.FileNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.FileRepository;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFileService;

import java.io.InputStream;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileService implements IFileService {

    private final FileRepository fileRepository;
    private final MinioService minioService;

    public File findByOwnerAndFileName(User owner, String fileName) {
        return fileRepository.findByOwnerAndFileName(owner, fileName)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));
    }

    public File findFileByOwnerIdAndId(Long ownerId, Long id) {
        return fileRepository.findByOwnerIdAndId(ownerId, id)
                .orElseThrow(() -> new FileNotFoundException("File does not exist"));
    }

    public List<FileDTO> findByFolderId(Folder folderId) {
        return fileRepository.findByFolderId(folderId).stream()
                .map(file -> FileDTO.builder()
                        .name(file.getFileName())
                        .id(file.getId())
                        .minioObjectId(file.getMinioObjectId())
                        .parentFolderId(file.getFolderId().getId())
                        .build()).toList();
    }

    @Transactional(readOnly = true)
    public InputStream downloadFile(User owner, Long id) {
        log.info("Downloading file ID: {} for user: {}", id, owner.getUsername());

        File file = findFileByOwnerIdAndId(owner.getId(), id);
        return minioService.downloadFile(file.getMinioObjectId());
    }

    public File save(File file) {
        return fileRepository.save(file);
    }

    @Transactional
    public File renameFile(User owner, Long id, String newFileName) {
        File file = findFileByOwnerIdAndId(owner.getId(), id);
        String oldFileName = file.getFileName();
        log.info("Renaming file from {} to {}", oldFileName, newFileName);

        String newMinioObjectId = file.getFolderId().getMinioObjectId() + newFileName;
        minioService.renameFile(file.getMinioObjectId(),newMinioObjectId);

        file.setMinioObjectId(newMinioObjectId);
        file.setFileName(newFileName);
        log.info("File {} renamed to {}", oldFileName, newFileName);
        fileRepository.save(file);
        return file;
    }

    public File replaceFile(User owner, Long id, Folder newParentFolder) {
        File file = findFileByOwnerIdAndId(owner.getId(), id);
        String oldMinioObjectId = file.getMinioObjectId();

        log.info("Replacing file: {} in folder: {} with new folder: {}", id, file.getFolderId().getId(), newParentFolder.getId());

        String newMinioObjectId = newParentFolder.getMinioObjectId() + file.getFileName();
        minioService.renameFile(oldMinioObjectId, newMinioObjectId);

        file.setMinioObjectId(newMinioObjectId);
        file.setFolderId(newParentFolder);
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public File deleteFile(User owner, Long id) {
        log.info("Deleting file ID: {} for user: {}", id, owner.getUsername());

        File file = findFileByOwnerIdAndId(owner.getId(), id);

        minioService.deleteFile(file.getMinioObjectId());
        fileRepository.deleteById(file.getId());
        log.info("File {} deleted by user {}", file.getFileName(), owner.getUsername());
        return file;
    }

    @Override
    public RamUsageDTO getTotalUsedRamByUser(Long ownerId) {
        return new RamUsageDTO(ownerId, fileRepository.getTotalUsedRamByUser(ownerId));
    }

}
