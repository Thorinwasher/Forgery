package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.forgeries.StructureHolder;
import dev.thorinwasher.forgery.vector.BlockLocation;
import dev.thorinwasher.forgery.vector.BlockVector;

import java.util.*;

public class PlacedStructureRegistry {

    private final Map<UUID, Map<BlockVector, MultiblockStructure<? extends StructureHolder<?>>>> structures = new HashMap<>();
    private final Map<ForgeryStructureType, Set<MultiblockStructure<?>>> typedMultiBlockStructureMap = new HashMap<>();

    public void registerStructure(MultiblockStructure<?> multiblockStructure) {
        for (BlockLocation location : multiblockStructure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).put(location.toVector(), multiblockStructure);
        }
        typedMultiBlockStructureMap.computeIfAbsent(multiblockStructure.holder().structureType(), ignored -> new HashSet<>()).add(multiblockStructure);
    }

    public void unregisterStructure(MultiblockStructure<?> structure) {
        for (BlockLocation location : structure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).remove(location.toVector());
        }
        typedMultiBlockStructureMap.computeIfAbsent(structure.holder().structureType(), ignored -> new HashSet<>()).remove(structure);
    }

    public Optional<MultiblockStructure<?>> getStructure(BlockLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BlockVector, MultiblockStructure<?>> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()));
    }

    public Set<MultiblockStructure<?>> getStructures(Collection<BlockLocation> locations) {
        Set<MultiblockStructure<?>> breweryStructures = new HashSet<>();
        for (BlockLocation location : locations) {
            getStructure(location).ifPresent(breweryStructures::add);
        }
        return breweryStructures;
    }

    public Set<MultiblockStructure<?>> getStructures(ForgeryStructureType structureType) {
        return typedMultiBlockStructureMap.computeIfAbsent(structureType, ignored -> new HashSet<>());
    }

    public Optional<StructureHolder<?>> getHolder(BlockLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BlockVector, MultiblockStructure<? extends StructureHolder<?>>> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()))
                .map(MultiblockStructure::holder);
    }

    public void unloadWorld(UUID worldUuid) {
        Map<BlockVector, MultiblockStructure<? extends StructureHolder<?>>> removed = structures.remove(worldUuid);
        if (removed == null) {
            return;
        }
        removed.forEach((ignored1, structure) -> {
            typedMultiBlockStructureMap.computeIfAbsent(structure.holder().structureType(), ignored2 -> new HashSet<>()).remove(structure);
        });
    }

    public void clear() {
        structures.clear();
    }
}
