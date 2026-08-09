package org.example.sigamelocal.game.service;

import org.example.sigamelocal.game.model.Game;
import org.example.sigamelocal.game.model.GameState;
import org.example.sigamelocal.game.model.Player;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameService {
    private final Game game;
    public GameService() {
        this.game = new Game();
    }

    public Player addPlayer(String name) {
        Player player = new Player(
                name,
                UUID.randomUUID().toString()
        );
        game.getPlayers().add(player);
        return player;
    }

    public Game getGame() {
        return game;
    }

    public void startGame() {
        game.setState(GameState.QUESTION);
    }
}
