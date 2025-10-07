package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.forging.TemperatureData;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.recipe.HeatBehavior;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;

public class ForgingItemSerializer implements TypeSerializer<ForgingItem> {

    @Override
    public ForgingItem deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        ForgingSteps forgingSteps = node.node("steps").get(ForgingSteps.class);
        ForgingMaterial material = node.node("material").get(ForgingMaterial.class);
        if (forgingSteps == null && material == null) {
            return null;
        }
        Double temperature = node.node("temperature").get(Double.class);
        Long timestamp = node.node("timestamp").get(Long.class);
        HeatBehavior heatBehavior = node.node("heat-behavior").get(HeatBehavior.class);
        return new ForgingItem(
                material,
                forgingSteps == null ? new ForgingSteps(List.of()) : forgingSteps,
                heatBehavior == null ? null : new TemperatureData(
                        heatBehavior,
                        temperature == null ? 25D : temperature,
                        timestamp == null ? TimeProvider.time() : timestamp
                )
        );
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgingItem obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        if (!obj.steps().steps().isEmpty()) {
            node.node("steps").set(obj.steps());
        }
        if (obj.material() != null) {
            node.node("material").set(obj.material());
        }
        if (obj.temperatureData() != null) {
            if (obj.temperatureData().temperature() != 25D) {
                node.node("temperature").set(obj.temperatureData().temperature());
            }
            node.node("heat-behavior").set(obj.temperatureData().behavior());
            node.node("timestamp").set(obj.temperatureData().timestamp());
        }
    }
}
