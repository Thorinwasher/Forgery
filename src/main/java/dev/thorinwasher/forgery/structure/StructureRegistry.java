package dev.thorinwasher.forgery.structure;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StructureRegistry {

    private final Map<ForgeryStructureType, Map<Material, Set<ForgeryStructure>>> structuresWithMaterials = new HashMap<>();
    private final Map<ForgeryStructureType, Set<ForgeryStructure>> structures = new HashMap<>();
    private final Map<String, ForgeryStructure> structureNames = new HashMap<>();

    public Optional<ForgeryStructure> getStructure(@NotNull String key) {
        Preconditions.checkNotNull(key);
        return Optional.ofNullable(structureNames.get(key));
    }

    public Set<ForgeryStructure> getPossibleStructures(@NotNull Material material, ForgeryStructureType structureType) {
        Preconditions.checkNotNull(material);
        return structuresWithMaterials.computeIfAbsent(structureType, ignored -> new HashMap<>()).getOrDefault(material, Set.of());
    }

    public void addStructure(@NotNull ForgeryStructure structure) {
        Preconditions.checkNotNull(structure);
        structureNames.put(structure.getName(), structure);
        structures.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashSet<>()).add(structure);
        for (BlockData blockData : structure.getPalette()) {
            structuresWithMaterials.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashMap<>())
                    .computeIfAbsent(blockData.getMaterial(), ignored -> new HashSet<>()).add(structure);
        }
    }
}
