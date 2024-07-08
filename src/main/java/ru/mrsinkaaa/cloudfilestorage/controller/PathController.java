package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.service.FileManagerService;
import ru.mrsinkaaa.cloudfilestorage.service.FileService;
import ru.mrsinkaaa.cloudfilestorage.service.FolderService;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;
import ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils;

import java.util.List;

import static ru.mrsinkaaa.cloudfilestorage.util.BreadcrumbsUtils.getBreadcrumbLinks;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getUserRootFolderPrefix;
import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.removeUserParentFolderPrefix;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PathController {

    private final FileService fileService;
    private final UserService userService;
    private final FolderService folderService;
    private final FileManagerService fileManagerService;

    @GetMapping("/")
    public ModelAndView getPath(@RequestParam(value = "path", required = false) String path,
                                @AuthenticationPrincipal User user) {

        ModelAndView modelAndView = new ModelAndView("storage");
        modelAndView.addObject("user", user);

        path = path.isEmpty() ?
                getUserRootFolderPrefix(user.getUsername())
                : removeUserParentFolderPrefix(path) + "/";

        var owner = userService.findByUsername(user.getUsername());
        FolderDTO folder = folderService.findByFolderName(owner, path);
        modelAndView.addObject("folders", folderService.findSubFolders(folder.getId()));
        modelAndView.addObject("files", fileManagerService.getFilesByFolder(folder.getId()));


        List<String> pathLinks;
        if(folder.getParentFolderId() != null) {
            pathLinks = getBreadcrumbLinks(folder.getMinioObjectId());
        } else {
            pathLinks = getBreadcrumbLinks(path);
        }
        modelAndView.addObject("pathLinks", pathLinks);
        modelAndView.addObject("minioRootFolderUtils", new MinioRootFolderUtils());

        return modelAndView;
    }
}
