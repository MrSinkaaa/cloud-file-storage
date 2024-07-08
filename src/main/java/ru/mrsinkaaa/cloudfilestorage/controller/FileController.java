package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.service.FileManagerService;
import ru.mrsinkaaa.cloudfilestorage.service.FileService;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFileService;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IUserService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final IFileService fileService;
    private final IUserService userService;
    private final FileManagerService fileManagerService;

    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile uploadFile,
                                   @AuthenticationPrincipal User user,
                                   @RequestParam("folderName") String folderName) {

        var owner = userService.findByUsername(user.getUsername());
        File file = fileManagerService.uploadFile(owner, uploadFile, folderName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @PatchMapping
    public String renameFile(@RequestParam("from") String oldName,
                                   @RequestParam("to") String newName) {
        File file = fileService.renameFile(oldName, newName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @DeleteMapping
    public String deleteFile(@AuthenticationPrincipal User user,
                                   @RequestParam("id") Long id) {

        var owner = userService.findByUsername(user.getUsername());
        File file = fileService.deleteFile(owner, id);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

}
