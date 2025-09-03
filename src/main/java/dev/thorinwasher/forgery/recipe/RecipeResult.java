package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import org.bukkit.inventory.ItemStack;

public record RecipeResult(Data data, boolean overrideLore) {

    interface Data {

        ItemStack get();
    }

    public class DataBased implements Data {

        private final ItemStack itemStack;

        public DataBased(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public ItemStack get() {
            return itemStack.clone();
        }
    }

    public record PluginItem(ForgingMaterial material) implements Data {
        @Override
        public ItemStack get() {
            
        }
    }
}
