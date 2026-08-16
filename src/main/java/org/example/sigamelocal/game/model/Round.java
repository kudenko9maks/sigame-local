package org.example.sigamelocal.game.model;

import java.util.List;

public class Round {

    private final String name;

    private final List<Category> categories;


    public Round(
            String name,
            List<Category> categories
    ) {

        this.name = name;
        this.categories = categories;
    }


    public String getName() {

        return name;
    }


    public List<Category> getCategories() {

        return categories;
    }
}
