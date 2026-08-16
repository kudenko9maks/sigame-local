package org.example.sigamelocal.game.model;

public class GameSnapshot {

    private final GameState state;
    private final Host host;
    private final Player turnPlayer;
    private final int playerCount;

    public GameSnapshot(
            GameState state,
            Host host,
            Player turnPlayer,
            int playerCount
    ) {
        this.state = state;
        this.host = host;
        this.turnPlayer = turnPlayer;
        this.playerCount = playerCount;
    }

    public GameState getState() {
        return state;
    }

    public Host getHost() {
        return host;
    }

    public Player getTurnPlayer() {
        return turnPlayer;
    }

    public int getPlayerCount() {
        return playerCount;
    }
}
