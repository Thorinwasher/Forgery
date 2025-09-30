package dev.thorinwasher.forgery.inventory;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ForgingItem(@Nullable ForgingMaterial material, ForgingSteps steps, double temperature,
                          long temperatureTimeStamp) {


    public static ForgingItem materialBased(ForgingMaterial material) {
        return new ForgingItem(material, new ForgingSteps(List.of()), 25D, TimeProvider.time());
    }

    public static ForgingItem materialBased(ForgeryKey key) {
        return materialBased(new ForgingMaterial(key));
    }

    public Double calculatedTemperature() {
        return Math.max(25D, temperature - (TimeProvider.time() - temperatureTimeStamp) / 5D);
    }
}
