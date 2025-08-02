package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.forgeries.StructureHolder;
import dev.thorinwasher.forgery.vector.BlockLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MultiblockStructure<H extends StructureHolder<H>> {

    List<BlockLocation> positions();

    H holder();

    BlockLocation unique();

    <V> @Nullable V metaValue(StructureMeta<V> meta);
}