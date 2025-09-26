package dev.thorinwasher.forgery.structure;

import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Set;

@ConfigSerializable
public record StructureToolInputBehavior(Set<BlockType> interfaceBlocks, Sound interactSound) {
}
