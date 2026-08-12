package org.example.sigamelocal.game.model;

public class PlayerSnapshot {
    private final Player player;
    private final GameSnapshot gameSnapshot;

    public PlayerSnapshot(Player player, GameSnapshot gameSnapshot) {
        this.player = player;
        this.gameSnapshot = gameSnapshot;
    }

    public Player getPlayer() {
        return player;
    }

    public GameSnapshot getGameSnapshot() {
        return gameSnapshot;
    }
}
