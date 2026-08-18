package org.example.sigamelocal.game.model;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private GameState state;

    private Host host;

    private final List<Player> players;

    private Question currentQuestion;

    private Player buzzedPlayer;

    private Player turnPlayer;

    private final List<Category> categories;

    private Pack pack;

    private int currentRound;

    /*
     * PRESENTATION
     */

    private String presentationType;

    private long presentationStartedAt;

    private long presentationDuration;

    /*
     * ANSWERING TIMER
     *
     * answeringRemainingMillis — сколько времени осталось.
     *
     * answeringDeadline — абсолютное время окончания,
     * когда таймер реально запущен.
     *
     * Если answeringDeadline == 0,
     * таймер сейчас остановлен.
     */

    private long answeringRemainingMillis;

    private long answeringDeadline;


    public Game() {

        this.state = GameState.LOBBY;

        this.players =
                new ArrayList<>();

        this.categories =
                new ArrayList<>();

        this.answeringRemainingMillis =
                15000;

        this.answeringDeadline =
                0;
    }


    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }


    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }


    public List<Player> getPlayers() {
        return players;
    }


    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(
            Question currentQuestion
    ) {
        this.currentQuestion =
                currentQuestion;
    }


    public Player getBuzzedPlayer() {
        return buzzedPlayer;
    }

    public void setBuzzedPlayer(
            Player buzzedPlayer
    ) {
        this.buzzedPlayer =
                buzzedPlayer;
    }


    public Player getTurnPlayer() {
        return turnPlayer;
    }

    public void setTurnPlayer(
            Player turnPlayer
    ) {
        this.turnPlayer =
                turnPlayer;
    }


    public List<Category> getCategories() {
        return categories;
    }


    public Pack getPack() {
        return pack;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
    }


    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(
            int currentRound
    ) {
        this.currentRound =
                currentRound;
    }


    /*
     * =====================================================
     * PRESENTATION
     * =====================================================
     */

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(
            String presentationType
    ) {
        this.presentationType =
                presentationType;
    }


    public long getPresentationStartedAt() {
        return presentationStartedAt;
    }

    public void setPresentationStartedAt(
            long presentationStartedAt
    ) {
        this.presentationStartedAt =
                presentationStartedAt;
    }


    public long getPresentationDuration() {
        return presentationDuration;
    }

    public void setPresentationDuration(
            long presentationDuration
    ) {
        this.presentationDuration =
                presentationDuration;
    }


    /*
     * =====================================================
     * ANSWERING TIMER
     * =====================================================
     */

    public long getAnsweringRemainingMillis() {
        return answeringRemainingMillis;
    }

    public void setAnsweringRemainingMillis(
            long answeringRemainingMillis
    ) {
        this.answeringRemainingMillis =
                answeringRemainingMillis;
    }


    public long getAnsweringDeadline() {
        return answeringDeadline;
    }

    public void setAnsweringDeadline(
            long answeringDeadline
    ) {
        this.answeringDeadline =
                answeringDeadline;
    }
}