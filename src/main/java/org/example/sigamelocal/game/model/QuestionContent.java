package org.example.sigamelocal.game.model;

public class QuestionContent {

    private final String type;
    private final String content;

    public QuestionContent(
            String type,
            String content
    ) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Content type cannot be blank");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be blank");
        }

        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
