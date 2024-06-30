package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mrsinkaaa.cloudfilestorage.service.FolderService;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;

import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getUserRootFolderPrefix;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/folder")
public class FolderController {

    private final FolderService folderService;
    private final UserService userService;

    @PostMapping("/upload")
    public String uploadFolder(@RequestParam("folderName") String folderName,
                               @RequestParam("parentFolder") String parentFolder,
                               @AuthenticationPrincipal User user) {
        var owner = userService.findByUsername(user.getUsername());

        folderService.uploadFolder(owner, folderName, parentFolder);

        return "redirect:/?path=" + (parentFolder == null? getUserRootFolderPrefix(user.getUsername()) : parentFolder);
    }


    @PostMapping("/create")
    public String createFolder(@RequestParam("folderName") String folderName,
                               @RequestParam("parentFolder") String parentFolder,
                               @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        folderService.createFolder(owner, folderName, parentFolder);

        return "redirect:/?path=" + (parentFolder == null ? getUserRootFolderPrefix(user.getUsername()) : parentFolder);
    }
}
