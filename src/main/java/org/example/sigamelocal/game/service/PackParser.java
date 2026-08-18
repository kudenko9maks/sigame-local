package org.example.sigamelocal.game.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.example.sigamelocal.game.model.Category;
import org.example.sigamelocal.game.model.Pack;
import org.example.sigamelocal.game.model.Question;
import org.example.sigamelocal.game.model.QuestionContent;
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

        if (
                nameNode == null ||
                        !nameNode.isTextual() ||
                        nameNode.asText().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Pack must have a name"
            );
        }

        JsonNode categoriesNode = root.get("categories");

        if (
                categoriesNode == null ||
                        !categoriesNode.isArray() ||
                        categoriesNode.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Pack must have a categories"
            );
        }

        List<Category> categories =
                new ArrayList<>();

        for (JsonNode categoryNode : categoriesNode) {
            categories.add(
                    parseCategory(categoryNode)
            );
        }

        int totalCategories =
                categories.size();

        int firstRoundSize =
                (totalCategories + 1) / 2;

        List<Category> firstRoundCategories =
                new ArrayList<>(
                        categories.subList(
                                0,
                                firstRoundSize
                        )
                );

        List<Category> secondRoundCategories =
                new ArrayList<>(
                        categories.subList(
                                firstRoundSize,
                                totalCategories
                        )
                );

        firstRoundCategories =
                applyPriceMultiplier(
                        firstRoundCategories,
                        1
                );

        secondRoundCategories =
                applyPriceMultiplier(
                        secondRoundCategories,
                        2
                );

        List<Round> rounds =
                new ArrayList<>();

        rounds.add(
                new Round(
                        "Раунд 1",
                        firstRoundCategories
                )
        );

        if (!secondRoundCategories.isEmpty()) {
            rounds.add(
                    new Round(
                            "Раунд 2",
                            secondRoundCategories
                    )
            );
        }

        return new Pack(
                packName(nameNode),
                rounds
        );
    }

    private String packName(JsonNode nameNode) {
        return nameNode.asText();
    }

    private Category parseCategory(
            JsonNode categoryNode
    ) {

        JsonNode nameNode =
                categoryNode.get("name");

        if (
                nameNode == null ||
                        !nameNode.isTextual() ||
                        nameNode.asText().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Category must have a name"
            );
        }

        String name =
                nameNode.asText();

        JsonNode questionsNode =
                categoryNode.get("questions");

        if (
                questionsNode == null ||
                        !questionsNode.isArray() ||
                        questionsNode.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Category '" +
                            name +
                            "' must contain questions"
            );
        }

        List<Question> questions =
                new ArrayList<>();

        for (JsonNode questionNode : questionsNode) {
            questions.add(
                    parseQuestion(questionNode)
            );
        }

        return new Category(
                name,
                questions
        );
    }

    private Question parseQuestion(
            JsonNode questionNode
    ) {

        JsonNode priceNode =
                questionNode.get("price");

        JsonNode typeNode =
                questionNode.get("type");

        JsonNode contentNode =
                questionNode.get("content");

        JsonNode textNode =
                questionNode.get("text");

        JsonNode answerNode =
                questionNode.get("answer");

        if (
                priceNode == null ||
                        !priceNode.isInt()
        ) {
            throw new IllegalArgumentException(
                    "Question must have integer price"
            );
        }

        String type = "text";
        String content;

        if (typeNode != null) {

            if (
                    !typeNode.isTextual() ||
                            typeNode.asText().isBlank()
            ) {
                throw new IllegalArgumentException(
                        "Question type cannot be blank"
                );
            }

            type =
                    typeNode
                            .asText()
                            .trim()
                            .toLowerCase();

            if (
                    !type.equals("text") &&
                            !type.equals("image")
            ) {
                throw new IllegalArgumentException(
                        "Unsupported question type: " +
                                type
                );
            }
        }

        if (
                contentNode != null &&
                        contentNode.isTextual() &&
                        !contentNode.asText().isBlank()
        ) {
            content =
                    contentNode.asText();

        } else if (
                textNode != null &&
                        textNode.isTextual() &&
                        !textNode.asText().isBlank()
        ) {
            content =
                    textNode.asText();

        } else {
            throw new IllegalArgumentException(
                    "Question must have content"
            );
        }

        QuestionContent answer =
                parseAnswer(answerNode);

        int price =
                priceNode.asInt();

        if (price <= 0) {
            throw new IllegalArgumentException(
                    "Question must have positive price"
            );
        }

        validateContentPath(
                type,
                content,
                "Question"
        );

        return new Question(
                type,
                content,
                answer,
                price
        );
    }

    private QuestionContent parseAnswer(
            JsonNode answerNode
    ) {

        if (
                answerNode == null
        ) {
            throw new IllegalArgumentException(
                    "Question must have answer"
            );
        }

        if (answerNode.isTextual()) {

            String value =
                    answerNode.asText();

            if (value.isBlank()) {
                throw new IllegalArgumentException(
                        "Answer cannot be blank"
                );
            }

            return new QuestionContent(
                    "text",
                    value
            );
        }

        if (!answerNode.isObject()) {
            throw new IllegalArgumentException(
                    "Answer must be text or object"
            );
        }

        JsonNode typeNode =
                answerNode.get("type");

        JsonNode contentNode =
                answerNode.get("content");

        if (
                typeNode == null ||
                        !typeNode.isTextual() ||
                        typeNode.asText().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Answer type cannot be blank"
            );
        }

        if (
                contentNode == null ||
                        !contentNode.isTextual() ||
                        contentNode.asText().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Answer content cannot be blank"
            );
        }

        String type =
                typeNode
                        .asText()
                        .trim()
                        .toLowerCase();

        String content =
                contentNode.asText();

        if (
                !type.equals("text") &&
                        !type.equals("image")
        ) {
            throw new IllegalArgumentException(
                    "Unsupported answer type: " +
                            type
            );
        }

        validateContentPath(
                type,
                content,
                "Answer"
        );

        return new QuestionContent(
                type,
                content
        );
    }

    private void validateContentPath(
            String type,
            String content,
            String label
    ) {

        if (!type.equals("image")) {
            return;
        }

        boolean external =
                content.startsWith("http://") ||
                        content.startsWith("https://");

        if (external) {
            return;
        }

        if (
                content.startsWith("/") ||
                        content.contains("..") ||
                        content.contains("\\")
        ) {
            throw new IllegalArgumentException(
                    label +
                            " image path is invalid"
            );
        }

        if (!content.startsWith("images/")) {
            throw new IllegalArgumentException(
                    label +
                            " image must be inside images/"
            );
        }
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

        for (Category category : categories) {

            List<Question> questions =
                    new ArrayList<>();

            for (Question question :
                    category.getQuestions()) {

                int newPrice =
                        question.getPrice() *
                                multiplier;

                Question newQuestion =
                        new Question(
                                question.getType(),
                                question.getContent(),
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
