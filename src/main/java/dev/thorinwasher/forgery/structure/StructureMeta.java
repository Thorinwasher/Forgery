package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record StructureMeta<T>(Key key, TypeToken<T> token) implements Keyed {

    public StructureMeta(Key key, Type type) {
        this(key, (TypeToken<T>) TypeToken.get(type));
    }

    public static final StructureMeta<Map<String, ForgeryInventory.Behavior>> INVENTORIES = new StructureMeta<>(
            Forgery.key("inventories"),
            TypeFactory.parameterizedClass(Map.class, String.class, ForgeryInventory.Behavior.class)
    );

    public static final StructureMeta<List<BlockTransform>> BLOCK_TRANSFORMS = new StructureMeta<>(
            Forgery.key("block_transforms"),
            TypeFactory.parameterizedClass(List.class, BlockTransform.class)
    );

    public static final StructureMeta<Integer> HEAT_RESULT = new StructureMeta<>(
            Forgery.key("heat_result"),
            Integer.class
    );

    public static final StructureMeta<Set<String>> PROCESS_PARAMETERS = new StructureMeta<>(
            Forgery.key("process_parameters"),
            TypeFactory.parameterizedClass(Set.class, String.class)
    );

    public static final StructureMeta<String> OUTPUT_INVENTORY = new StructureMeta<>(
            Forgery.key("output_inventory"),
            String.class
    );

    public static final StructureMeta<Map<String, List<Condition>>> STATE = new StructureMeta<>(
            Forgery.key("states"),
            TypeFactory.parameterizedClass(Map.class, String.class, TypeFactory.parameterizedClass(List.class, Condition.class))
    );

    public static final StructureMeta<List<InventoryTransform>> INVENTORY_TRANSFORMS = new StructureMeta<>(
            Forgery.key("inventory_transforms"),
            TypeFactory.parameterizedClass(List.class, InventoryTransform.class)
    );

    public static final StructureMeta<Map<String, StructureToolInputBehavior>> TOOL_INPUT = new StructureMeta<>(
            Forgery.key("tool_input"),
            TypeFactory.parameterizedClass(Map.class, String.class, StructureToolInputBehavior.class)
    );
}
