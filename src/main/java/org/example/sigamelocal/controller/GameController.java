package org.example.sigamelocal.controller;

import org.example.sigamelocal.game.model.Game;
import org.example.sigamelocal.game.model.Host;
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
        return "Server is working!";
    }

    @GetMapping("/role")
    public String getRole() {

        if (gameService.getGame().getHost() == null) {
            return "HOST";
        }

        return "PLAYER";
    }

    @PostMapping("/host")
    public Host becomeHost(@RequestParam String name) {
        return gameService.becomeHost(name);
    }

    @PostMapping("/players")
    public Player addPlayer(@RequestParam String name) {
        return gameService.addPlayer(name);
    }

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return gameService.getGame().getPlayers();
    }

    @GetMapping("/game")
    public Game getGame() {
        return gameService.getGame();
    }

    @PostMapping("/game/start")
    public Game startGame() {
        gameService.startGame();
        return gameService.getGame();
    }

    @GetMapping("/players/{id}")
    public Player getPlayer(@PathVariable String id) {
        return gameService.getPlayerById(id);
    }
}