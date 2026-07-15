package com.zenith.client.flipping.craft;

import com.zenith.client.api.cache.RecipeCache;

/**
 * Validates that a recipe is craftable with current inventory contents (stub).
 * Full item-matching is added in Phase 12 when InventoryGUIScraper can count
 * held ingredients.
 */
public final class RecipeValidator {

    private RecipeValidator() {}

    public static boolean canCraft(String outputId) {
        var recipes = RecipeCache.getInstance().recipesFor(outputId);
        return !recipes.isEmpty();
    }
}
