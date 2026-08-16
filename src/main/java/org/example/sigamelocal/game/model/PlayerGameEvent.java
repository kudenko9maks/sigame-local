package org.example.sigamelocal.game.model;

public class PlayerGameEvent {

    private final String type;
    private final PlayerSnapshot snapshot;

    public PlayerGameEvent(
            String type,
            PlayerSnapshot snapshot
    ) {
        this.type = type;
        this.snapshot = snapshot;
    }

    public String getType() {
        return type;
    }

    public PlayerSnapshot getSnapshot() {
        return snapshot;
    }
}
