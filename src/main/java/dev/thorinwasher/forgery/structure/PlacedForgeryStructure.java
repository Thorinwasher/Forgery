package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.forgeries.StructureBehavior;
import dev.thorinwasher.forgery.vector.BlockLocation;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record PlacedForgeryStructure
        (
                ForgeryStructure structure,
                Matrix3d transformation,
                BlockLocation worldOrigin,
                StructureBehavior behavior
        ) {

    private static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();

    public static Optional<PlacedForgeryStructure> findValid(ForgeryStructure structure, Location worldOrigin, Supplier<StructureBehavior> holderSupplier) {
        for (Matrix3d transformation : ALLOWED_TRANSFORMATIONS) {
            Optional<Location> possibleOrigin = structure.findValidOrigin(transformation, worldOrigin);
            if (possibleOrigin.isPresent()) {
                StructureBehavior holder = holderSupplier.get();
                PlacedForgeryStructure placedStructure = possibleOrigin
                        .map(origin -> new PlacedForgeryStructure(structure, transformation, BlockLocation.fromLocation(possibleOrigin.get()), holder))
                        .get();
                holder.setStructure(placedStructure);
                return Optional.of(placedStructure);
            }
        }
        return Optional.empty();
    }

    public List<BlockLocation> positions() {
        return structure.getExpectedBlocks(transformation, worldOrigin.toLocation())
                .keySet()
                .stream()
                .map(location -> new BlockLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID()))
                .toList();
    }

    public <V> @Nullable V metaValue(StructureMeta<V> meta) {
        return structure.metaValue(meta);
    }

    public StructureBehavior holder() {
        return behavior;
    }

    private static List<Matrix3d> compileAllowedTransformations() {
        Stream.Builder<Matrix3d> output = Stream.builder();
        Matrix3d transformation = new Matrix3d();
        for (int i = 0; i < 4; i++) {
            output.add(transformation.rotate(Math.PI / 2 * i, 0, 1, 0, new Matrix3d()));
        }
        for (int i = 0; i < 4; i++) {
            output.add(transformation.rotate(Math.PI / 2 * i, 0, 1, 0, new Matrix3d()).negateX());
        }
        return output
                .build()
                .map(PlacedForgeryStructure::round)
                .toList();
    }

    private static Matrix3d round(Matrix3d input) {
        double[] values = input.get(new double[9]);
        Matrix3d mat = new Matrix3d();
        mat.set(Arrays.stream(values).map(Math::round).toArray());
        return mat;
    }

    public BlockLocation origin() {
        return worldOrigin;
    }

    public Transformation adjustTransformation(Transformation bukkitTransformation) {
        return new Transformation(
                transformation.transform(bukkitTransformation.getTranslation(), new Vector3f()),
                rotateQuaternionf(bukkitTransformation.getLeftRotation()),
                bukkitTransformation.getScale(),
                bukkitTransformation.getRightRotation()
        );
    }

    private Quaternionf rotateQuaternionf(Quaternionf input) {
        return transformation.rotate(input, new Matrix3d()).getNormalizedRotation(new Quaternionf());
    }
}
