package com.task11.utils;

public class UrlUtils {
    public static String convertIdPath(String path) {
        if(path == null) {
            return "/";
        }
        if(path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        if(path.matches("\\/tables\\/\\S+")) {
            return "/tables/{tableId}";
        }
        return path;
    }
}
