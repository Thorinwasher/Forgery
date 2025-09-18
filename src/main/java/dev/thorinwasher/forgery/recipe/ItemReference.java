package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.Forgery;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemReference implements Keyed, RecipeResult.ItemWriter {
    private final NamespacedKey key;
    private final ItemStack itemStack;

    public ItemReference(String name, ItemStack itemStack) {
        this.key = Forgery.key(name);
        this.itemStack = itemStack;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public ItemStack itemStack() {
        return itemStack.clone();
    }

    @Override
    public ItemStack get() {
        return itemStack();
    }
}
