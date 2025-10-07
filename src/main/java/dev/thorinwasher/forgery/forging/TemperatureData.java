package dev.thorinwasher.forgery.forging;

import dev.thorinwasher.forgery.recipe.HeatBehavior;

public record TemperatureData(HeatBehavior behavior, double temperature, long timestamp) {
}
