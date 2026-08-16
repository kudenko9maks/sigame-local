package org.example.sigamelocal.game.model;

public class Player {
    private final String id;
    private final String name;
    private int score;

    public Player (String id, String name) {
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

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void substructScore (int points) {
        this.score -= points;
    }
}
