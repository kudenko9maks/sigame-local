package org.example.sigamelocal.game.service;

import org.example.sigamelocal.game.model.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {

    private final Game game;

    private final SimpMessagingTemplate messagingTemplate;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> presentationFuture;

    private ScheduledFuture<?> answerRevealFuture;

    private static final long ANSWERING_DURATION = 15000L;
    private static final long ANSWER_REVEAL_DURATION = 5000L;

    public GameService(
            SimpMessagingTemplate messagingTemplate
    ) {

        this.game = new Game();

        this.messagingTemplate =
                messagingTemplate;
    }

    public Game getGame() {

        return game;
    }

    public Pack getPack() {

        return game.getPack();
    }

    public int getCurrentRound() {

        return game.getCurrentRound();
    }

    public Round getCurrentRoundData() {

        Pack pack =
                game.getPack();

        if (pack == null) {
            return null;
        }

        int roundIndex =
                game.getCurrentRound();

        if (
                roundIndex < 0 ||
                        roundIndex >= pack.getRounds().size()
        ) {

            return null;
        }

        return pack
                .getRounds()
                .get(roundIndex);
    }

    public synchronized void loadPack(
            Pack pack
    ) {

        if (pack == null) {

            throw new IllegalArgumentException(
                    "Pack cannot be null"
            );
        }

        if (
                pack.getRounds() == null ||
                        pack.getRounds().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Rounds cannot be null"
            );
        }

        if (
                game.getState() !=
                        GameState.LOBBY
        ) {

            throw new IllegalArgumentException(
                    "Game is not in lobby"
            );
        }

        cancelPresentationTimer();
        cancelAnswerRevealTimer();
        clearPresentation();
        resetAnswerTimer();

        game.setPack(pack);

        game.setCurrentRound(0);

        game.getCategories().clear();

        game.getCategories().addAll(
                pack
                        .getRounds()
                        .get(0)
                        .getCategories()
        );

        game.setCurrentQuestion(null);

        game.setBuzzedPlayer(null);

        game.setTurnPlayer(null);

        game.setState(
                GameState.LOBBY
        );

        broadcastGameUpdate(
                "PACK_LOADED"
        );

        broadcastPlayerUpdate(
                "PACK_LOADED"
        );
    }

    public Host becomeHost(
            String name
    ) {

        if (
                game.getHost() != null
        ) {

            throw new IllegalStateException(
                    "Host already exists"
            );
        }

        Host host =
                new Host(
                        UUID.randomUUID().toString(),
                        name
                );

        game.setHost(host);

        return host;
    }

    public Host getHost() {

        return game.getHost();
    }

    public Player addPlayer(
            String name
    ) {

        Player player =
                new Player(
                        UUID.randomUUID().toString(),
                        name
                );

        game.getPlayers().add(
                player
        );

        PlayerEvent event =
                new PlayerEvent(
                        "PLAYER_JOINED",
                        player
                );

        messagingTemplate.convertAndSend(
                "/topic/game",
                event
        );

        broadcastGameUpdate(
                "PLAYER_JOINED"
        );

        broadcastPlayerUpdate(
                "PLAYER_JOINED"
        );

        return player;
    }

    public Player getPlayerById(
            String id
    ) {

        return game
                .getPlayers()
                .stream()
                .filter(
                        player ->
                                player
                                        .getId()
                                        .equals(id)
                )
                .findFirst()
                .orElse(null);
    }

    public synchronized void startGame() {

        if (
                game.getPack() == null
        ) {

            throw new IllegalArgumentException(
                    "Pack cannot be null"
            );
        }

        if (
                game.getPlayers().isEmpty()
        ) {

            throw new IllegalStateException(
                    "There are no players"
            );
        }

        cancelPresentationTimer();

        clearPresentation();

        game.setCurrentRound(0);

        loadCurrentRoundCategories();

        Player firstPlayer =
                game
                        .getPlayers()
                        .get(0);

        game.setTurnPlayer(
                firstPlayer
        );

        game.setCurrentQuestion(
                null
        );

        game.setBuzzedPlayer(
                null
        );

        startInitialPresentation();
    }

    private synchronized void startInitialPresentation() {

        if (
                game.getPack() == null
        ) {

            throw new IllegalStateException(
                    "No pack loaded"
            );
        }

        cancelPresentationTimer();

        game.setCurrentRound(0);

        loadCurrentRoundCategories();

        game.setCurrentQuestion(
                null
        );

        game.setBuzzedPlayer(
                null
        );

        game.setPresentationType(
                "INITIAL"
        );

        game.setPresentationStartedAt(
                System.currentTimeMillis()
        );

        game.setPresentationDuration(
                6500
        );

        game.setState(
                GameState.PRESENTATION
        );

        broadcastGameUpdate(
                "INITIAL_PRESENTATION_STARTED"
        );

        broadcastPlayerUpdate(
                "INITIAL_PRESENTATION_STARTED"
        );

        presentationFuture =
                scheduler.schedule(
                        this::finishInitialPresentation,
                        6500,
                        TimeUnit.MILLISECONDS
                );
    }

    private synchronized void finishInitialPresentation() {

        if (
                game.getState() !=
                        GameState.PRESENTATION
        ) {

            return;
        }

        if (
                !"INITIAL".equals(
                        game.getPresentationType()
                )
        ) {

            return;
        }

        cancelPresentationTimer();

        startRoundPresentation();
    }

    private synchronized void startRoundPresentation() {

        Round round =
                getCurrentRoundData();

        if (round == null) {

            finishPresentationToBoard();

            return;
        }

        int categoryCount =
                round
                        .getCategories()
                        .size();

        if (
                categoryCount == 0
        ) {

            finishPresentationToBoard();

            return;
        }

        cancelPresentationTimer();

        long duration =
                categoryCount * 2000L;

        game.setPresentationType(
                "ROUND"
        );

        game.setPresentationStartedAt(
                System.currentTimeMillis()
        );

        game.setPresentationDuration(
                duration
        );

        game.setState(
                GameState.PRESENTATION
        );

        broadcastGameUpdate(
                "ROUND_PRESENTATION_STARTED"
        );

        broadcastPlayerUpdate(
                "ROUND_PRESENTATION_STARTED"
        );

        presentationFuture =
                scheduler.schedule(
                        this::finishPresentationToBoard,
                        duration,
                        TimeUnit.MILLISECONDS
                );
    }

    private synchronized void finishPresentationToBoard() {

        cancelPresentationTimer();

        clearPresentation();

        game.setState(
                GameState.BOARD
        );

        broadcastGameUpdate(
                "PRESENTATION_FINISHED"
        );

        broadcastPlayerUpdate(
                "PRESENTATION_FINISHED"
        );
    }

    private void loadCurrentRoundCategories() {

        Pack pack =
                game.getPack();

        if (pack == null) {

            throw new IllegalArgumentException(
                    "Pack cannot be null"
            );
        }

        int roundIndex =
                game.getCurrentRound();

        if (
                roundIndex < 0 ||
                        roundIndex >=
                                pack
                                        .getRounds()
                                        .size()
        ) {

            throw new IllegalArgumentException(
                    "Round index out of bounds"
            );
        }

        Round round =
                pack
                        .getRounds()
                        .get(roundIndex);

        game.getCategories().clear();

        game.getCategories().addAll(
                round.getCategories()
        );
    }

    private boolean currentRoundFinished() {

        return game
                .getCategories()
                .stream()
                .allMatch(
                        category ->
                                category
                                        .getQuestions()
                                        .stream()
                                        .allMatch(
                                                Question::isUsed
                                        )
                );
    }

    private boolean hasNextRound() {

        Pack pack =
                game.getPack();

        return pack != null &&
                game.getCurrentRound() + 1 <
                        pack
                                .getRounds()
                                .size();
    }

    private synchronized void startNextRound() {

        if (
                !hasNextRound()
        ) {

            cancelPresentationTimer();
            cancelAnswerRevealTimer();
            resetAnswerTimer();
            clearPresentation();

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

        game.setCurrentRound(
                game.getCurrentRound() + 1
        );

        loadCurrentRoundCategories();

        game.setCurrentQuestion(
                null
        );

        game.setBuzzedPlayer(
                null
        );

        startRoundPresentation();
    }

    public synchronized Game nextRoundForTest() {

        if (
                !hasNextRound()
        ) {

            throw new IllegalStateException(
                    "There is no next round"
            );
        }

        cancelPresentationTimer();
        cancelAnswerRevealTimer();
        resetAnswerTimer();

        game.setCurrentRound(
                game.getCurrentRound() + 1
        );

        loadCurrentRoundCategories();

        game.setCurrentQuestion(null);
        game.setBuzzedPlayer(null);

        startRoundPresentation();

        return game;
    }

    public GameSnapshot getSnapshot() {

        return new GameSnapshot(
                game.getState(),
                game.getHost(),
                game.getTurnPlayer(),
                game.getPlayers().size()
        );
    }

    public PlayerSnapshot getPlayerSnapshot(
            String playerId
    ) {

        Player player =
                getPlayerById(
                        playerId
                );

        if (player == null) {

            return null;
        }

        boolean canBuzz =
                game.getState() ==
                        GameState.ANSWERING
                        &&
                        game.getBuzzedPlayer()
                                == null;

        return createPlayerSnapshot(
                player,
                "SNAPSHOT",
                canBuzz
        );
    }

    public void openTestQuestion() {

        Question question =
                new Question(
                        "Столица Франции?",
                        "Париж",
                        100
                );

        game.setCurrentQuestion(
                question
        );

        game.setBuzzedPlayer(
                null
        );

        resetAnswerTimer();

        game.setState(
                GameState.QUESTION
        );

        broadcastGameUpdate(
                "GAME_UPDATED"
        );

        broadcastPlayerUpdate(
                "GAME_UPDATED"
        );
    }

    public synchronized void startAnswering() {

        if (
                game.getCurrentQuestion()
                        == null
        ) {

            throw new IllegalStateException(
                    "No question is currently open"
            );
        }

        cancelAnswerRevealTimer();

        game.setAnsweringRemainingMillis(
                ANSWERING_DURATION
        );

        game.setAnsweringDeadline(
                System.currentTimeMillis() +
                        ANSWERING_DURATION
        );

        game.setState(
                GameState.ANSWERING
        );

        broadcastGameUpdate(
                "ANSWERING_STARTED"
        );

        broadcastPlayerUpdate(
                "ANSWERING_STARTED"
        );

        presentationFuture =
                scheduler.schedule(
                        this::finishAnsweringByTimeout,
                        ANSWERING_DURATION,
                        TimeUnit.MILLISECONDS
                );
    }

    private synchronized void finishAnsweringByTimeout() {

        if (
                game.getState() !=
                        GameState.ANSWERING
        ) {

            return;
        }

        game.setAnsweringRemainingMillis(0);
        game.setAnsweringDeadline(0);
        game.setBuzzedPlayer(null);

        cancelPresentationTimer();
        cancelAnswerRevealTimer();

        game.setState(
                GameState.ANSWER_REVEAL
        );

        broadcastGameUpdate(
                "ANSWER_REVEAL_STARTED"
        );

        broadcastPlayerUpdate(
                "ANSWER_REVEAL_STARTED"
        );

        answerRevealFuture =
                scheduler.schedule(
                        this::finishAnswerReveal,
                        ANSWER_REVEAL_DURATION,
                        TimeUnit.MILLISECONDS
                );
    }

    public synchronized Player buzz(
            String playerId
    ) {

        if (
                game.getState() !=
                        GameState.ANSWERING
        ) {

            return null;
        }

        if (
                game.getBuzzedPlayer()
                        != null
        ) {

            return game.getBuzzedPlayer();
        }

        Player player =
                getPlayerById(
                        playerId
                );

        if (player == null) {

            return null;
        }

        game.setBuzzedPlayer(
                player
        );

        if (
                game.getAnsweringDeadline() > 0
        ) {

            long remaining =
                    Math.max(
                            0,
                            game.getAnsweringDeadline() -
                                    System.currentTimeMillis()
                    );

            game.setAnsweringRemainingMillis(
                    remaining
            );

            game.setAnsweringDeadline(0);

            cancelPresentationTimer();
        }

        broadcastGameUpdate(
                "PLAYER_BUZZED"
        );

        broadcastPlayerUpdate(
                "PLAYER_BUZZED"
        );

        return player;
    }

    public synchronized Player answerCorrectly() {

        Player player =
                game.getBuzzedPlayer();

        if (player == null) {

            if (
                    game.getState() ==
                            GameState.ANSWER_REVEAL
            ) {

                return null;
            }

            throw new IllegalStateException(
                    "No player is answering"
            );
        }

        Question question =
                game.getCurrentQuestion();

        if (question == null) {

            throw new IllegalStateException(
                    "No question is currently open"
            );
        }

        player.setScore(
                player.getScore()
                        + question.getPrice()
        );

        game.setTurnPlayer(
                player
        );

        game.setBuzzedPlayer(
                null
        );

        game.setAnsweringRemainingMillis(0);
        game.setAnsweringDeadline(0);

        cancelPresentationTimer();
        cancelAnswerRevealTimer();

        game.setState(
                GameState.ANSWER_REVEAL
        );

        broadcastGameUpdate(
                "ANSWER_REVEAL_STARTED"
        );

        broadcastPlayerUpdate(
                "ANSWER_REVEAL_STARTED"
        );

        answerRevealFuture =
                scheduler.schedule(
                        this::finishAnswerReveal,
                        ANSWER_REVEAL_DURATION,
                        TimeUnit.MILLISECONDS
                );

        return player;
    }

    private synchronized void finishAnswerReveal() {

        if (
                game.getState() !=
                        GameState.ANSWER_REVEAL
        ) {

            return;
        }

        cancelAnswerRevealTimer();

        game.setCurrentQuestion(null);

        if (
                currentRoundFinished()
        ) {

            startNextRound();

            return;
        }

        game.setState(
                GameState.BOARD
        );

        broadcastGameUpdate(
                "ANSWER_REVEAL_FINISHED"
        );

        broadcastPlayerUpdate(
                "ANSWER_REVEAL_FINISHED"
        );
    }

    public synchronized Player answerIncorrectly() {

        Player player =
                game.getBuzzedPlayer();

        if (player == null) {

            throw new IllegalStateException(
                    "No player is answering"
            );
        }

        game.setBuzzedPlayer(
                null
        );

        long remaining =
                game.getAnsweringRemainingMillis();

        if (
                remaining <= 0
        ) {

            game.setAnsweringRemainingMillis(
                    ANSWERING_DURATION
            );

            remaining =
                    ANSWERING_DURATION;
        }

        game.setAnsweringDeadline(
                System.currentTimeMillis() +
                        remaining
        );

        game.setState(
                GameState.ANSWERING
        );

        cancelPresentationTimer();

        presentationFuture =
                scheduler.schedule(
                        this::finishAnsweringByTimeout,
                        remaining,
                        TimeUnit.MILLISECONDS
                );

        broadcastGameUpdate(
                "ANSWER_INCORRECT"
        );

        broadcastPlayerUpdate(
                "ANSWER_INCORRECT"
        );

        return player;
    }

    public synchronized Question selectQuestion(
            String categoryName,
            int price
    ) {

        if (
                game.getState() !=
                        GameState.BOARD
        ) {

            throw new IllegalStateException(
                    "Question cannot be selected now"
            );
        }

        Category category =
                game
                        .getCategories()
                        .stream()
                        .filter(
                                c ->
                                        c.getName()
                                                .equals(
                                                        categoryName
                                                )
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Category not found"
                                        )
                        );

        Question question =
                category
                        .getQuestions()
                        .stream()
                        .filter(
                                q ->
                                        q.getPrice()
                                                == price
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Question not found"
                                        )
                        );

        if (
                question.isUsed()
        ) {

            throw new IllegalStateException(
                    "Question has already been used"
            );
        }

        question.setUsed(
                true
        );

        game.setCurrentQuestion(
                question
        );

        game.setBuzzedPlayer(
                null
        );

        game.setAnsweringRemainingMillis(0);
        game.setAnsweringDeadline(0);
        cancelPresentationTimer();

        game.setState(
                GameState.QUESTION
        );

        broadcastGameUpdate(
                "QUESTION_SELECTED"
        );

        broadcastPlayerUpdate(
                "QUESTION_SELECTED"
        );

        return question;
    }

    private PlayerSnapshot createPlayerSnapshot(
            Player player,
            String eventType,
            boolean canBuzz
    ) {

        return new PlayerSnapshot(
                player,
                game.getState(),
                canBuzz,
                game.getBuzzedPlayer(),
                game.getCurrentQuestion(),
                eventType,
                game.getAnsweringRemainingMillis(),
                game.getAnsweringDeadline()
        );
    }

    private synchronized void cancelAnswerRevealTimer() {

        if (
                answerRevealFuture != null
        ) {

            answerRevealFuture.cancel(
                    false
            );

            answerRevealFuture = null;
        }
    }

    private void resetAnswerTimer() {

        game.setAnsweringRemainingMillis(
                ANSWERING_DURATION
        );

        game.setAnsweringDeadline(0);
    }

    private synchronized void cancelPresentationTimer() {

        if (
                presentationFuture != null
        ) {

            presentationFuture.cancel(
                    false
            );

            presentationFuture =
                    null;
        }
    }

    private void clearPresentation() {

        game.setPresentationType(
                null
        );

        game.setPresentationStartedAt(
                0
        );

        game.setPresentationDuration(
                0
        );
    }

    private void broadcastGameUpdate(
            String type
    ) {

        GameEvent event =
                new GameEvent(
                        type,
                        game
                );

        messagingTemplate.convertAndSend(
                "/topic/host",
                event
        );
    }

    private void broadcastPlayerUpdate(
            String type
    ) {

        for (
                Player player :
                game.getPlayers()
        ) {

            boolean canBuzz =
                    game.getState() ==
                            GameState.ANSWERING
                            &&
                            game.getBuzzedPlayer()
                                    == null;

            PlayerSnapshot snapshot =
                    createPlayerSnapshot(
                            player,
                            type,
                            canBuzz
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
