package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.forgeries.StructureHolder;
import dev.thorinwasher.forgery.vector.BlockLocation;
import dev.thorinwasher.forgery.vector.BlockVector;

import java.util.*;

public class PlacedStructureRegistry {

    private final Map<UUID, Map<BlockVector, PlacedForgeryStructure<? extends StructureHolder<?>>>> structures = new HashMap<>();
    private final Map<ForgeryStructureType, Set<PlacedForgeryStructure<?>>> typedPlacedForgeryStructureMap = new HashMap<>();

    public void registerStructure(PlacedForgeryStructure<?> PlacedForgeryStructure) {
        for (BlockLocation location : PlacedForgeryStructure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).put(location.toVector(), PlacedForgeryStructure);
        }
        typedPlacedForgeryStructureMap.computeIfAbsent(PlacedForgeryStructure.holder().structureType(), ignored -> new HashSet<>()).add(PlacedForgeryStructure);
    }

    public void unregisterStructure(PlacedForgeryStructure<?> structure) {
        for (BlockLocation location : structure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).remove(location.toVector());
        }
        typedPlacedForgeryStructureMap.computeIfAbsent(structure.holder().structureType(), ignored -> new HashSet<>()).remove(structure);
    }

    public Optional<PlacedForgeryStructure<?>> getStructure(BlockLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BlockVector, PlacedForgeryStructure<?>> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()));
    }

    public Set<PlacedForgeryStructure<?>> getStructures(Collection<BlockLocation> locations) {
        Set<PlacedForgeryStructure<?>> breweryStructures = new HashSet<>();
        for (BlockLocation location : locations) {
            getStructure(location).ifPresent(breweryStructures::add);
        }
        return breweryStructures;
    }

    public Set<PlacedForgeryStructure<?>> getStructures(ForgeryStructureType structureType) {
        return typedPlacedForgeryStructureMap.computeIfAbsent(structureType, ignored -> new HashSet<>());
    }

    public Optional<StructureHolder<?>> getHolder(BlockLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BlockVector, PlacedForgeryStructure<? extends StructureHolder<?>>> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()))
                .map(PlacedForgeryStructure::holder);
    }

    public void unloadWorld(UUID worldUuid) {
        Map<BlockVector, PlacedForgeryStructure<? extends StructureHolder<?>>> removed = structures.remove(worldUuid);
        if (removed == null) {
            return;
        }
        removed.forEach((ignored1, structure) -> {
            typedPlacedForgeryStructureMap.computeIfAbsent(structure.holder().structureType(), ignored2 -> new HashSet<>()).remove(structure);
        });
    }

    public void clear() {
        structures.clear();
    }
}
