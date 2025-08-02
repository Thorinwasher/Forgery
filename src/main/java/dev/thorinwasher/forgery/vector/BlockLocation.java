package dev.thorinwasher.forgery.vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

public record BlockLocation(int x, int y, int z, UUID worldUuid) {

    public BlockVector toVector() {
        return new BlockVector(x, y, z);
    }

    public static BlockLocation fromLocation(Location location) {
        return new BlockLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
    }

    public Block toBlock() {
        return Bukkit.getWorld(worldUuid).getBlockAt(x, y, z);
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(worldUuid), x, y, z);
    }
}
