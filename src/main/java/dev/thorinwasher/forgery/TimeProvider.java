package dev.thorinwasher.forgery;

import org.bukkit.Bukkit;

public class TimeProvider {

    public static long time() {
        // TODO: Temp
        return Bukkit.getWorlds().getFirst().getGameTime();
    }
}
