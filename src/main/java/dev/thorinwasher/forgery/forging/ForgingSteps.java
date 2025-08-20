package dev.thorinwasher.forgery.forging;

import dev.thorinwasher.forgery.inventory.ForgingMaterial;

import java.util.List;
import java.util.Optional;

public record ForgingSteps(List<ForgingStep> steps) {

    public Optional<ForgingMaterial> calculateMaterial() {
        return Optional.empty();
    }
}
