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

import java.util.List;

import static ru.mrsinkaaa.cloudfilestorage.util.BreadcrumbsUtils.getBreadcrumbLinks;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getUserRootFolderPrefix;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.removeUserParentFolderPrefix;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final UserService userService;
    private final FolderService folderService;
    private final FileManagerService fileManagerService;
    private final FileService fileService;
    private final RamConfig ramConfig;

    public ModelAndView getMainPathModelAndView(User user, String path) {
        ModelAndView storage = new ModelAndView("storage");
        storage.addObject("user", user);

        path = adjustPath(user, path);
        FolderDTO folder = folderService.findByFolderName(user, path);

        addFolderAndFileData(storage, folder.getId());
        addUsedRamData(storage, user);
        addPathLinks(storage, folder, path);

        storage.addObject("minioRootFolderUtils", new MinioRootFolderUtils());
        return storage;
    }

    public ModelAndView search(User user, String objectName) {
        ModelAndView storage = new ModelAndView("storage");
        storage.addObject("user", user);

        FolderDTO folder;
        if(objectName.contains("/")) {
            folder = folderService.findByFolderName(user, objectName);
        } else {
            File file = fileService.findByOwnerAndFileName(user, objectName);
            folder = folderService.findByFolderName(user, file.getFolderId().getFolderName());
        }

        addFolderAndFileData(storage, folder.getId());
        addUsedRamData(storage, user);
        addPathLinks(storage, folder, objectName);

        storage.addObject("minioRootFolderUtils", new MinioRootFolderUtils());
        return storage;
    }

    public ModelAndView getMainPageWithError(User user, String path, ErrorDTO errorDTO) {
        ModelAndView storage = getMainPathModelAndView(user, path);

        storage.addObject("error", errorDTO);
        return storage;
    }

    private String adjustPath(User user, String path) {
        return path.isEmpty() ? getUserRootFolderPrefix(user.getUsername())
                : removeUserParentFolderPrefix(path) + "/";
    }

    private void addFolderAndFileData(ModelAndView modelAndView, Long folderId) {
        modelAndView.addObject("folders", folderService.findSubFolders(folderId));
        modelAndView.addObject("files", fileManagerService.getFilesByFolder(folderId));
    }

    private void addUsedRamData(ModelAndView modelAndView, User user) {
        var owner = userService.findByUsername(user.getUsername());
        double usedRam = fetchRAMUsage(fileService.getTotalUsedRamByUser(owner.getId()), ramConfig.getAvailableRamInBytes());
        modelAndView.addObject("usedRam", usedRam);
    }

    private void addPathLinks(ModelAndView modelAndView, FolderDTO folder, String path) {
        List<String> pathLinks = folder.getParentFolderId() != null ? getBreadcrumbLinks(folder.getMinioObjectId()) : getBreadcrumbLinks(path);
        modelAndView.addObject("pathLinks", pathLinks);
    }

    private double fetchRAMUsage(RamUsageDTO ramUsageDTO, long availableRamInBytes) {
        long usedRam = ramUsageDTO.getTotalUsedRam();
        double percentage = ( usedRam / (double) availableRamInBytes) * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }
}

