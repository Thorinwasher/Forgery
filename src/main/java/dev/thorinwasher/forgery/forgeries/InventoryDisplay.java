package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class InventoryDisplay {
    private final ForgeryInventory inventory;
    private final List<Location> locations;
    private final ForgeryInventory.ItemDisplayBehavior displayBehavior;
    private final List<ItemDisplay> previouslyPopulated = new ArrayList<>();
    private final Transformation transformation;

    public InventoryDisplay(ForgeryInventory inventory, List<Location> locations,
                            ForgeryInventory.ItemDisplayBehavior displayBehavior, Transformation transformation) {
        this.inventory = inventory;
        this.locations = locations;
        this.displayBehavior = displayBehavior;
        this.transformation = transformation;
    }


    public void display(ItemAdapter itemAdapter) {
        if (locations.isEmpty()) {
            return;
        }
        clear();
        List<ForgingItem> items = inventory.items()
                .stream()
                .map(ForgeryInventory.ItemRecord::forgeryItem)
                .toList();
        int inventoryContents = items.size();
        int base = inventoryContents / locations.size();
        int extra = inventoryContents % locations.size();
        int i = 0;
        for (Location location : locations) {
            int amount;
            if (extra-- > 0) {
                amount = base + 1;
            } else {
                amount = base;
            }
            List<Location> displayLocations = amountDelta(amount)
                    .map(delta -> delta.add(displayBehavior.delta()))
                    .map(delta -> location.clone().add(delta))
                    .toList();
            for (Location displayLocation : displayLocations) {
                ForgingItem item = items.get(i++);
                ItemStack bukkit = itemAdapter.toBukkit(item);
                ItemDisplay itemDisplay = displayLocation.getWorld().spawn(displayLocation, ItemDisplay.class, display -> {
                    display.setPersistent(false);
                    display.setItemStack(bukkit);
                    display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    display.setTransformation(transformation);
                });
                previouslyPopulated.add(itemDisplay);
            }
        }
    }

    public Stream<Vector> amountDelta(int amount) {
        if (amount <= 0) {
            return Stream.of();
        }
        return switch (amount) {
            case 1 -> Stream.of(new Vector(0.5, 0, 0.5));
            case 2 -> Stream.of(new Vector(0.33, 0, 0.33), new Vector(0.66, 0, 0.66));
            case 3 -> Stream.of(
                    new Vector(0.25, 0, 0.25),
                    new Vector(0.75, 0, 0.25),
                    new Vector(0.5, 0, 0.75)
            );
            case 4 -> Stream.of(
                    new Vector(0.25, 0, 0.25),
                    new Vector(0.75, 0, 0.25),
                    new Vector(0.25, 0, 0.75),
                    new Vector(0.75, 0, 0.75)
            );
            case 5 -> Stream.of(
                    new Vector(0.20, 0, 0.20),
                    new Vector(0.80, 0, 0.20),
                    new Vector(0.20, 0, 0.80),
                    new Vector(0.80, 0, 0.80),
                    new Vector(0.5, 0, 0.5)
            );
            case 6 -> Stream.of(
                    new Vector(0.20, 0, 0.20),
                    new Vector(0.80, 0, 0.20),
                    new Vector(0.20, 0, 0.80),
                    new Vector(0.80, 0, 0.80),
                    new Vector(0.20, 0, 0.50),
                    new Vector(0.80, 0, 0.50)
            );
            case 7 -> Stream.of(
                    new Vector(0.20, 0, 0.20),
                    new Vector(0.80, 0, 0.20),
                    new Vector(0.20, 0, 0.80),
                    new Vector(0.80, 0, 0.80),
                    new Vector(0.20, 0, 0.50),
                    new Vector(0.80, 0, 0.50),
                    new Vector(0.5, 0, 0.5)
            );
            case 8 -> Stream.of(
                    new Vector(0.20, 0, 0.20),
                    new Vector(0.80, 0, 0.20),
                    new Vector(0.20, 0, 0.80),
                    new Vector(0.80, 0, 0.80),
                    new Vector(0.20, 0, 0.50),
                    new Vector(0.80, 0, 0.50),
                    new Vector(0.5, 0, 0.20),
                    new Vector(0.5, 0, 0.80)
            );
            default -> Stream.of(
                    new Vector(0.20, 0, 0.20),
                    new Vector(0.80, 0, 0.20),
                    new Vector(0.20, 0, 0.80),
                    new Vector(0.80, 0, 0.80),
                    new Vector(0.20, 0, 0.50),
                    new Vector(0.80, 0, 0.50),
                    new Vector(0.5, 0, 0.20),
                    new Vector(0.5, 0, 0.80),
                    new Vector(0.5, 0, 0.5)
            );
        };
    }

    public boolean needsRefresh() {
        return !previouslyPopulated.stream().allMatch(ItemDisplay::isValid);
    }

    public void clear() {
        previouslyPopulated.forEach(ItemDisplay::remove);
        previouslyPopulated.clear();
    }

}
