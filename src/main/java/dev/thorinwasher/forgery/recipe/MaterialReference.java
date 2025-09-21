package dev.thorinwasher.forgery.recipe;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public record MaterialReference(ForgingMaterial material) implements RecipeResult.ItemWriter {
    public MaterialReference {
        Preconditions.checkNotNull(material.key(), "Expected material to have a key");
    }

    @Override
    public ItemStack write(IntegrationRegistry integrationRegistry, int score) {
        // TODO add score data
        return integrationRegistry.itemIntegrations()
                .map(itemIntegration -> itemIntegration.toBukkit(material))
                .flatMap(Optional::stream)
                .findAny().orElse(ItemAdapter.failedItem());
    }
}
