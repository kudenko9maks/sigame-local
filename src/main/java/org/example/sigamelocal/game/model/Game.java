package org.example.sigamelocal.game.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private GameState state;
    private final List<Player> players;

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

    public List<Player> getPlayers() {
        return players;
    }
}
