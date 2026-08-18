package org.example.sigamelocal.game.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class PackStorageService {

    private static final int MAX_ENTRIES = 1_000;

    private static final long MAX_ENTRY_SIZE =
            25L * 1024 * 1024;

    private static final long MAX_TOTAL_SIZE =
            100L * 1024 * 1024;

    private Path activePackDirectory;

    public synchronized Path extractPack(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "ZIP-пак пустой"
            );
        }

        Path directory =
                Files.createTempDirectory(
                        "sigame-pack-"
                );

        boolean success = false;

        try (
                InputStream input =
                        file.getInputStream();
                ZipInputStream zip =
                        new ZipInputStream(input)
        ) {

            ZipEntry entry;

            int entryCount = 0;

            long extractedSize = 0;

            while (
                    (entry = zip.getNextEntry()) != null
            ) {

                entryCount++;

                if (entryCount > MAX_ENTRIES) {
                    throw new IllegalArgumentException(
                            "В ZIP слишком много файлов"
                    );
                }

                String name =
                        entry.getName()
                                .replace('\\', '/');

                validateEntryName(name);

                Path target =
                        directory.resolve(name)
                                .normalize();

                if (
                        !target.startsWith(
                                directory
                        )
                ) {
                    throw new IllegalArgumentException(
                            "Недопустимый путь в ZIP: " +
                                    name
                    );
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Path parent =
                            target.getParent();

                    if (parent != null) {
                        Files.createDirectories(parent);
                    }

                    extractedSize = copyEntry(
                            zip,
                            target,
                            extractedSize
                    );
                }

                zip.closeEntry();
            }

            Path packJson =
                    directory.resolve(
                            "pack.json"
                    );

            if (!Files.isRegularFile(packJson)) {
                throw new IllegalArgumentException(
                        "В ZIP должен находиться pack.json"
                );
            }

            success = true;

            return directory;

        } finally {

            if (!success) {
                deleteDirectory(directory);
            }
        }
    }

    public synchronized void activate(
            Path directory
    ) {

        Path old =
                activePackDirectory;

        activePackDirectory =
                directory;

        if (
                old != null &&
                        !old.equals(directory)
        ) {
            deleteDirectory(old);
        }
    }

    public synchronized Resource getAsset(
            String relativePath
    ) throws IOException {

        if (
                activePackDirectory == null
        ) {
            return null;
        }

        String normalized =
                relativePath
                        .replace('\\', '/');

        if (
                normalized.isBlank() ||
                        normalized.startsWith("/") ||
                        normalized.contains("../") ||
                        normalized.equals("..")
        ) {
            throw new IllegalArgumentException(
                    "Недопустимый путь к ресурсу"
            );
        }

        Path asset =
                activePackDirectory
                        .resolve(normalized)
                        .normalize();

        if (
                !asset.startsWith(
                        activePackDirectory
                ) ||
                        !Files.isRegularFile(asset)
        ) {
            return null;
        }

        return new UrlResource(
                asset.toUri()
        );
    }

    public synchronized String getPackJsonContent(
            Path directory
    ) throws IOException {

        return Files.readString(
                directory.resolve("pack.json")
        );
    }

    private void validateEntryName(
            String name
    ) {

        if (
                name.isBlank() ||
                        name.startsWith("/") ||
                        name.contains("../") ||
                        name.equals("..") ||
                        name.contains(":/") ||
                        name.contains("\u0000")
        ) {
            throw new IllegalArgumentException(
                    "Недопустимый путь в ZIP: " +
                            name
            );
        }
    }

    private long copyEntry(
            ZipInputStream zip,
            Path target,
            long extractedSize
    ) throws IOException {

        long entrySize = 0;

        byte[] buffer = new byte[8192];

        try (
                var output =
                        Files.newOutputStream(target)
        ) {

            int read;

            while ((read = zip.read(buffer)) != -1) {

                if (
                        entrySize > MAX_ENTRY_SIZE - read
                ) {
                    throw new IllegalArgumentException(
                            "Файл в ZIP слишком большой"
                    );
                }

                if (
                        extractedSize > MAX_TOTAL_SIZE - read
                ) {
                    throw new IllegalArgumentException(
                            "Распакованный ZIP слишком большой"
                    );
                }

                output.write(buffer, 0, read);

                entrySize += read;
                extractedSize += read;
            }
        }

        return extractedSize;
    }

    public synchronized void delete(
            Path directory
    ) {
        deleteDirectory(directory);
    }

    public synchronized void clear() {

        Path old =
                activePackDirectory;

        activePackDirectory =
                null;

        deleteDirectory(old);
    }

    private void deleteDirectory(
            Path directory
    ) {

        if (
                directory == null ||
                        !Files.exists(directory)
        ) {
            return;
        }

        try (
                var stream =
                        Files.walk(directory)
        ) {
            stream
                    .sorted(
                            java.util.Comparator.reverseOrder()
                    )
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            }
                    );
        } catch (IOException ignored) {
        }
    }
}
