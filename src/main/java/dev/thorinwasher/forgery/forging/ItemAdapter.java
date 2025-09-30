package dev.thorinwasher.forgery.forging;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.integration.ItemIntegration;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.inventory.ForgingMaterialPersistentDataType;
import dev.thorinwasher.forgery.util.PdcKeys;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record ItemAdapter(IntegrationRegistry registry) {

    public Optional<ForgingItem> toForgery(ItemStack itemStack) {
        PersistentDataContainerView view = itemStack.getPersistentDataContainer();
        Optional<ForgingMaterial> materialOptional = registry.itemIntegrations()
                .map(itemIntegration -> itemIntegration.toForgery(itemStack))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(ItemIntegration.ForgingMaterialResult::priority))
                .map(ItemIntegration.ForgingMaterialResult::material)
                .findFirst();
        Double tempTemperature = view.get(PdcKeys.TEMPERATURE, PersistentDataType.DOUBLE);
        double temperature = tempTemperature == null ? 25D : tempTemperature;
        Long tempTimestamp = view.get(PdcKeys.TIMESTAMP, PersistentDataType.LONG);
        long timeStamp = tempTimestamp == null ? TimeProvider.time() : tempTimestamp;
        return Optional.ofNullable(view.get(PdcKeys.FORGING_STEPS, ForgingStepsPersistentData.INSTANCE))
                .map(forgingSteps -> new ForgingItem(materialOptional.orElse(null), forgingSteps, temperature, timeStamp))
                .or(() ->
                        materialOptional.map(material ->
                                new ForgingItem(material, new ForgingSteps(List.of()), temperature, timeStamp)
                        )
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
                pdc.set(PdcKeys.FORGING_STEPS, ForgingStepsPersistentData.INSTANCE, item.steps());
            }
            if (item.material() != null && item.material().providesExtraData()) {
                pdc.set(PdcKeys.FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE, item.material());
            }
        });
        return output;
    }

    public static ItemStack failedItem() {
        ItemStack itemStack = new ItemStack(Material.STONE);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Failed slag"));
        return itemStack;
    }

    public static ItemStack placeholder() {
        ItemStack itemStack = new ItemStack(Material.GLASS);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Placeholder"));
        return itemStack;
    }
}
