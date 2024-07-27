package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import ru.mrsinkaaa.cloudfilestorage.config.RamConfig;
import ru.mrsinkaaa.cloudfilestorage.dto.ErrorDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.RamUsageDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.mrsinkaaa.cloudfilestorage.util.BreadcrumbsUtils.getBreadcrumbLinks;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getUserRootFolderPrefix;

@Service
@RequiredArgsConstructor
public class StorageViewService {

    private static final String STORAGE_VIEW_NAME = "storage";
    private static final String USER_ATTRIBUTE = "user";
    private static final String RAM_USAGE_ATTRIBUTE = "usedRam";
    private static final String PATH_LINKS_ATTRIBUTE = "pathLinks";
    private static final String FOLDERS_ATTRIBUTE = "folders";
    private static final String FILES_ATTRIBUTE = "files";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String MINIO_UTILS_ATTRIBUTE = "minioUtils";
    private static final String FOLDERS_PATH_LINKS_ATTRIBUTE = "foldersPathLinks";

    private final UserService userService;
    private final FolderService folderService;
    private final FileManagerService fileManagerService;
    private final FileService fileService;
    private final RamConfig ramConfig;

    public ModelAndView getMainPathModelAndView(User user, String path) {
        ModelAndView storage = new ModelAndView(STORAGE_VIEW_NAME);
        storage.addObject(USER_ATTRIBUTE, user);

        path = adjustPath(user, path);
        FolderDTO folder = folderService.findByOwnerAndMinioObjectId(user, path);

        addFolderAndFileData(storage, folder.getId());
        addUsedRamData(storage, user);
        addPathLinks(storage, folder, path);

        storage.addObject(MINIO_UTILS_ATTRIBUTE, new MinioRootFolderUtils());
        return storage;
    }

    public ModelAndView search(User user, String objectName) {
        ModelAndView storage = new ModelAndView(STORAGE_VIEW_NAME);
        storage.addObject(USER_ATTRIBUTE, user);

        List<FolderDTO> folders;
        if (objectName.contains("/")) {
            folders = folderService.findFoldersByOwnerAndFolderNameContainingIgnoreCase(user, objectName);
        } else {
            folders = new ArrayList<>();
            List<File> files = fileService.findFilesByOwnerAndFileNameContainingIgnoreCase(user, objectName);
            files.forEach(file -> {
                folders.add(folderService.findByFolderName(user, file.getFolderId().getFolderName()));
            });
        }

        if (folders.size() == 1) {
            addFolderAndFileData(storage, folders.get(0).getId());
            addPathLinks(storage, folders.get(0), objectName);
        } else {
            addFoldersPathLinks(storage, folders);
        }

        addUsedRamData(storage, user);
        storage.addObject(MINIO_UTILS_ATTRIBUTE, new MinioRootFolderUtils());
        return storage;
    }

    public ModelAndView getMainPageWithError(User user, String path, ErrorDTO errorDTO) {
        ModelAndView storage = getMainPathModelAndView(user, path);

        storage.addObject(ERROR_ATTRIBUTE, errorDTO);
        return storage;
    }

    private String adjustPath(User user, String path) {
        return path.isEmpty() ? getUserRootFolderPrefix(user.getUsername())
                : path;
    }

    private void addFoldersPathLinks(ModelAndView storage, List<FolderDTO> folders) {
        List<List<String>> foldersPathLinks =
                folders.stream()
                .map(folder -> getBreadcrumbLinks(folder.getMinioObjectId()))
                .toList();

        storage.addObject(FOLDERS_PATH_LINKS_ATTRIBUTE, foldersPathLinks);
    }

    private void addFolderAndFileData(ModelAndView storage, Long folderId) {
        storage.addObject(FOLDERS_ATTRIBUTE, folderService.findSubFolders(folderId));
        storage.addObject(FILES_ATTRIBUTE, fileManagerService.getFilesByFolder(folderId));
    }

    private void addUsedRamData(ModelAndView storage, User user) {
        var owner = userService.findByUsername(user.getUsername());
        double usedRam = fetchRAMUsage(fileService.getTotalUsedRamByUser(owner.getId()), ramConfig.getAvailableRamInBytes());
        storage.addObject(RAM_USAGE_ATTRIBUTE, usedRam);
    }

    private void addPathLinks(ModelAndView storage, FolderDTO folder, String path) {
        List<String> pathLinks = folder.getParentFolderId() != null ?
                getBreadcrumbLinks(folder.getMinioObjectId()) : getBreadcrumbLinks(path);
        storage.addObject(PATH_LINKS_ATTRIBUTE, pathLinks);
    }

    private double fetchRAMUsage(RamUsageDTO ramUsageDTO, long availableRamInBytes) {
        long usedRam = ramUsageDTO.getTotalUsedRam();
        double percentage = (usedRam / (double) availableRamInBytes) * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }
}

