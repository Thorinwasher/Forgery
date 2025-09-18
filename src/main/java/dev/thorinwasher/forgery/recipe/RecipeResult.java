package dev.thorinwasher.forgery.recipe;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RecipeResult(ItemWriter itemWriter, int amount, boolean overrideLore, List<Component> lore,
                           @Nullable Component name) {

    public interface ItemWriter {

        ItemStack get();
    }
}
