package org.example.sigamelocal.game.model;

public class PlayerSnapshot {

    private final Player player;

    private final GameState state;

    private final boolean canBuzz;

    private final Player buzzedPlayer;

    private final Question currentQuestion;

    private final String eventType;

    private final long answeringRemainingMillis;

    private final long answeringDeadline;


    public PlayerSnapshot(
            Player player,
            GameState state,
            boolean canBuzz,
            Player buzzedPlayer,
            Question currentQuestion,
            String eventType,
            long answeringRemainingMillis,
            long answeringDeadline
    ) {

        this.player = player;

        this.state = state;

        this.canBuzz = canBuzz;

        this.buzzedPlayer =
                buzzedPlayer;

        this.currentQuestion =
                currentQuestion;

        this.eventType =
                eventType;

        this.answeringRemainingMillis =
                answeringRemainingMillis;

        this.answeringDeadline =
                answeringDeadline;
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


    public Question getCurrentQuestion() {
        return currentQuestion;
    }


    public String getEventType() {
        return eventType;
    }


    public long getAnsweringRemainingMillis() {
        return answeringRemainingMillis;
    }


    public long getAnsweringDeadline() {
        return answeringDeadline;
    }
}