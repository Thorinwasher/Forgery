package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.forging.ToolInput;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ToolInputSerializer implements TypeSerializer<ToolInput> {

    @Override
    public ToolInput deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        String value = node.node("tool").get(String.class);
        if (value == null) {
            return null;
        }
        Long timeStamp = node.node("time_stamp").get(Long.class);
        if (timeStamp == null) {
            return null;
        }
        return new ToolInput(value, timeStamp);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ToolInput obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.node("tool").set(obj.tool());
        node.node("time_stamp").set(obj.timePoint());
    }
}
