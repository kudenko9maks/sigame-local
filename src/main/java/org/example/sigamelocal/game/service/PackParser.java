package org.example.sigamelocal.game.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.example.sigamelocal.game.model.Category;
import org.example.sigamelocal.game.model.Pack;
import org.example.sigamelocal.game.model.Question;
import org.example.sigamelocal.game.model.Round;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PackParser {
    private final ObjectMapper objectMapper;

    public PackParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Pack parse(InputStream inputStream) throws IOException {

        JsonNode root = objectMapper.readTree(inputStream);

        JsonNode nameNode = root.get("name");
        if (nameNode == null || !nameNode.isTextual() || nameNode.asText().isBlank()) {
            throw new IllegalArgumentException("Pack must have a name");
        }

        String packName = nameNode.asText();

        JsonNode categoriesNode = root.get("categories");

        if (categoriesNode == null ||
                !categoriesNode.isArray() ||
                categoriesNode.isEmpty()) {
            throw new IllegalArgumentException("Pack must have a categories");
        }

        List<Category> categories = new ArrayList<>();

        for (JsonNode categoryNode : categoriesNode) {
            categories.add(parseCategory(categoryNode));
        }

        int totalCategories = categories.size();

        int firstRoundSize = (totalCategories + 1) / 2;

        List<Category> firstRoundCategories = new ArrayList<>(categories.subList(0, firstRoundSize));
        List<Category> secondRoundCategories = new ArrayList<>(categories.subList(firstRoundSize, totalCategories));

        firstRoundCategories = applyPriceMultiplier(firstRoundCategories, 1);
        secondRoundCategories = applyPriceMultiplier(secondRoundCategories, 2);

        List<Round> rounds = new ArrayList<>();

        rounds.add(new Round("Раунд 1", firstRoundCategories));

        if (!secondRoundCategories.isEmpty()) {
            rounds.add(new Round("Раунд 2", secondRoundCategories));
        }
        return new Pack(packName, rounds);
    }

    private Category parseCategory(JsonNode categoryNode) {
        JsonNode nameNode = categoryNode.get("name");

        if (
                nameNode == null ||
                        !nameNode.isTextual() ||
                        nameNode.asText().isBlank()
        ) {throw new IllegalArgumentException(
                    "Category must have a name");
        }

        String name = nameNode.asText();

        JsonNode questionsNode = categoryNode.get("questions");

        if (
                questionsNode == null ||
                        !questionsNode.isArray() ||
                        questionsNode.isEmpty()
        ) {throw new IllegalArgumentException(
                    "Category '" + name + "' must contain questions");
        }

        List<Question> questions = new ArrayList<>();

        for (JsonNode questionNode : questionsNode) {
            questions.add(parseQuestion(questionNode));
        }

        return new Category(name, questions);
    }

    private Question parseQuestion(JsonNode questionNode) {
        JsonNode priceNode = questionNode.get("price");
        JsonNode textNode = questionNode.get("text");
        JsonNode answerNode = questionNode.get("answer");
        if (
                priceNode == null ||
                        !priceNode.isInt()
        ) {

            throw new IllegalArgumentException(
                    "Question must have integer price"
            );
        }


        if (
                textNode == null ||
                        !textNode.isTextual() ||
                        textNode.asText().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Question must have text"
            );
        }


        if (
                answerNode == null ||
                        !answerNode.isTextual() ||
                        answerNode.asText().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Question must have answer"
            );
        }

        int price = priceNode.asInt();
        String text = textNode.asText();
        String answer = answerNode.asText();

        if (price <= 0) {
            throw new IllegalArgumentException("Question must have positive price");
        }

        return new Question(text, answer, price);
    }

    private List<Category> applyPriceMultiplier(
            List<Category> categories,
            int roundNumber
    ) {

        int multiplier =
                (int) Math.pow(
                        2,
                        roundNumber - 1
                );


        List<Category> result =
                new ArrayList<>();


        for (
                Category category :
                categories
        ) {

            List<Question> questions =
                    new ArrayList<>();


            for (
                    Question question :
                    category.getQuestions()
            ) {

                int newPrice =
                        question.getPrice()
                                * multiplier;


                Question newQuestion =
                        new Question(
                                question.getText(),
                                question.getAnswer(),
                                newPrice
                        );


                questions.add(
                        newQuestion
                );
            }


            result.add(
                    new Category(
                            category.getName(),
                            questions
                    )
            );
        }


        return result;
    }
}
