package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RecipeResult(ItemWriter itemWriter, int amount, boolean overrideLore, List<Component> lore,
                           @Nullable Component name) {

    public interface ItemWriter {

        default ItemStack write(IntegrationRegistry registry) {
            return write(registry, 10);
        }

        ItemStack write(IntegrationRegistry registry, int score);
    }
}
