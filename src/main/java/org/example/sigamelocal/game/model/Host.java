package org.example.sigamelocal.game.model;

public class Host {

    private final String id;
    private final String name;

    public Host(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
