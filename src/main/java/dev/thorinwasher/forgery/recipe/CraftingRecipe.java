package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CraftingRecipe(String shape, Map<Character, ForgeryKey> materials, RecipeResult result) {

    public ShapedRecipe compile(ItemAdapter itemAdapter, String key) {
        ItemStack result = result().get(10, itemAdapter.registry());
        ShapedRecipe recipe = new ShapedRecipe(Forgery.key(key), result);
        recipe.shape(shape().split("\n"));
        for (Map.Entry<Character, ForgeryKey> entry : materials.entrySet()) {
            recipe.setIngredient(entry.getKey(), compileExact(entry.getValue(), itemAdapter));
        }
        return recipe;
    }

    private RecipeChoice.ExactChoice compileExact(ForgeryKey key, ItemAdapter itemAdapter) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int score = 1; score <= 10; score++) {
            itemStacks.add(itemAdapter.toBukkit(new ForgingItem(
                    new ForgingMaterial(key, score),
                    new ForgingSteps(List.of()))
            ));
        }
        return new RecipeChoice.ExactChoice(itemStacks);
    }
}
