package dev.thorinwasher.forgery.inventory;

import com.google.common.collect.ImmutableList;
import dev.thorinwasher.forgery.database.Database;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;

public class ForgeryInventory implements InventoryHolder {

    private final String typeName;
    private final Behavior behavior;
    private final ForgingItem[] contents;
    private final PersistencyAccess persistencyAccess;
    private final UUID structureUuid;
    private final ItemAdapter itemAdapter;
    private @Nullable Inventory inventory;

    public ForgeryInventory(Behavior behavior, String typeName, PersistencyAccess persistencyAccess, UUID structureUuid, ItemAdapter itemAdapter) {
        this.typeName = typeName;
        this.behavior = behavior;
        this.contents = new ForgingItem[behavior.size()];
        this.persistencyAccess = persistencyAccess;
        this.structureUuid = structureUuid;
        this.itemAdapter = itemAdapter;
    }

    public void setItem(ItemRecord item) {
        contents[item.pos()] = item.forgeryItem();
    }

    public void store(ForgingItem item, int position) {
        ForgingItem previous = contents[position];
        contents[position] = item;
        Database database = persistencyAccess.database();
        InventoryContentStoredData.ItemInfo itemInfo = new InventoryContentStoredData.ItemInfo(
                new ItemRecord(position, item),
                structureUuid,
                typeName
        );
        if (previous == null) {
            database.insert(persistencyAccess.inventoryContentStoredData(), itemInfo);
            return;
        }
        if (item == null) {
            database.remove(persistencyAccess.inventoryContentStoredData(), itemInfo);
            return;
        }
        database.update(persistencyAccess.inventoryContentStoredData(), itemInfo);
    }

    public String typeName() {
        return this.typeName;
    }

    public Behavior behavior() {
        return this.behavior;
    }

    public void updateInterfaceFromContents() {
        if (inventory == null) {
            return;
        }
        for (int i = 0; i < contents.length; i++) {
            ForgingItem item = contents[i];
            if (item == null) {
                inventory.setItem(i, null);
                continue;
            }
            inventory.setItem(i, itemAdapter.toBukkit(item));
        }
    }

    public boolean updateContentsFromInterface() {
        if (inventory == null) {
            return false;
        }
        boolean hasUpdated = false;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            ForgingItem item;
            if (itemStack == null) {
                item = null;
            } else {
                item = itemAdapter.toForgery(itemStack).orElse(null);
            }
            if (!Objects.equals(item, contents[i])) {
                hasUpdated = true;
            }
            store(item, i);
        }
        return hasUpdated;
    }

    @Override
    public @NotNull String toString() {
        return String.format("ForgeryInventory{%s}", String.join(", ", List.of("typeName=" + typeName, "behavior=" + behavior, "contents=" + Arrays.deepToString(contents))));
    }

    public @NotNull List<ItemRecord> items() {
        ImmutableList.Builder<ItemRecord> items = new ImmutableList.Builder<>();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                continue;
            }
            items.add(new ItemRecord(i, contents[i]));
        }
        return items.build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, behavior().size());
        }
        return this.inventory;
    }

    /**
     * @param forgeryItem
     * @return True if the item was added
     */
    public boolean addItem(ForgingItem forgeryItem) {
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                continue;
            }
            store(forgeryItem, i);
            return true;
        }
        return false;
    }

    public Optional<ForgingItem> retrieveFirstAndSave() {
        ForgingItem item = null;
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                item = contents[i];
                this.store(null, i);
                contents[i] = null;
                break;
            }
        }
        return Optional.ofNullable(item);
    }

    public void clear() {
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                this.store(null, i);
                contents[i] = null;
            }
        }
    }

    public boolean isFull() {
        return behavior.size() <= items().size();
    }

    public record ItemRecord(int pos, ForgingItem forgeryItem) {

    }

    @ConfigSerializable
    public record Behavior(AccessBehavior access, ItemDisplayBehavior itemDisplay, int size,
                           Set<BlockType> interfaceBlocks, Set<ItemType> allows,
                           @Nullable Transformation transformation) {
    }

    public enum AccessBehavior {
        OPENABLE,
        INSERTABLE
    }

    public enum ItemDisplayBehavior {
        UNDER, ABOVE, INSIDE, NONE;

        public Vector delta() {
            return switch (this) {
                case UNDER -> new Vector(0, -0.125, 0);
                case ABOVE -> new Vector(0, 1, 0);
                case INSIDE -> new Vector(0, 0.5, 0);
                case NONE -> new Vector();
            };
        }

        public Quaternionf leftRotation() {
            return switch (this) {
                case UNDER, ABOVE -> new AxisAngle4f((float) (Math.PI / 2), 1F, 0F, 0F)
                        .get(new Quaternionf());
                case INSIDE, NONE -> new Quaternionf();
            };
        }
    }
}
