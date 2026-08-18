package org.example.sigamelocal.controller;

import org.example.sigamelocal.game.model.Pack;
import org.example.sigamelocal.game.service.GameService;
import org.example.sigamelocal.game.service.PackParser;
import org.example.sigamelocal.game.service.PackStorageService;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/pack")
public class PackController {

    private final PackParser packParser;
    private final GameService gameService;
    private final PackStorageService packStorageService;

    public PackController(
            PackParser packParser,
            GameService gameService,
            PackStorageService packStorageService
    ) {
        this.packParser = packParser;
        this.gameService = gameService;
        this.packStorageService = packStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPack(
            @RequestParam("file") MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Файл пустой");
        }

        String filename =
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename()
                                .toLowerCase();

        if (filename.endsWith(".zip")) {
            return uploadZip(file);
        }

        if (filename.endsWith(".json")) {
            return uploadJson(file);
        }

        return ResponseEntity
                .badRequest()
                .body("Загрузите ZIP-пак или старый JSON-пак");
    }

    @GetMapping("/assets")
    public ResponseEntity<?> getAsset(
            @RequestParam("path") String path
    ) {

        try {

            Resource resource =
                    packStorageService.getAsset(path);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType =
                    detectMediaType(path);

            return ResponseEntity
                    .ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "no-cache"
                    )
                    .body(resource);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IOException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    private ResponseEntity<?> uploadZip(
            MultipartFile file
    ) {

        Path directory = null;

        try {

            directory =
                    packStorageService.extractPack(
                            file
                    );

            Path packJson =
                    directory.resolve(
                            "pack.json"
                    );

            Pack pack;

            try (
                    var input =
                            Files.newInputStream(packJson)
            ) {
                pack =
                        packParser.parse(input);
            }

            gameService.loadPack(pack);

            packStorageService.activate(
                    directory
            );

            return ResponseEntity.ok(pack);

        } catch (
                IllegalArgumentException |
                IllegalStateException e
        ) {

            if (directory != null) {
                packStorageService.delete(directory);
            }

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IOException e) {

            if (directory != null) {
                packStorageService.delete(directory);
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Не удалось прочитать ZIP-пак"
                    );
        }
    }

    private ResponseEntity<?> uploadJson(
            MultipartFile file
    ) {

        try {

            Pack pack =
                    packParser.parse(
                            file.getInputStream()
                    );

            gameService.loadPack(pack);
            packStorageService.clear();

            return ResponseEntity.ok(pack);

        } catch (
                IllegalArgumentException |
                IllegalStateException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IOException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Не удалось прочитать JSON-файл"
                    );
        }
    }

    private MediaType detectMediaType(
            String path
    ) {

        String lower =
                path.toLowerCase();

        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        if (
                lower.endsWith(".jpg") ||
                        lower.endsWith(".jpeg")
        ) {
            return MediaType.IMAGE_JPEG;
        }

        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }

        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType(
                    "image/webp"
            );
        }

        if (lower.endsWith(".svg")) {
            return MediaType.parseMediaType(
                    "image/svg+xml"
            );
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
