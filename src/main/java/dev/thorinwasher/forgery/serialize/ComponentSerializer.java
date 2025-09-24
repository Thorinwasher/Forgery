package dev.thorinwasher.forgery.serialize;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ComponentSerializer implements TypeSerializer<Component> {
    @Override
    public Component deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null) {
            return null;
        }
        return MiniMessage.miniMessage().deserialize(string);
    }

    @Override
    public void serialize(Type type, @Nullable Component obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(MiniMessage.miniMessage().serialize(obj));
    }
}
