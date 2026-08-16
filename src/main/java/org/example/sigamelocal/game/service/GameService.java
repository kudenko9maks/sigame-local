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
        createTestPack();
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

        if (game.getPlayers().isEmpty()) {
            throw new IllegalStateException(
                    "There are no players"
            );
        }

        Player firstPlayer =
                game.getPlayers().get(0);

        game.setTurnPlayer(firstPlayer);

        game.setState(GameState.BOARD);

        broadcastGameUpdate("GAME_STARTED");
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

        player.setScore(
                player.getScore()
                        + question.getPrice()
        );

        game.setTurnPlayer(player);
        game.setBuzzedPlayer(null);
        game.setCurrentQuestion(null);
        game.setState(GameState.BOARD);

        broadcastGameUpdate("ANSWER_CORRECT");

        return player;
    }

    public Player answerIncorrectly() {

        Player player = game.getBuzzedPlayer();

        if (player == null) {
            throw new IllegalStateException(
                    "No player is answering"
            );
        }

        game.setBuzzedPlayer(null);

        game.setState(GameState.ANSWERING);

        broadcastGameUpdate("ANSWER_INCORRECT");

        return player;
    }

    private void createTestPack() {

        Category history = new Category(
                "История",
                java.util.List.of(
                        new Question(
                                "В каком году началась Вторая мировая война?",
                                "1939",
                                100
                        ),
                        new Question(
                                "Кто был первым императором Рима?",
                                "Октавиан Август",
                                200
                        ),
                        new Question(
                                "В каком году произошло Крещение Руси?",
                                "988",
                                300
                        ),
                        new Question(
                                "Кто основал Санкт-Петербург?",
                                "Пётр I",
                                400
                        ),
                        new Question(
                                "В каком году пала Римская империя?",
                                "476",
                                500
                        )
                )
        );


        Category geography = new Category(
                "География",
                java.util.List.of(
                        new Question(
                                "Столица Франции?",
                                "Париж",
                                100
                        ),
                        new Question(
                                "Самая большая страна мира?",
                                "Россия",
                                200
                        ),
                        new Question(
                                "Какая река самая длинная в мире?",
                                "Нил",
                                300
                        ),
                        new Question(
                                "Столица Австралии?",
                                "Канберра",
                                400
                        ),
                        new Question(
                                "Самая высокая гора мира?",
                                "Эверест",
                                500
                        )
                )
        );


        Category cinema = new Category(
                "Кино",
                java.util.List.of(
                        new Question(
                                "Кто сыграл Джека в «Титанике»?",
                                "Леонардо ДиКаприо",
                                100
                        ),
                        new Question(
                                "Как называется школа Гарри Поттера?",
                                "Хогвартс",
                                200
                        ),
                        new Question(
                                "Кто режиссёр фильма «Аватар»?",
                                "Джеймс Кэмерон",
                                300
                        ),
                        new Question(
                                "Как называется вымышленная страна Чёрной Пантеры?",
                                "Ваканда",
                                400
                        ),
                        new Question(
                                "Какой фильм получил «Оскар» за лучший фильм в 1998 году?",
                                "Титаник",
                                500
                        )
                )
        );


        game.getCategories().add(history);
        game.getCategories().add(geography);
        game.getCategories().add(cinema);
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

        return question;
    }
}
