package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.forging.ForgingSteps;

public record Recipe(ForgingSteps steps, RecipeResult result, String structureType, String name) {
}
