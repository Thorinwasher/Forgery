package dev.thorinwasher.forgery.structure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record InventoryTransform(String state, int decrementAmount) {
}
