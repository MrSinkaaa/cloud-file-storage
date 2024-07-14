package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.io.InputStream;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final IFileService fileService;
    private final IUserService userService;
    private final FileManagerService fileManagerService;

    @GetMapping
    public ResponseEntity<Resource> downloadFile(@RequestParam("id") Long fileId,
                                                 @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        File file = fileService.findFileByOwnerIdAndId(owner.getId(), fileId);

        InputStream inputStream = fileService.downloadFile(owner, fileId);
        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getFileSize())
                .body(resource);
    }

    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile uploadFile,
                                   @AuthenticationPrincipal User user,
                                   @RequestParam("folderName") String folderName) {

        var owner = userService.findByUsername(user.getUsername());
        File file = fileManagerService.uploadFile(owner, uploadFile, folderName);

        return "redirect:/?path=" + file.getFolderId().getMinioObjectId();
    }

    @PatchMapping
    public String renameFile(@RequestParam("id") Long id,
                                   @RequestParam("newName") String newName,
                             @AuthenticationPrincipal User user) {
        var owner = userService.findByUsername(user.getUsername());
        File file = fileService.renameFile(owner, id, newName);

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
