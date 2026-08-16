package org.example.sigamelocal.game.model;

import java.util.List;

public class Category {

    private final String name;
    private final List<Question> questions;

    public Category(String name, List<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
