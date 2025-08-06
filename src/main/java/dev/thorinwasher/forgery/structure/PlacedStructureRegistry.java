package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.forgeries.StructureBehavior;
import dev.thorinwasher.forgery.vector.BlockLocation;
import dev.thorinwasher.forgery.vector.BlockVector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlacedStructureRegistry {

    private final Map<UUID, Map<BlockVector, PlacedForgeryStructure>> structures = new ConcurrentHashMap<>();
    private final Map<ForgeryStructure, Set<PlacedForgeryStructure>> typedPlacedForgeryStructureMap = new ConcurrentHashMap<>();

    public void registerStructure(PlacedForgeryStructure PlacedForgeryStructure) {
        for (BlockLocation location : PlacedForgeryStructure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).put(location.toVector(), PlacedForgeryStructure);
        }
        typedPlacedForgeryStructureMap.computeIfAbsent(PlacedForgeryStructure.structure(), ignored -> new HashSet<>()).add(PlacedForgeryStructure);
    }

    public void unregisterStructure(PlacedForgeryStructure structure) {
        for (BlockLocation location : structure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).remove(location.toVector());
        }
        typedPlacedForgeryStructureMap.computeIfAbsent(structure.structure(), ignored -> new HashSet<>()).remove(structure);
    }

    public Optional<PlacedForgeryStructure> getStructure(BlockLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BlockVector, PlacedForgeryStructure> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()));
    }

    public Set<PlacedForgeryStructure> getStructures(Collection<BlockLocation> locations) {
        Set<PlacedForgeryStructure> breweryStructures = new HashSet<>();
        for (BlockLocation location : locations) {
            getStructure(location).ifPresent(breweryStructures::add);
        }
        return breweryStructures;
    }

    public Set<PlacedForgeryStructure> getStructures(ForgeryStructure structure) {
        return typedPlacedForgeryStructureMap.computeIfAbsent(structure, ignored -> new HashSet<>());
    }

    public Optional<StructureBehavior> getHolder(BlockLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BlockVector, PlacedForgeryStructure> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()))
                .map(PlacedForgeryStructure::holder);
    }

    public void unloadWorld(UUID worldUuid) {
        Map<BlockVector, PlacedForgeryStructure> removed = structures.remove(worldUuid);
        if (removed == null) {
            return;
        }
        removed.forEach((ignored1, structure) -> {
            typedPlacedForgeryStructureMap.computeIfAbsent(structure.structure(), ignored2 -> new HashSet<>()).remove(structure);
        });
    }

    public void clear() {
        structures.clear();
    }
}
