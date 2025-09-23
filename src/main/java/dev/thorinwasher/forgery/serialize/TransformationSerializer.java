package dev.thorinwasher.forgery.serialize;

import org.bukkit.util.Transformation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class TransformationSerializer implements TypeSerializer<Transformation> {
    @Override
    public Transformation deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        Vector3f translation = node.node("translation").get(Vector3f.class);
        Quaternionf leftRotation = node.node("left-rotation").get(Quaternionf.class);
        Vector3f scale = node.node("scale").get(Vector3f.class);
        Quaternionf rightRotation = node.node("right-rotation").get(Quaternionf.class);
        if (translation == null && leftRotation == null && scale == null && rightRotation == null) {
            return null;
        }
        return new Transformation(
                translation == null ? new Vector3f() : translation,
                leftRotation == null ? new Quaternionf() : leftRotation,
                scale == null ? new Vector3f() : scale,
                rightRotation == null ? new Quaternionf() : rightRotation
        );
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Transformation obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        if (!obj.getTranslation().equals(new Vector3f())) {
            node.node("translation").set(obj.getTranslation());
        }
        if (!obj.getLeftRotation().equals(new Quaternionf())) {
            node.node("left-rotation").set(obj.getLeftRotation());
        }
        if (!obj.getScale().equals(new Vector3f())) {
            node.node("scale").set(obj.getScale());
        }
        if (!obj.getRightRotation().equals(new Quaternionf())) {
            node.node("left-rotation").set(obj.getRightRotation());
        }
    }
}
