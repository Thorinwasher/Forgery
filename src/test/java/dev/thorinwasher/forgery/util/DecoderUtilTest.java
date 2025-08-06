package dev.thorinwasher.forgery.util;

import org.joml.Matrix3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecoderUtilTest {

    @ParameterizedTest
    @MethodSource("compileAllowedTransformations")
    void asTransformation(Matrix3d transformation) {
        int encoded = DecoderUtil.asInteger(transformation);
        Matrix3d decoded = DecoderUtil.asTransformation(encoded);
        assertEquals(transformation, decoded);
    }

    private static List<Arguments> compileAllowedTransformations() {
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
                .map(DecoderUtilTest::round)
                .map(Arguments::of)
                .toList();
    }

    private static Matrix3d round(Matrix3d input) {
        double[] values = input.get(new double[9]);
        Matrix3d mat = new Matrix3d();
        mat.set(Arrays.stream(values).map(Math::round).toArray());
        return mat;
    }
}