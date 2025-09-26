package dev.thorinwasher.forgery.util;

import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.recipe.ItemReference;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemPresets {

    public static void saveAllIfNotExists(Map<Key, ItemReference> itemReferences, PersistencyAccess access) {
        saveItemReferenceIfNotExists(new ItemReference("pig_iron", pigIron()), itemReferences, access);
        saveItemReferenceIfNotExists(new ItemReference("pig_iron_nugget", pigIronNugget()), itemReferences, access);
        saveItemReferenceIfNotExists(new ItemReference("hammer", hammer()), itemReferences, access);
    }

    private static ItemStack hammer() {
        ItemStack hammer = new ItemStack(Material.MACE);
        hammer.unsetData(DataComponentTypes.WEAPON);
        hammer.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes()
                .build());
        return hammer;
    }


    private static void saveItemReferenceIfNotExists(ItemReference itemReference, Map<Key, ItemReference> itemReferences, PersistencyAccess access) {
        if (!itemReferences.containsKey(itemReference.getKey())) {
            itemReferences.put(itemReference.getKey(), itemReference);
            access.database().insert(access.itemStoredData(), itemReference);
        }
    }

    private static ItemStack pigIron() {
        ItemStack pigIron = new ItemStack(Material.IRON_INGOT);
        pigIron.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Pig iron ingot")
                .decoration(TextDecoration.ITALIC, false));
        return pigIron;
    }

    private static ItemStack pigIronNugget() {
        ItemStack pigIron = new ItemStack(Material.IRON_NUGGET);
        pigIron.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Pig iron nugget")
                .decoration(TextDecoration.ITALIC, false));
        return pigIron;
    }
}
