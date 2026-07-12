package com.zenith.client.api.neu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.RecipeCache;
import com.zenith.client.api.cache.RecipeCache.Ingredient;
import com.zenith.client.api.cache.RecipeCache.Recipe;
import com.zenith.client.core.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Downloads NEU recipes.json and populates the in-memory RecipeCache. */
public final class NEURecipeFetcher {

    private static final NEURecipeFetcher INSTANCE = new NEURecipeFetcher();
    public static NEURecipeFetcher getInstance() { return INSTANCE; }

    private volatile boolean fetched = false;
    public boolean isFetched() { return fetched; }

    private NEURecipeFetcher() {}

    public void fetchAsync() { ThreadUtils.runAsync(this::fetchNow); }

    public void fetchNow() {
        try {
            JsonElement el = NEURepoClient.getInstance().fetchRecipes().join();
            if (el == null || !el.isJsonObject()) return;
            RecipeCache.getInstance().clear();
            JsonObject all = el.getAsJsonObject();
            int count = 0;
            for (Map.Entry<String, JsonElement> entry : all.entrySet()) {
                String outputId = entry.getKey();
                JsonArray arr = entry.getValue().isJsonArray() ? entry.getValue().getAsJsonArray() : null;
                if (arr == null) continue;
                for (JsonElement re : arr) {
                    if (!re.isJsonObject()) continue;
                    Recipe r = parseRecipe(outputId, re.getAsJsonObject());
                    if (r != null) { RecipeCache.getInstance().add(r); count++; }
                }
            }
            fetched = true;
            ZenithClient.LOGGER.info("[NEU.recipes] Loaded {} recipes ({} outputs).", count, all.size());
        } catch (Exception e) {
            ZenithClient.LOGGER.warn("[NEU.recipes] failed", e);
        }
    }

    private Recipe parseRecipe(String outputId, JsonObject o) {
        String type = o.has("type") ? o.get("type").getAsString() : "crafting";
        int count = o.has("count") ? o.get("count").getAsInt() : 1;
        List<Ingredient> ingredients = new ArrayList<>();
        JsonArray arr = o.has("ingredients") ? o.getAsJsonArray("ingredients") : null;
        if (arr != null) {
            for (JsonElement ie : arr) {
                if (!ie.isJsonObject()) continue;
                JsonObject io = ie.getAsJsonObject();
                String id = io.has("item_id") ? io.get("item_id").getAsString() : null;
                if (id == null) continue;
                int ic = io.has("count") ? io.get("count").getAsInt() : 1;
                ingredients.add(new Ingredient(id, ic));
            }
        }
        if (ingredients.isEmpty() && !"trade".equals(type) && !"npc_shop".equals(type)) return null;
        return new Recipe(outputId, count, List.copyOf(ingredients), type);
    }
}
