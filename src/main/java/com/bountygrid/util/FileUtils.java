package com.bountygrid.util;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class FileUtils {
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");

    private FileUtils() {
    }

    public static String sanitize(String original) {
        if (original == null || !original.contains(".")) {
            throw new IllegalArgumentException("Invalid file type");
        }
        String ext = original.substring(original.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Invalid file type");
        }
        return UUID.randomUUID() + ext;
    }
}
