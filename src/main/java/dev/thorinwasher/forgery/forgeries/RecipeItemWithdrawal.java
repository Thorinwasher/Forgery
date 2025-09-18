package dev.thorinwasher.forgery.forgeries;

import java.util.UUID;

public record RecipeItemWithdrawal(String inventoryType, UUID structureUuid, int amount) {
}
