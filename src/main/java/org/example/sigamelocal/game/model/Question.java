package org.example.sigamelocal.game.model;

public class Question {
    private final String text;
    private final String answer;
    private final int price;

    public Question(String text, String answer, int price) {
        this.text = text;
        this.answer = answer;
        this.price = price;
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
}
