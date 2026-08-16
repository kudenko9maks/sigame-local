package org.example.sigamelocal.game.model;

public class Question {

    private final String text;
    private final String answer;
    private final int price;

    private boolean used;

    public Question(
            String text,
            String answer,
            int price
    ) {
        this.text = text;
        this.answer = answer;
        this.price = price;
        this.used = false;
    }

    public String getText() {
        return text;
    }

    public String getAnswer() {
        return answer;
    }

    public int getPrice() {
        return price;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}