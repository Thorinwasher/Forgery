package dev.thorinwasher.forgery.structure;

import com.google.common.base.Preconditions;

public sealed interface Condition {

    record InventoryEmptyCondition(String inventoryTypeName) implements Condition {
    }

    record StructureAgeCondition(long age) implements Condition {
    }

    record InvertedCondition(Condition condition) implements Condition {

        public InvertedCondition {
            Preconditions.checkArgument(!(condition instanceof InvertedCondition), "Double inverting a condition does nothing");
        }
    }
}
