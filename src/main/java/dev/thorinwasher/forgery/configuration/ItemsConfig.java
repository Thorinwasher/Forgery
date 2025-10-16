package dev.thorinwasher.forgery.configuration;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.recipe.ItemReference;
import dev.thorinwasher.forgery.serialize.Serialize;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class ItemsConfig {

    public ItemType defaultMaterial = ItemType.ROTTEN_FLESH;

    public List<ItemPreset> items = List.of(
            new ItemPreset("pig_iron", NamespacedKey.minecraft("iron_ingot"), Component.text("Pig Iron Ingot")),
            new ItemPreset("pliers", NamespacedKey.minecraft("shears"), Component.text("Pliers")),
            new ItemPreset("bellows", NamespacedKey.minecraft("dried_kelp"), Component.text("Bellows")),
            new ItemPreset("hammer", NamespacedKey.minecraft("mace"), Component.text("hammer"))
    );

    public void insertIntoReferences(Map<Key, ItemReference> itemReferences) {
        items.forEach(preset -> itemReferences.putIfAbsent(Forgery.key(preset.key()), new ItemReference(preset.key(), preset.toItem(defaultMaterial))));
    }

    public static ItemsConfig load(File dataFolder) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .defaultOptions(opts -> opts.serializers(Serialize::registerSerializers))
                    .file(new File(dataFolder, "items.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();
            ConfigurationNode node = loader.load();
            node.mergeFrom(loader.createNode().set(new ItemsConfig()));
            loader.save(node);
            return node
                    .get(ItemsConfig.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
