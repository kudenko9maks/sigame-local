package org.example.sigamelocal.game.model;

import java.util.List;

public class Pack {

    private final String name;

    private final List<Round> rounds;


    public Pack(
            String name,
            List<Round> rounds
    ) {

        this.name = name;
        this.rounds = rounds;
    }


    public String getName() {

        return name;
    }


    public List<Round> getRounds() {

        return rounds;
    }
}