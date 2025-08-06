package dev.thorinwasher.forgery.util;

import org.joml.Matrix3d;

import java.nio.ByteBuffer;
import java.util.UUID;

public class DecoderUtil {
    private DecoderUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static Matrix3d asTransformation(int transformation) {
        double[] out = new double[9];
        for (int i = out.length - 1; i >= 0; i--) {
            out[i] = (transformation & 0x3) - 1D;
            transformation = transformation >> 2;
        }
        return new Matrix3d().set(out);
    }

    public static int asInteger(Matrix3d transformation) {
        double[] values = transformation.get(new double[9]);
        int out = 0;
        for (int i = 0; i < values.length; i++) {
            int rounded = (int) values[i];
            if (rounded > 1 || rounded < -1) {
                throw new IllegalArgumentException("Unsupported transformation value at index: " + i);
            }
            int value = rounded + 1;
            out = out << 2;
            out |= value;
        }
        return out;
    }
}
