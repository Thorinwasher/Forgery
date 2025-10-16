package dev.thorinwasher.forgery.configuration;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@ConfigSerializable
public record ItemPreset(@Required String key, @Required NamespacedKey model, @Required Component name, @Nullable ItemType material) {

    public ItemPreset(String key, NamespacedKey model, Component name) {
        this(key, model, name, null);
    }

    private static final Set<DataComponentType> BANNED_FROM_STRIP = Set.of(
            DataComponentTypes.ITEM_NAME,
            DataComponentTypes.MAX_STACK_SIZE
    );

    public ItemStack toItem(ItemType defaultMaterial) {
        ItemStack output = (material == null ? defaultMaterial : material).createItemStack();
        stripComponents(output);
        output.setData(DataComponentTypes.ITEM_MODEL, model);
        output.setData(DataComponentTypes.CUSTOM_NAME, name
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE)
        );
        return output;
    }

    private void stripComponents(ItemStack itemStack) {
        List<DataComponentType> dataComponentTypes = itemStack.getDataTypes().stream()
                .filter(dataComponentType -> !BANNED_FROM_STRIP.contains(dataComponentType))
                .toList();
        for (DataComponentType dataComponentType : dataComponentTypes) {
            itemStack.unsetData(dataComponentType);
        }
    }
}
