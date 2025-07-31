package dev.thorinwasher.forgery.vector;

import org.bukkit.Location;

import java.util.UUID;

public record BlockLocation(int x, int y, int z, UUID worldUuid) {

    public BlockVector toVector() {
        return new BlockVector(x, y, z);
    }

    public static BlockLocation fromLocation(Location location) {
        return new BlockLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
    }
}
