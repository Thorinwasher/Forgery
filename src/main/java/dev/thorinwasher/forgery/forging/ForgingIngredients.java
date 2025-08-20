package dev.thorinwasher.forgery.forging;

import dev.thorinwasher.forgery.inventory.ForgingMaterial;

import java.util.Map;

public record ForgingIngredients(Map<ForgingMaterial, Integer> ingredients) {

}
