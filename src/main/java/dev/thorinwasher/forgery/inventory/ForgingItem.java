package dev.thorinwasher.forgery.inventory;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.forging.TemperatureData;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ForgingItem(@Nullable ForgingMaterial material, ForgingSteps steps,
                          @Nullable TemperatureData temperatureData) {


    public static ForgingItem materialBased(ForgingMaterial material) {
        return new ForgingItem(material, new ForgingSteps(List.of()), null);
    }

    public static ForgingItem materialBased(ForgeryKey key) {
        return materialBased(new ForgingMaterial(key));
    }

    public Double calculatedTemperature() {
        if (temperatureData == null) {
            return 25D;
        }
        return temperatureData.behavior().computeNewTemperature(temperatureData.temperature(), temperatureData.timestamp());
    }
}
