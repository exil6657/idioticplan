package com.zenith.client.api.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of NEU crafting / trade / NPC-shop recipes, populated by
 * {@link com.zenith.client.api.neu.NEURecipeFetcher}.
 */
public final class RecipeCache {

    public static final class Ingredient {
        private final String skyblockId;
        private final int count;
        public Ingredient(String skyblockId, int count) {
            this.skyblockId = skyblockId;
            this.count = Math.max(1, count);
        }
        public String skyblockId() { return skyblockId; }
        public int count()       { return count; }
    }

    public static final class Recipe {
        private final String outputId;
        private final int outputCount;
        private final List<Ingredient> ingredients;
        private final String type;
        public Recipe(String outputId, int outputCount, List<Ingredient> ingredients, String type) {
            this.outputId = outputId;
            this.outputCount = Math.max(1, outputCount);
            this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
            this.type = type == null ? "crafting" : type;
        }
        public String outputId()         { return outputId; }
        public int outputCount()         { return outputCount; }
        public List<Ingredient> ingredients() { return ingredients; }
        public String type()             { return type; }
    }

    private static final RecipeCache INSTANCE = new RecipeCache();
    public static RecipeCache getInstance() { return INSTANCE; }

    private final Map<String, List<Recipe>> byOutput = new ConcurrentHashMap<>();

    private RecipeCache() {}

    public void add(Recipe r) {
        if (r == null || r.outputId() == null) return;
        byOutput.computeIfAbsent(r.outputId().toUpperCase(), k -> new ArrayList<>()).add(r);
    }

    public List<Recipe> recipesFor(String outputId) {
        if (outputId == null) return List.of();
        List<Recipe> list = byOutput.get(outputId.toUpperCase());
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    public int size() {
        int n = 0;
        for (var v : byOutput.values()) n += v.size();
        return n;
    }

    public int outputCount() { return byOutput.size(); }
    public void clear() { byOutput.clear(); }
}
