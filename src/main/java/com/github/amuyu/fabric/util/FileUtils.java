package com.github.amuyu.fabric.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static void write(String filePath, String v) throws IOException {
        write(Paths.get(filePath).toFile(), v);
    }

    public static void write(File destination, String v) throws IOException {
        try (FileOutputStream out = new FileOutputStream(destination)) {
            out.write(v.getBytes());
        }
    }

    public static String read(String filePath) throws IOException {
        return read(Paths.get(filePath));
    }

    public static String read(Path path) throws IOException {
        byte[] b = Files.readAllBytes(path);
        return new String(b);
    }

    public static boolean exists(String filePath) {
        return exists(Paths.get(filePath));
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }
}
