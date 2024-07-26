package edu.wpi.first.deployutils;

import java.util.Stack;

public class PathUtils {
    public static String combine(String root, String relative) {
        return normalize(relative == null ? root : join(root, relative));
    }

    public static String join(String root, String relative) {
        if (relative.startsWith("/")) return relative;
        if (root.charAt(root.length() - 1) != '/') root += '/';
        return root += relative;
    }

    public static String normalize(String filepath) {
        String[] strings = filepath.split("/");
        Stack<String> s = new Stack<>();
        for (String str : strings) {
            if (str.trim().equals("..")) {
                s.pop();
            } else {
                s.push(str);
            }
        }
        return String.join("/", s);
    }
}
