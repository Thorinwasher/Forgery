package dev.thorinwasher.forgery.util;

import dev.thorinwasher.forgery.Forgery;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A key with fewer restrictions on text
 */
public record ForgeryKey(String namespace, String key) {

    public static ForgeryKey forgery(String string) {
        String[] split = string.split(":", 2);
        if (split.length == 1) {
            return new ForgeryKey(Forgery.NAMESPACE, split[0]);
        }
        return new ForgeryKey(split[0], split[1]);
    }

    public Optional<Key> toAdventure() {
        if (!Key.parseableNamespace(namespace) || !Key.parseableValue(key)) {
            return Optional.empty();
        }
        return Optional.of(Key.key(namespace, key));
    }

    public static ForgeryKey fromAdventure(@NotNull Key key) {
        return new ForgeryKey(key.namespace(), key.value());
    }

    public String asString() {
        return namespace + ":" + key;
    }
}
