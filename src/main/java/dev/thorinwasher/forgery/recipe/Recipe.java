package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record Recipe(ForgingSteps steps, RecipeResult result, String structureType, ForgeryKey key) {
}
