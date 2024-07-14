package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.service.FileManagerService;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFolderService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/folder")
public class FolderController {

    private final IFolderService folderService;
    private final UserService userService;
    private final FileManagerService fileManagerService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFolder(@RequestParam("folder") MultipartFile[] folder,
                                          @RequestParam("parentFolder") String parentFolder,
                                          @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        fileManagerService.uploadFolder(owner, folder, parentFolder);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam("folderName") String folderName,
                               @RequestParam("parentFolder") String parentFolder,
                               @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        folderService.createFolder(owner, folderName, parentFolder);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rename")
    public ResponseEntity<?> renameFolder(@RequestParam("id") Long folderId,
                               @RequestParam("newFolderName") String newFolderName,
                               @AuthenticationPrincipal User user) {
        var owner = userService.findByUsername(user.getUsername());
        fileManagerService.renameFolder(owner, folderId, newFolderName);

        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFolder(@RequestParam("id") Long folderId,
                               @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        fileManagerService.deleteFolder(owner, folderId);
        return ResponseEntity.ok().build();
    }
}
