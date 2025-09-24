package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.ForgeryKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RecipeResult(ForgeryKey key, int amount, boolean overrideLore, List<Component> lore,
                           @Nullable Component name, @Nullable String toolId) {

    private static final NamespacedKey TOOL_KEY = Forgery.key("tool");


    public ItemStack get(int score, IntegrationRegistry registry) {
        return get(score, registry, 1);
    }

    public ItemStack get(int score, IntegrationRegistry registry, int multiplier) {
        ForgingMaterial material = new ForgingMaterial(key, score);
        ItemStack itemStack = registry.itemIntegration(key().namespace())
                .flatMap(itemIntegration -> itemIntegration.toBukkit(material))
                .orElse(ItemAdapter.placeholder());

        if (name() != null) {
            itemStack.setData(DataComponentTypes.CUSTOM_NAME, name()
                    .decoration(TextDecoration.ITALIC, false)
                    .colorIfAbsent(NamedTextColor.WHITE)
            );
        }
        if (!lore().isEmpty()) {
            itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(
                    lore()
            ));
        }
        if (toolId() != null) {
            itemStack.editPersistentDataContainer(pdc -> {
                pdc.set(TOOL_KEY, PersistentDataType.STRING, toolId());
            });
        }
        itemStack.setAmount(amount * multiplier);
        return itemStack;
    }
}
