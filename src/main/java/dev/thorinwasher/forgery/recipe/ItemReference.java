package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.inventory.ForgingMaterialPersistentDataType;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemReference implements Keyed {
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

    public ItemStack write(int score) {
        return writeMaterial(itemStack(), new ForgingMaterial(ForgeryKey.fromAdventure(key), score));
    }

    private ItemStack writeMaterial(ItemStack itemStack, ForgingMaterial material) {
        itemStack.editPersistentDataContainer(pdc -> {
            pdc.set(ItemAdapter.FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE, material);
        });
        return itemStack;
    }
}
