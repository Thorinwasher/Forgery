package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.util.Duration;
import dev.thorinwasher.forgery.util.TimeUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class DurationSerializer implements TypeSerializer<Duration> {
    @Override
    public Duration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String duration = node.getString();
        if (duration == null) {
            return null;
        }
        return new Duration(TimeUtil.parse(duration));
    }

    @Override
    public void serialize(Type type, @Nullable Duration obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(TimeUtil.minimalString(obj.duration()));
    }
}
