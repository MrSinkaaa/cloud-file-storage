package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.service.FileManagerService;
import ru.mrsinkaaa.cloudfilestorage.service.FileService;
import ru.mrsinkaaa.cloudfilestorage.service.FolderService;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final FolderService folderService;
    private final UserService userService;
    private final FileService fileService;
    private final FileManagerService fileManagerService;

    @GetMapping("/search")
    public ModelAndView search(@AuthenticationPrincipal User user,
                               @RequestParam("query") String objectName) {
        ModelAndView modelAndView = new ModelAndView("storage");
        modelAndView.addObject("user", user);

        FolderDTO folder;
        if(objectName.contains("/")) {
            var owner = userService.findByUsername(user.getUsername());

            folder = folderService.findByFolderName(owner, objectName);
            modelAndView.addObject("folders", folderService.findSubFolders(folder.getParentFolderId()));
        } else {
            FileDTO file = fileService.findByFileName(objectName);
            modelAndView.addObject("folders", folderService.findSubFolders(file.getParentFolderId()));
            modelAndView.addObject("files", fileManagerService.getFilesByFolder(file.getParentFolderId()));
        }

        return modelAndView;
    }
}
