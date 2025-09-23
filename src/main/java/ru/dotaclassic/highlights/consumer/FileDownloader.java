package ru.dotaclassic.highlights.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class FileDownloader {
    private static final Logger log = LoggerFactory.getLogger(FileDownloader.class);


    /**
     * Downloads a ZIP from a URL, unzips it to a temporary directory,
     * executes the given consumer on the unzipped directory, then cleans up.
     *
     * @param url      URL of the ZIP file
     * @param consumer Lambda that takes the path of the unzipped directory
     */
    public void getReplay(String url, Consumer<Path> consumer) throws IOException {
        Path tempZip = Files.createTempFile("download-", ".zip");
        Path tempDir = Files.createTempDirectory("unzipped-");

        try {
            // Download ZIP
            try (InputStream in = new URI(url).toURL().openStream()) {
                Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
            }

            // Unzip
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(tempZip))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path newFile = tempDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
                        try (OutputStream out = Files.newOutputStream(newFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                out.write(buffer, 0, len);
                            }
                        }
                    }
                }
            }

            // Execute the lambda
            try (var replayList = Files.list(tempDir)) {
                var replay = replayList.findFirst().orElseThrow();
                consumer.accept(replay);
            }

        } catch (Exception e) {
            log.error("Couldn't download and unzip file! ", e);
        } finally {
            // Cleanup ZIP file
            Files.deleteIfExists(tempZip);
            // Cleanup extracted files recursively
            deleteDirectoryRecursively(tempDir);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var files = Files.walk(path)) {
            files.sorted((a, b) -> b.compareTo(a)) // delete children first
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Failed to delete {}", p, e);
                        }
                    });
        }
    }
}
