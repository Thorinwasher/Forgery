package dev.thorinwasher.forgery.integration.item;

import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.ItemIntegration;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.inventory.ForgingMaterialPersistentDataType;
import dev.thorinwasher.forgery.util.ForgeryKey;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

public class ForgeryItemIntegration implements ItemIntegration {

    @Override
    public Optional<ForgingMaterialResult> toForgery(ItemStack itemStack) {
        PersistentDataContainerView view = itemStack.getPersistentDataContainer();
        if (!view.has(ItemAdapter.FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE)) {
            return Optional.empty();
        }
        return Optional.ofNullable(view.get(ItemAdapter.FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE))
                .map(this::toResult);
    }

    @Override
    public Optional<ItemStack> toBukkit(ForgingMaterial material) {
        return Optional.empty();
    }

    @Override
    public Stream<ForgingMaterialResult> fromString(String key) {
        return Stream.of(new ForgingMaterial(new ForgeryKey(id(), key)))
                .map(this::toResult);
    }

    @Override
    public String id() {
        return "forgery";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void initialize() {
        //NOOP, everything is already set up
    }

    private ForgingMaterialResult toResult(ForgingMaterial material) {
        return new ForgingMaterialResult(material, Priority.NORMAL);
    }
}
