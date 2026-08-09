package org.example.sigamelocal.game.model;

public class Player {
    private final String name;
    private final String id;
    private int score;

    public Player (String name, String id) {
        this.name = name;
        this.id = id;
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void substructScore (int points) {
        this.score -= points;
    }
}
