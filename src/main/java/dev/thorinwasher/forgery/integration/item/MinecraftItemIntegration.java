package dev.thorinwasher.forgery.integration.item;

import dev.thorinwasher.forgery.integration.ItemIntegration;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.ForgeryKey;
import dev.thorinwasher.forgery.util.PdcKeys;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.stream.Stream;

public class MinecraftItemIntegration implements ItemIntegration {
    @Override
    public Optional<ForgingMaterialResult> toForgery(ItemStack itemStack) {
        Integer score = itemStack.getPersistentDataContainer().get(PdcKeys.SCORE, PersistentDataType.INTEGER);
        return Optional.of(new ForgingMaterial(ForgeryKey.fromAdventure(itemStack.getType().key()), score == null ? 10 : score))
                .map(this::toResult);
    }

    @Override
    public Optional<ItemStack> toBukkit(ForgingMaterial material) {
        return material.key().toAdventure()
                .flatMap(key -> Optional.ofNullable(Registry.MATERIAL.get(key)))
                .map(ItemStack::new)
                .map(itemStack -> {
                    if (material.score() != 10) {
                        itemStack.editPersistentDataContainer(pdc -> pdc.set(PdcKeys.SCORE, PersistentDataType.INTEGER, material.score()));
                    }
                    return itemStack;
                });
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
