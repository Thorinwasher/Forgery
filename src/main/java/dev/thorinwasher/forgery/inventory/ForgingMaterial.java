package dev.thorinwasher.forgery.inventory;

import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.jetbrains.annotations.Nullable;

public record ForgingMaterial(@Nullable ForgeryKey key, int score) {

    public ForgingMaterial(ForgeryKey key) {
        this(key, 10);
    }

    public boolean providesExtraData() {
        return (key != null && key.namespace().equals(Forgery.NAMESPACE)) || score != 10;
    }
}
