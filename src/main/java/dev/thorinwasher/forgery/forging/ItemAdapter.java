package dev.thorinwasher.forgery.forging;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.integration.ItemIntegration;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.inventory.ForgingMaterialPersistentDataType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record ItemAdapter(IntegrationRegistry registry) {

    public static final NamespacedKey FORGING_STEPS = new NamespacedKey(Forgery.NAMESPACE, "forging_steps");
    public static final NamespacedKey FORGING_MATERIAL = new NamespacedKey(Forgery.NAMESPACE, "material");

    public Optional<ForgingItem> toForgery(ItemStack itemStack) {
        PersistentDataContainerView view = itemStack.getPersistentDataContainer();
        Optional<ForgingMaterial> materialOptional = registry.itemIntegrations()
                .map(itemIntegration -> itemIntegration.toForgery(itemStack))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(ItemIntegration.ForgingMaterialResult::priority))
                .map(ItemIntegration.ForgingMaterialResult::material)
                .findFirst();
        return Optional.ofNullable(view.get(FORGING_STEPS, ForgingStepPersistentData.INSTANCE))
                .map(forgingSteps -> new ForgingItem(materialOptional.orElse(null), forgingSteps))
                .or(() ->
                        materialOptional.map(material -> new ForgingItem(material, new ForgingSteps(List.of())))
                );
    }

    public ItemStack toBukkit(ForgingItem item) {
        Optional<ForgingMaterial> stepsMaterial = item.steps().calculateMaterial();
        ItemStack output = stepsMaterial.or(() -> Optional.ofNullable(item.material()))
                .flatMap(forgingMaterial -> registry.itemIntegration(forgingMaterial.key().namespace())
                        .flatMap(itemIntegration -> itemIntegration.toBukkit(forgingMaterial))
                ).orElse(failedItem());
        output.editPersistentDataContainer(pdc -> {
            if (!item.steps().steps().isEmpty()) {
                pdc.set(FORGING_STEPS, ForgingStepPersistentData.INSTANCE, item.steps());
            }
            if (item.material() != null && item.material().providesExtraData()) {
                pdc.set(FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE, item.material());
            }
        });
        return output;
    }

    private ItemStack failedItem() {
        ItemStack itemStack = new ItemStack(Material.STONE);
        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                Component.text("Failed slag")
        )));
        return itemStack;
    }
}
