package com.skrra.atmosphereplus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Durable text file writes: content is written to a temporary file in the target's
 * directory and then moved over the target, so a crash mid-write can never leave a
 * truncated file behind. Callers serialize to a String before calling so that a
 * serialization failure cannot touch the live file either.
 */
public final class SafeFileIo {
    private static final Logger LOGGER = LoggerFactory.getLogger("atmosphereplus");
    private static final DateTimeFormatter QUARANTINE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private SafeFileIo() {
    }

    public static void writeString(Path target, String content) throws IOException {
        Path absoluteTarget = target.toAbsolutePath();
        Path parent = absoluteTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temp = Files.createTempFile(parent, absoluteTarget.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, absoluteTarget, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temp, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // Best-effort cleanup; the original write failure is the error that matters.
            }
            throw exception;
        }
    }

    /**
     * Copies a file that failed to load to a timestamped quarantine name next to the
     * original, so fallback behavior (defaults, empty sets, later saves) can never be
     * the only remaining copy of the user's data. The original file is left in place.
     *
     * @return the quarantine path, or {@code null} if the file is missing or the copy failed
     */
    public static Path quarantineCorruptFile(Path target) {
        try {
            if (!Files.exists(target)) {
                return null;
            }

            Path backup = quarantinePath(target);
            Files.copy(target, backup);
            LOGGER.warn("Preserved unreadable file {} as {}", target, backup);
            return backup;
        } catch (IOException exception) {
            LOGGER.warn("Could not create quarantine backup of {}", target, exception);
            return null;
        }
    }

    private static Path quarantinePath(Path target) {
        String name = target.getFileName().toString();
        String base = name.toLowerCase().endsWith(".json") ? name.substring(0, name.length() - ".json".length()) : name;
        String stamp = LocalDateTime.now().format(QUARANTINE_TIMESTAMP);

        Path parent = target.toAbsolutePath().getParent();
        Path candidate = parent.resolve(base + ".corrupt-" + stamp + ".json");
        int suffix = 2;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(base + ".corrupt-" + stamp + "-" + suffix++ + ".json");
        }
        return candidate;
    }
}
