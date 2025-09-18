package dev.thorinwasher.forgery.forging;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ForgingStep(Map<ForgingStepProperty<?>, Object> properties) {


    public <T> @NotNull T getOrThrow(ForgingStepProperty<T> property) {
        T t = (T) properties.get(property);
        Preconditions.checkState(t != null, "Expected the property to exist");
        return t;
    }

    public <T> @NotNull T getOrDefault(ForgingStepProperty<T> property, @NotNull T defaultValue) {
        Preconditions.checkNotNull(defaultValue);
        T t = (T) properties.get(property);
        if (t == null) {
            t = defaultValue;
        }
        return t;
    }
}
