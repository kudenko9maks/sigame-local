package org.example.sigamelocal.game.service;

import org.example.sigamelocal.game.model.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.example.sigamelocal.game.model.Question;
import org.example.sigamelocal.game.model.GameEvent;

import java.util.UUID;

@Service
public class GameService {

    private final Game game;
    private final SimpMessagingTemplate messagingTemplate;

    public GameService(SimpMessagingTemplate messagingTemplate) {
        this.game = new Game();
        this.messagingTemplate = messagingTemplate;
    }

    public Game getGame() {
        return game;
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

    public Player addPlayer(String name) {

        Player player = new Player(
                UUID.randomUUID().toString(),
                name
        );

        game.getPlayers().add(player);

        PlayerEvent event =
                new PlayerEvent("PLAYER_JOINED", player);

        messagingTemplate.convertAndSend(
                "/topic/game",
                event
        );

        return player;
    }

    public void startGame() {
        game.setState(GameState.QUESTION);
    }

    public Player getPlayerById(String id) {
        return game.getPlayers().stream()
                .filter(player -> player.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Host getHost() {
        return game.getHost();
    }

    public GameSnapshot getSnapshot() {
        return new GameSnapshot(
                game.getState(),
                game.getHost(),
                game.getPlayers().size()
        );
    }

    public PlayerSnapshot getPlayerSnapshot(String id) {

        Player player = getPlayerById(id);

        if (player == null) {
            return null;
        }

        return new PlayerSnapshot(
                player,
                getSnapshot()
        );
    }

    public void openTestQuestion() {
        Question question = new Question(
                "Столица Франции?",
                "Париж",
                100
        );

        game.setCurrentQuestion(question);
        game.setBuzzedPlayer(null);
        game.setState(GameState.QUESTION);

        broadcastGameUpdate("GAME_UPDATED");
    }

    public void startAnswering() {
        if (game.getCurrentQuestion() == null) {
            throw new IllegalStateException("No question currently exists");
        }
        game.setState(GameState.ANSWERING);

        broadcastGameUpdate("GAME_UPDATED");
    }

    public synchronized Player buzz(String playerId) {

        if (game.getState() != GameState.ANSWERING) {
            return null;
        }

        if (game.getBuzzedPlayer() != null) {
            return game.getBuzzedPlayer();
        }

        Player player = getPlayerById(playerId);

        if (player == null) {
            return null;
        }

        game.setBuzzedPlayer(player);

        broadcastGameUpdate("PLAYER_BUZZED");

        return player;
    }

    private void broadcastGameUpdate(String type) {
        GameEvent event = new GameEvent(type, game);

        messagingTemplate.convertAndSend("/topic/game", event);
    }
}
