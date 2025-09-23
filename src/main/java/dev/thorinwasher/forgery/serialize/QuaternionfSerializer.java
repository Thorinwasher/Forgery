package dev.thorinwasher.forgery.serialize;

import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class QuaternionfSerializer implements TypeSerializer<Quaternionf> {
    @Override
    public Quaternionf deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null) {
            return null;
        }
        String[] split = string.split("[ ,]");
        float angle = Float.parseFloat(split[0]);
        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float z = Float.parseFloat(split[3]);
        return new AxisAngle4f(angle, x, y, z).get(new Quaternionf());
    }

    @Override
    public void serialize(Type type, @Nullable Quaternionf obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        AxisAngle4f angle4f = obj.get(new AxisAngle4f());
        node.set(String.format("%f,%f,%f,%f", angle4f.angle, angle4f.x, angle4f.y, angle4f.z));
    }
}
