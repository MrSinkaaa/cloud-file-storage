package ru.mrsinkaaa.cloudfilestorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.mrsinkaaa.cloudfilestorage.service.StorageService;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;

@Controller
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;
    private final UserService userService;

    @GetMapping("/")
    public ModelAndView getPath(@RequestParam(value = "path", required = false) String path,
                                @AuthenticationPrincipal User user) {

        var owner = userService.findByUsername(user.getUsername());
        return storageService.getMainPathModelAndView(owner, path);
    }

    @GetMapping("/search")
    public ModelAndView search(@AuthenticationPrincipal User user,
                               @RequestParam("query") String objectName) {

        var owner = userService.findByUsername(user.getUsername());
        return storageService.search(owner, objectName);
    }
}
