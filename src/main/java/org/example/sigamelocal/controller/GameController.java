package org.example.sigamelocal.controller;

import org.example.sigamelocal.game.model.Player;
import org.example.sigamelocal.game.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/test")
    public String test() {
        return "Server works";
    }

    @PostMapping("/players")
    public Player adddPlayer(@RequestParam String name) {
        return gameService.addPlayer(name);
    }

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return gameService.getPlayers();
    }
}
