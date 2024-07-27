package ru.mrsinkaaa.cloudfilestorage.service.interfaces;


import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.RamUsageDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;


import java.io.InputStream;
import java.util.List;

public interface IFileService {

    File findByOwnerAndFileName(User owner, String fileName);
    File findFileByOwnerIdAndId(Long ownerId, Long id);
    List<FileDTO> findByFolderId(Folder folderId);
    List<File> findFilesByOwnerAndFileNameContainingIgnoreCase(User owner, String fileName);
    InputStream downloadFile(User owner, Long id);
    File save(File file);
    File renameFile(User owner, Long id, String newFileName);
    File replaceFile(User owner, Long id, Folder newParentFolder);
    File deleteFile(User user, Long id);
    RamUsageDTO getTotalUsedRamByUser(Long ownerId);
}
