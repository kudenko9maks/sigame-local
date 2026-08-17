package org.example.sigamelocal.controller;

import org.example.sigamelocal.game.model.Pack;
import org.example.sigamelocal.game.service.GameService;
import org.example.sigamelocal.game.service.PackParser;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pack")
public class PackController {

    private final PackParser packParser;
    private final GameService gameService;

    public PackController(PackParser packParser, GameService gameService) {
        this.packParser = packParser;
        this.gameService = gameService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPack(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Файл пустой");
        }

        try {

            Pack pack =
                    packParser.parse(
                            file.getInputStream()
                    );

            gameService.loadPack(pack);

            return ResponseEntity.ok(pack);

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );


        } catch (
                IllegalStateException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );


        } catch (
                IOException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Не удалось прочитать JSON-файл"
                    );
        }
    }
}