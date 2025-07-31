package dev.thorinwasher.forgery.structure;

import com.google.gson.JsonElement;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.ForgeryRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;

import java.util.function.Function;

public record StructureMeta<T>(Key key, Function<JsonElement, T> deserializer) implements Keyed {

    public static final StructureMeta<ForgeryStructureType> TYPE = new StructureMeta<>(NamespacedKey.fromString("type", Forgery.instance()), json ->
            ForgeryRegistry.STRUCTURE_TYPES.get(NamespacedKey.fromString(json.getAsString(), Forgery.instance()))
    );
}
