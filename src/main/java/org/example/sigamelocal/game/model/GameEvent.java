package org.example.sigamelocal.game.model;

public class GameEvent {

    private final String type;
    private final Game game;

    public GameEvent(String type, Game game) {
        this.type = type;
        this.game = game;
    }

    public String getType() {
        return this.type;
    }

    public Game getGame() {
        return this.game;
    }
}
