package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.structure.ForgeryStructureType;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;

import java.util.UUID;

public interface StructureHolder<H extends StructureHolder<H>> {

    PlacedForgeryStructure<H> placedStructure();

    void setStructure(PlacedForgeryStructure<H> structure);

    ForgeryStructureType structureType();

    UUID uuid();
}
