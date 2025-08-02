package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.forgeries.StructureHolder;
import dev.thorinwasher.forgery.vector.BlockLocation;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlacedForgeryStructure<H extends StructureHolder<H>> implements MultiblockStructure<H> {
    private static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();
    private final ForgeryStructure structure;
    private final Matrix3d transformation;
    private final Location worldOrigin;
    private final BlockLocation unique;
    private final H holder;

    public PlacedForgeryStructure(ForgeryStructure structure, Matrix3d transformation,
                                  Location worldOrigin, H holder) {
        this.structure = structure;
        this.transformation = transformation;
        this.worldOrigin = worldOrigin;
        this.unique = compileUnique();
        this.holder = holder;
    }

    public static <H extends StructureHolder<H>> Optional<PlacedForgeryStructure<H>> findValid(ForgeryStructure structure, Location worldOrigin, Supplier<H> holderSupplier) {
        for (Matrix3d transformation : ALLOWED_TRANSFORMATIONS) {
            Optional<Location> possibleOrigin = structure.findValidOrigin(transformation, worldOrigin);
            if (possibleOrigin.isPresent()) {
                H holder = holderSupplier.get();
                PlacedForgeryStructure<H> placedStructure = possibleOrigin
                        .map(origin -> new PlacedForgeryStructure<>(structure, transformation, origin, holder))
                        .get();
                holder.setStructure(placedStructure);
                return Optional.of(placedStructure);
            }
        }
        return Optional.empty();
    }

    public List<BlockLocation> positions() {
        return structure.getExpectedBlocks(transformation, worldOrigin)
                .keySet()
                .stream()
                .map(location -> new BlockLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID()))
                .toList();
    }

    @Override
    public BlockLocation unique() {
        return unique;
    }

    @Override
    public <V> @Nullable V metaValue(StructureMeta<V> meta) {
        return structure.metaValue(meta);
    }

    @Override
    public H holder() {
        return holder;
    }

    private BlockLocation compileUnique() {
        List<BlockLocation> positions = new ArrayList<>(positions());
        positions.sort(this::comparePositions);
        return positions.getFirst();
    }

    private static List<Matrix3d> compileAllowedTransformations() {
        Stream.Builder<Matrix3d> output = Stream.builder();
        Matrix3d transformation = new Matrix3d();
        for (int i = 0; i < 4; i++) {
            output.add(transformation.rotate(Math.PI / 2 * i, 0, 1, 0, new Matrix3d()));
        }
        transformation.reflect(1, 0, 0);
        for (int i = 0; i < 4; i++) {
            output.add(transformation.rotate(Math.PI / 2 * i, 0, 1, 0, new Matrix3d()));
        }
        return output
                .build()
                .map(PlacedForgeryStructure::round)
                .toList();
    }

    private int comparePositions(BlockLocation breweryLocation, BlockLocation breweryLocation1) {
        if (breweryLocation.y() > breweryLocation1.y()) {
            return -1;
        }
        if (breweryLocation.x() > breweryLocation1.x()) {
            return -1;
        }
        if (breweryLocation.z() > breweryLocation1.z()) {
            return -1;
        }
        return 0;
    }

    private static Matrix3d round(Matrix3d input) {
        double[] values = input.get(new double[9]);
        Matrix3d mat = new Matrix3d();
        mat.set(Arrays.stream(values).map(Math::round).toArray());
        return mat;
    }
}
