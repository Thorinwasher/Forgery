package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.structure.ForgeryStructureType;
import dev.thorinwasher.forgery.structure.MultiblockStructure;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.vector.BlockLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;

import java.util.Set;

public class BlastFurnace implements StructureHolder<BlastFurnace>, Interactable {
    private MultiblockStructure<BlastFurnace> structure;

    public BlastFurnace() {

    }


    @Override
    public MultiblockStructure<BlastFurnace> getStructure() {
        return this.structure;
    }

    @Override
    public void setStructure(MultiblockStructure<BlastFurnace> structure) {
        this.structure = structure;
    }

    @Override
    public ForgeryStructureType structureType() {
        return ForgeryRegistry.STRUCTURE_TYPES.get("blast_furnace");
    }

    @Override
    public void interact(Player actor, BlockLocation location) {
        Block block = location.toBlock();
        BlockType blockType = block.getType().asBlockType();
        Set<BlockType> fuel = structure.metaValue(StructureMeta.FUEL_INTERFACE_BLOCKS);
        Set<BlockType> input = structure.metaValue(StructureMeta.INPUT_INTERFACE_BLOCKS);
        if (fuel != null && fuel.contains(blockType)) {
            actor.sendMessage(Component.text("Fuel"));
        }
        if (input != null && input.contains(blockType)) {
            actor.sendMessage(Component.text("Input"));
        }
    }
}
