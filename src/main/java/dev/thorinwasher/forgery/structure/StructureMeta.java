package dev.thorinwasher.forgery.structure;

import com.google.gson.JsonElement;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.ForgeryRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record StructureMeta<T>(Key key, Function<JsonElement, T> deserializer) implements Keyed {

    public static final StructureMeta<ForgeryStructureType> TYPE = new StructureMeta<>(NamespacedKey.fromString("type", Forgery.instance()), json ->
            ForgeryRegistry.STRUCTURE_TYPES.get(NamespacedKey.fromString(json.getAsString(), Forgery.instance()))
    );

    public static final StructureMeta<Set<BlockType>> INPUT_INTERFACE_BLOCKS = new StructureMeta<>(
            NamespacedKey.fromString("input_interface_blocks", Forgery.instance()), StructureMeta::parseBlocks
    );

    public static final StructureMeta<Set<BlockType>> FUEL_INTERFACE_BLOCKS = new StructureMeta<>(
            NamespacedKey.fromString("fuel_interface_blocks", Forgery.instance()), StructureMeta::parseBlocks
    );

    private static Set<BlockType> parseBlocks(JsonElement json) {
        return json.getAsJsonArray().asList().stream()
                .map(JsonElement::getAsString)
                .map(NamespacedKey::fromString)
                .filter(Objects::nonNull)
                .map(Registry.BLOCK::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
