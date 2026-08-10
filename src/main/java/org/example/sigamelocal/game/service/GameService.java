package org.example.sigamelocal.game.service;

import org.example.sigamelocal.game.model.Game;
import org.example.sigamelocal.game.model.GameState;
import org.example.sigamelocal.game.model.Player;
import org.example.sigamelocal.game.model.PlayerEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.example.sigamelocal.game.model.Host;


import java.util.UUID;

@Service
public class GameService {

    private final Game game;
    private final SimpMessagingTemplate messagingTemplate;

    public GameService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.game = new Game();
    }

    public Player addPlayer(String name) {
        Player player = new Player(
                name,
                UUID.randomUUID().toString()
        );
        game.getPlayers().add(player);

        PlayerEvent event = new PlayerEvent("PLAYER_JOINED", player);

        messagingTemplate.convertAndSend("/topic/game", event);

        return player;
    }

    public Game getGame() {
        return game;
    }

    public void startGame() {
        game.setState(GameState.QUESTION);
    }

    public Host becomeHost(String name) {
        if (game.getHost() != null) {
            throw new IllegalStateException("Host already exists");
        }

        Host host = new Host(
                UUID.randomUUID().toString(),
                name
        );

        game.setHost(host);

        return host;
    }
}
