package org.example.sigamelocal.game.model;

public class PlayerEvent {
    private String type;
    private Player player;

    public PlayerEvent(String type, Player player) {
        this.type = type;
        this.player = player;
    }

    public String getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }
}
