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

    public void loadPack(Pack pack) {
        if (pack == null) {
            throw new IllegalArgumentException("Pack cannot be null");
        }

        if (pack.getRounds() == null || pack.getRounds().isEmpty()) {
            throw new IllegalArgumentException("Rounds cannot be null");
        }

        if (game.getState() != GameState.LOBBY) {
            throw new IllegalArgumentException("Game is not in lobby");
        }

        game.setPack(pack);
        game.setCurrentRound(0);
        game.getCategories().clear();
        game.getCategories().addAll(pack.getRounds().get(0).getCategories());
        game.setCurrentQuestion(null);
        game.setBuzzedPlayer(null);
        game.setTurnPlayer(null);
        game.setState(GameState.LOBBY);
        broadcastGameUpdate("PACK_LOADED");
    }

    public Pack getPack() {
        return game.getPack();
    }

    public int  getCurrentRound() {
        return game.getCurrentRound();
    }

    public  Round getCurrentRoundData() {
        Pack pack = game .getPack();

        if (pack == null) {
            return null;
        }
        int roundIndex = game.getCurrentRound();

        if (roundIndex < 0 || roundIndex >= pack.getRounds().size()) {
            return null;
        }
        return pack.getRounds().get(roundIndex);
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

        broadcastGameUpdate("PLAYER_JOINED");

        return player;
    }

    public void startGame() {

        if (game.getPack() == null) {
            throw new IllegalArgumentException("Pack cannot be null");
        }

        if (game.getPlayers().isEmpty()) {
            throw new IllegalStateException(
                    "There are no players"
            );
        }

        game.setCurrentRound(0);

        loadCurrentRoundCategories();

        Player firstPlayer =
                game.getPlayers().get(0);

        game.setTurnPlayer(firstPlayer);

        game.setCurrentQuestion(null);

        game.setBuzzedPlayer(null);

        game.setState(GameState.BOARD);

        broadcastGameUpdate("GAME_STARTED");

        broadcastPlayerUpdate(
                "GAME_STARTED"
        );
    }

    private void loadCurrentRoundCategories() {
        Pack pack = game.getPack();

        if (pack == null) {
            throw new IllegalArgumentException("Pack cannot be null");
        }

        int roundIndex = game.getCurrentRound();

        if (roundIndex < 0 || roundIndex >= pack.getRounds().size()) {
            throw new IllegalArgumentException("Round index out of bounds");
        }

        Round round = pack.getRounds().get(roundIndex);
        game.getCategories().clear();
        game.getCategories().addAll(round.getCategories());
    }

    private boolean currentRoundFinished() {
        return game.getCategories()
                .stream()
                .allMatch(
                        category ->
                                category.getQuestions()
                                        .stream()
                                        .allMatch(
                                                Question::isUsed
                                        )
                );
    }

    private boolean hasNextRound() {
        Pack pack = game.getPack();

        return pack != null && game.getCurrentRound() + 1 < pack.getRounds().size();
    }

    private void startNextRound() {
        if (!hasNextRound()) {

            game.setState(
                    GameState.FINISHED
            );

            game.setCurrentQuestion(
                    null
            );

            game.setBuzzedPlayer(
                    null
            );


            broadcastGameUpdate(
                    "GAME_FINISHED"
            );


            broadcastPlayerUpdate(
                    "GAME_FINISHED"
            );


            return;
        }

        game.setCurrentRound(game.getCurrentRound() + 1);
        loadCurrentRoundCategories();
        game.setCurrentQuestion(null);
        game.setBuzzedPlayer(null);
        game.setState(GameState.BOARD);
        broadcastGameUpdate("ROUND_STARTED");
        broadcastPlayerUpdate("ROUND_STARTED");
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
                game.getTurnPlayer(),
                game.getPlayers().size()
        );
    }

    public PlayerSnapshot getPlayerSnapshot(String playerId) {

        Player player = getPlayerById(playerId);

        if (player == null) {
            return null;
        }

        boolean canBuzz =
                game.getState() == GameState.ANSWERING
                        && game.getBuzzedPlayer() == null;

        return new PlayerSnapshot(
                player,
                game.getState(),
                canBuzz,
                game.getBuzzedPlayer()
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
            throw new IllegalStateException(
                    "No question is currently open"
            );
        }

        game.setState(GameState.ANSWERING);

        broadcastGameUpdate("GAME_UPDATED");
        broadcastPlayerUpdate("GAME_UPDATED");
    }

    public synchronized Player buzz(String playerId) {

        if (game.getState() != GameState.ANSWERING) {
            return null;
        }

        if (game.getBuzzedPlayer() != null) {
            return game.getBuzzedPlayer();
        }

        Player player =
                getPlayerById(playerId);

        if (player == null) {
            return null;
        }

        game.setBuzzedPlayer(player);

        broadcastGameUpdate("PLAYER_BUZZED");
        broadcastPlayerUpdate("PLAYER_BUZZED");

        return player;
    }

    private void broadcastGameUpdate(String type) {

        GameEvent event =
                new GameEvent(type, game);

        messagingTemplate.convertAndSend(
                "/topic/host",
                event
        );
    }

    public Player answerCorrectly() {

        Player player =
                game.getBuzzedPlayer();

        if (player == null) {
            throw new IllegalStateException(
                    "No player is answering"
            );
        }

        Question question =
                game.getCurrentQuestion();

        if (question == null) {
            throw new IllegalStateException("No question is currently open");
        }

        player.setScore(
                player.getScore()
                        + question.getPrice()
        );

        game.setTurnPlayer(player);

        game.setBuzzedPlayer(null);

        game.setCurrentQuestion(null);

        if (currentRoundFinished()) {
            startNextRound();
            return player;
        }

        game.setState(GameState.BOARD);

        broadcastGameUpdate("ANSWER_CORRECT");
        broadcastPlayerUpdate("ANSWER_CORRECT");

        return player;
    }

    public Player answerIncorrectly() {

        Player player =
                game.getBuzzedPlayer();

        if (player == null) {
            throw new IllegalStateException(
                    "No player is answering"
            );
        }

        game.setBuzzedPlayer(null);

        game.setState(GameState.ANSWERING);

        broadcastGameUpdate("ANSWER_INCORRECT");
        broadcastPlayerUpdate("ANSWER_INCORRECT");

        return player;
    }

    public Question selectQuestion(
            String categoryName,
            int price
    ) {

        if (game.getState() != GameState.BOARD) {
            throw new IllegalStateException(
                    "Question cannot be selected now"
            );
        }

        Category category =
                game.getCategories()
                        .stream()
                        .filter(c ->
                                c.getName()
                                        .equals(categoryName)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Category not found"
                                )
                        );


        Question question =
                category.getQuestions()
                        .stream()
                        .filter(q ->
                                q.getPrice() == price
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Question not found"
                                )
                        );


        if (question.isUsed()) {
            throw new IllegalStateException(
                    "Question has already been used"
            );
        }


        question.setUsed(true);

        game.setCurrentQuestion(question);
        game.setBuzzedPlayer(null);
        game.setState(GameState.QUESTION);

        broadcastGameUpdate("QUESTION_SELECTED");
        broadcastPlayerUpdate("QUESTION_SELECTED");

        return question;
    }

    private void broadcastPlayerUpdate(String type) {

        for (Player player : game.getPlayers()) {

            boolean canBuzz =
                    game.getState() == GameState.ANSWERING
                            && game.getBuzzedPlayer() == null;

            PlayerSnapshot snapshot =
                    new PlayerSnapshot(
                            player,
                            game.getState(),
                            canBuzz,
                            game.getBuzzedPlayer()
                    );

            PlayerGameEvent event =
                    new PlayerGameEvent(
                            type,
                            snapshot
                    );

            messagingTemplate.convertAndSend(
                    "/topic/players",
                    event
            );
        }
    }
}
