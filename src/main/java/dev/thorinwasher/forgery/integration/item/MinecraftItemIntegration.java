package dev.thorinwasher.forgery.integration.item;

import dev.thorinwasher.forgery.integration.ItemIntegration;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

public class MinecraftItemIntegration implements ItemIntegration {
    @Override
    public Optional<ForgingMaterialResult> toForgery(ItemStack itemStack) {
        return Optional.of(new ForgingMaterial(ForgeryKey.fromAdventure(itemStack.getType().key())))
                .map(this::toResult);
    }

    @Override
    public Optional<ItemStack> toBukkit(ForgingMaterial material) {
        return material.key().toAdventure()
                .flatMap(key -> Optional.ofNullable(Registry.MATERIAL.get(key)))
                .map(ItemStack::new);
    }

    @Override
    public Stream<ForgingMaterialResult> fromString(String key) {
        return Stream.of(new ForgingMaterial(new ForgeryKey(id(), key)))
                .map(this::toResult);
    }

    @Override
    public String id() {
        return "minecraft";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void initialize() {
        // NO-OP
    }

    private ForgingMaterialResult toResult(ForgingMaterial forgingMaterial) {
        return new ForgingMaterialResult(forgingMaterial, Priority.LOW);
    }
}
