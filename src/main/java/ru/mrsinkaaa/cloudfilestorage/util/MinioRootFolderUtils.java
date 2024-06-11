package ru.mrsinkaaa.cloudfilestorage.util;

public class MinioRootFolderUtils {

    public static String getUserRootFolderPrefix(String username) {
        return "user-" + username + "-files/";
    }

    public static String removeUserRootFolderPrefix(String path, String username) {
        return path.substring(getUserRootFolderPrefix(username).length());
    }

    public static String removeUserParentFolderPrefix(String path) {
        String[] folders = path.split("/");
        return folders[folders.length - 1];
    }
}
