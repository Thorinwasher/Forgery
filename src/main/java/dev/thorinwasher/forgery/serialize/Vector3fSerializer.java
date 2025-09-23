package dev.thorinwasher.forgery.serialize;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class Vector3fSerializer implements TypeSerializer<Vector3f> {
    @Override
    public Vector3f deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null) {
            return null;
        }
        String[] split = string.split("[ ,]");
        float x = Float.parseFloat(split[0]);
        float y = Float.parseFloat(split[1]);
        float z = Float.parseFloat(split[2]);
        return new Vector3f(x, y, z);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Vector3f obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(String.format("%f,%f,%f", obj.x, obj.y, obj.z));
    }
}
