package dev.thorinwasher.forgery.listener;

import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.ItemIntegration;
import org.bukkit.block.Crafter;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.*;

public record CraftingListener(ItemAdapter adapter) implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onCraftEvent(CraftItemEvent event) {
        CraftingInventory inventory = event.getInventory();
        if (checkRecipe(event.getRecipe(), inventory.getMatrix())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrafterCraftEvent(CrafterCraftEvent event) {
        if (!(event.getBlock().getState() instanceof Crafter crafter)) {
            return;
        }
        ItemStack[] matrix = crafter.getInventory().getContents();
        if (checkRecipe(event.getRecipe(), matrix)) {
            event.setCancelled(true);
        }
    }

    private boolean checkRecipe(Recipe recipe, ItemStack[] matrix) {
        ItemIntegration forgery = adapter.registry().itemIntegration("forgery").get();
        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            for (ItemStack itemStack : matrix) {
                if (itemStack == null) {
                    continue;
                }
                if (forgery.toForgery(itemStack).isPresent() && shapelessRecipe.getChoiceList()
                        .stream()
                        .anyMatch(choice -> choice.test(itemStack) && choice instanceof RecipeChoice.MaterialChoice)
                ) {
                    return true;
                }
            }
            return false;
        }
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            for (ItemStack itemStack : matrix) {
                if (itemStack == null) {
                    continue;
                }
                if (forgery.toForgery(itemStack).isPresent() && shapedRecipe.getChoiceMap().values()
                        .stream()
                        .anyMatch(choice -> choice.test(itemStack) && choice instanceof RecipeChoice.MaterialChoice)
                ) {
                    return true;
                }
            }
        }
        return false;
    }
}
