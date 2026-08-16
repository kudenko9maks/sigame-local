package org.example.sigamelocal.game.model;

public class PlayerSnapshot {

    private final Player player;
    private final GameState state;
    private final boolean canBuzz;
    private final Player buzzedPlayer;

    public PlayerSnapshot(
            Player player,
            GameState state,
            boolean canBuzz,
            Player buzzedPlayer
    ) {
        this.player = player;
        this.state = state;
        this.canBuzz = canBuzz;
        this.buzzedPlayer = buzzedPlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public GameState getState() {
        return state;
    }

    public boolean isCanBuzz() {
        return canBuzz;
    }

    public Player getBuzzedPlayer() {
        return buzzedPlayer;
    }
}