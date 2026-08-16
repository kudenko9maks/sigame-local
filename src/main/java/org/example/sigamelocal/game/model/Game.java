package org.example.sigamelocal.game.model;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private GameState state;
    private Host host;
    private final List<Player> players;
    private Question currentQuestion;
    private Player buzzedPlayer;

    public Game() {
        this.state = GameState.WAITING;
        this.players = new ArrayList<>();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public Player getBuzzedPlayer() {
        return buzzedPlayer;
    }

    public void setBuzzedPlayer(Player buzzedPlayer) {
        this.buzzedPlayer = buzzedPlayer;
    }
}