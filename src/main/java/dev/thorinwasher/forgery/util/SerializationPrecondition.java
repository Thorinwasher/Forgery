package dev.thorinwasher.forgery.util;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class SerializationPrecondition {

    public static void check(ConfigurationNode node, boolean expression, String message) throws SerializationException {
        if (!expression) {
            SerializationException exception = new SerializationException(message);
            exception.initPath(node::path);
            throw exception;
        }
    }
}
