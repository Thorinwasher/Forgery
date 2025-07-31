package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.structure.ForgeryStructureType;
import dev.thorinwasher.forgery.structure.MultiblockStructure;

public class BlastFurnace implements StructureHolder<BlastFurnace> {
    private MultiblockStructure<BlastFurnace> structure;

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
}
