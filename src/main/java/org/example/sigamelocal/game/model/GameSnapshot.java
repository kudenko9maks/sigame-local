package org.example.sigamelocal.game.model;

public class GameSnapshot {

    private final GameState state;
    private final Host host;
    private final int playerCount;

    public GameSnapshot(GameState state, Host host, int playerCount) {
        this.state = state;
        this.host = host;
        this.playerCount = playerCount;
    }

    public GameState getState() {
        return state;
    }

    public Host getHost() {
        return host;
    }

    public int getPlayerCount() {
        return playerCount;
    }
}
