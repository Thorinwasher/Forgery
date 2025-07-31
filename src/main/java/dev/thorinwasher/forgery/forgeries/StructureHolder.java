package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.structure.ForgeryStructureType;
import dev.thorinwasher.forgery.structure.MultiblockStructure;

public interface StructureHolder<H extends StructureHolder<H>> {

    MultiblockStructure<H> getStructure();

    void setStructure(MultiblockStructure<H> structure);

    ForgeryStructureType structureType();
}
