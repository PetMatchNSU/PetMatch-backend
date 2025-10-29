package org.nsu.files.util;

import org.apache.tika.Tika;

public class FileUtils {

    private static final Tika tika = new Tika();

    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public static String getFileExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        switch (mimeType) {
            case "image/jpeg":
                return "JPEG";
            case "image/png":
                return "PNG";
            case "application/pdf":
                return "PDF";
            default:
                return "";
        }
    }

    public static String detectMimeType(byte[] fileContent) {
        try {
            return tika.detect(fileContent);
        } catch (Exception e) {
            return "";
        }
    }
}
