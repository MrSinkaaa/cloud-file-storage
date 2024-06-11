package ru.mrsinkaaa.cloudfilestorage.util;

import java.util.ArrayList;
import java.util.List;

public class BreadcrumbsUtils {

    public static List<String> getBreadcrumbLinks(String path) {
        if(path.isEmpty()) {
            return List.of(path);
        }

        List<String> breadcrumbs = new ArrayList<>();
        for(int i = 0; i < path.length(); i++) {
            if(path.charAt(i) == '/') {
                breadcrumbs.add(path.substring(0, i) + "/");
            }
        }

        return breadcrumbs;
    }

}
