package org.example.sigamelocal.game.model;

public class Question {

    private final String type;
    private final String content;
    private final QuestionContent answer;
    private final int price;

    private boolean used;

    public Question(
            String text,
            String answer,
            int price
    ) {
        this(
                "text",
                text,
                new QuestionContent(
                        "text",
                        answer
                ),
                price
        );
    }

    public Question(
            String type,
            String content,
            String answer,
            int price
    ) {
        this(
                type,
                content,
                new QuestionContent(
                        "text",
                        answer
                ),
                price
        );
    }

    public Question(
            String type,
            String content,
            QuestionContent answer,
            int price
    ) {
        this.type = type;
        this.content = content;
        this.answer = answer;
        this.price = price;
        this.used = false;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getText() {
        return content;
    }

    public QuestionContent getAnswer() {
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
