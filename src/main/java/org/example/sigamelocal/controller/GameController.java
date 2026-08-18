package org.example.sigamelocal.controller;

import org.example.sigamelocal.game.model.*;
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
        return gameService.getGame().getHost() == null
                ? "HOST"
                : "PLAYER";
    }

    @PostMapping("/host")
    public Host becomeHost(@RequestParam String name) {
        return gameService.becomeHost(name);
    }

    @GetMapping("/host")
    public Host getHost() {
        return gameService.getGame().getHost();
    }

    @PostMapping("/players")
    public Player addPlayer(@RequestParam String name) {
        return gameService.addPlayer(name);
    }

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return gameService.getGame().getPlayers();
    }

    @GetMapping("/players/{id}")
    public Player getPlayer(@PathVariable String id) {
        return gameService.getPlayerById(id);
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

    @PostMapping("/game/next-round")
    public Game nextRound() {
        return gameService.nextRoundForTest();
    }

    @GetMapping("/game/snapshot")
    public GameSnapshot getSnapshot() {
        return gameService.getSnapshot();
    }

    @GetMapping("/players/{id}/snapshot")
    public PlayerSnapshot getSnapshot(
            @PathVariable String id
    ) {
        return gameService.getPlayerSnapshot(id);
    }

    @PostMapping("/game/question")
    public Game openQuestion() {
        gameService.openTestQuestion();
        return gameService.getGame();
    }

    @PostMapping("/game/answering")
    public Game startAnswering() {
        gameService.startAnswering();
        return gameService.getGame();
    }

    @PostMapping("/game/buzz/{playerId}")
    public Player buzz(@PathVariable String playerId) {
        return gameService.buzz(playerId);
    }

    @PostMapping("/game/answer/correct")
    public Player answerCorrectly() {
        return gameService.answerCorrectly();
    }

    @PostMapping("/game/answer/incorrect")
    public Player answerIncorrectly() {
        return gameService.answerIncorrectly();
    }

    @PostMapping("/game/question/select")
    public Question selectQuestion(
            @RequestParam String category,
            @RequestParam int price
    ) {
        return gameService.selectQuestion(
                category,
                price
        );
    }
}
