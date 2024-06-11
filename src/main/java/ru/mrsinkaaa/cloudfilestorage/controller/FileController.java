package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
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
public class FileController {

    private final FileService fileService;
    private final UserService userService;
    private final FolderService folderService;

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
        modelAndView.addObject("files", fileService.getFilesByFolder(folder.getId()));


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

    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile uploadFile,
                                   @AuthenticationPrincipal User user,
                                   @RequestParam("folderName") String folderName) {
        File file = fileService.uploadFile(user, uploadFile, folderName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @PostMapping("/files/rename")
    public String renameFile(@RequestParam("from") String oldName,
                                   @RequestParam("to") String newName) {
        File file = fileService.renameFile(oldName, newName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @DeleteMapping("/files/delete")
    public String deleteFile(@AuthenticationPrincipal User user,
                                   @RequestParam("id") Long id) {
        File file = fileService.deleteFile(user, id);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @PostMapping("/files/createFolder")
    public String createFolder(@RequestParam("folderName") String folderName,
                               @RequestParam("parentFolder") String parentFolder,
                               @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        folderService.createFolder(owner, folderName, parentFolder);

        return "redirect:/?path=" + (parentFolder == null ? getUserRootFolderPrefix(user.getUsername()) : parentFolder);
    }

}
