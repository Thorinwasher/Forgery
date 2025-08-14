package dev.thorinwasher.forgery.integration;

import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

public interface ItemIntegration {

    Optional<ForgingMaterialResult> toForgery(ItemStack itemStack);

    Optional<ItemStack> toBukkit(ForgingMaterial material);

    Stream<ForgingMaterialResult> fromString(String key);

    String id();

    boolean isEnabled();

    void initialize();

    enum Priority {
        HIGH,
        NORMAL,
        LOW
    }

    record ForgingMaterialResult(ForgingMaterial material, Priority priority) {

    }
}
