package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.service.FileService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile uploadFile,
                                   @AuthenticationPrincipal User user,
                                   @RequestParam("folderName") String folderName) {
        File file = fileService.uploadFile(user, uploadFile, folderName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam("from") String oldName,
                                   @RequestParam("to") String newName) {
        File file = fileService.renameFile(oldName, newName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @DeleteMapping("/delete")
    public String deleteFile(@AuthenticationPrincipal User user,
                                   @RequestParam("id") Long id) {
        File file = fileService.deleteFile(user, id);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

}
