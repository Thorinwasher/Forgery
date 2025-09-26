package dev.thorinwasher.forgery.integration.item;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.integration.ItemIntegration;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.inventory.ForgingMaterialPersistentDataType;
import dev.thorinwasher.forgery.recipe.ItemReference;
import dev.thorinwasher.forgery.util.ForgeryKey;
import dev.thorinwasher.forgery.util.PdcKeys;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ForgeryItemIntegration implements ItemIntegration {

    private final Map<Key, ItemReference> itemReferences;

    public ForgeryItemIntegration(Map<Key, ItemReference> itemReferences) {
        this.itemReferences = itemReferences;
    }

    @Override
    public Optional<ForgingMaterialResult> toForgery(ItemStack itemStack) {
        PersistentDataContainerView view = itemStack.getPersistentDataContainer();
        if (!view.has(PdcKeys.FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE)) {
            return Optional.empty();
        }

        return Optional.ofNullable(view.get(PdcKeys.FORGING_MATERIAL, ForgingMaterialPersistentDataType.INSTANCE))
                .map(this::toResult);
    }

    @Override
    public Optional<ItemStack> toBukkit(ForgingMaterial material) {
        if (material.key() == null || !material.key().namespace().equals(Forgery.NAMESPACE)) {
            return Optional.empty();
        }
        NamespacedKey key = Forgery.key(material.key().key());
        return Optional.ofNullable(itemReferences.get(key))
                .map(itemReference -> itemReference.write(material.score()));
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
