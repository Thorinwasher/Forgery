package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.util.PdcKeys;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Color;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
import java.util.Optional;

public enum HeatBehavior {

    IRON(1538D, 5, 0xFFFFFF),
    COPPER(1357D, 5, 0xCE702B);

    private final double meltingPoint;
    private final int decrementTime;
    private final int baseColor;

    private static final double PLANCK_CONSTANT = 6.626e-34;
    private static final double BOLTZMANN_CONSTANT = 1.380e-23;
    private static final double LIGHT_SPEED = 299792458D;
    // Arbitrary value representing the reflected light of the object
    private static final double NORMALIZATION_VALUE = 1e8;

    HeatBehavior(double meltingPoint, int decrementTime, int baseColor) {
        this.meltingPoint = meltingPoint;
        this.decrementTime = decrementTime;
        this.baseColor = baseColor;
    }

    public Color color(double temperature) {
        // Black body radiation
        double kelvin = temperature + 273.15;
        double intensityR = normalizedIntensityForWavelength(625e-9, kelvin);
        double intensityG = normalizedIntensityForWavelength(540e-9, kelvin);
        double intensityB = normalizedIntensityForWavelength(460e-9, kelvin);
        double fractionR = intensityR / (NORMALIZATION_VALUE + intensityR);
        double fractionG = intensityG / (NORMALIZATION_VALUE + intensityG);
        double fractionB = intensityB / (NORMALIZATION_VALUE + intensityB);
        double maxIntensity = Math.max(intensityR, Math.max(intensityG, intensityB));
        int bodyR = (int) (intensityR / maxIntensity * 255);
        int bodyG = (int) (intensityG / maxIntensity * 255);
        int bodyB = (int) (intensityB / maxIntensity * 255);
        int baseR = baseColor & 0xFF;
        int baseG = baseColor & 0xFF00;
        baseG = baseG >>> 8;
        int baseB = baseColor & 0xFF0000;
        baseB = baseB >>> 16;
        return Color.fromRGB(
                (int) Math.min(255, bodyR * fractionR + baseR * (1 - fractionR)),
                (int) Math.min(255, bodyG * fractionG + baseG * (1 - fractionG)),
                (int) Math.min(255, bodyB * fractionB + baseB * (1 - fractionB))
        );
    }

    private static double normalizedIntensityForWavelength(double wavelength, double kelvin) {
        double exponentContents = PLANCK_CONSTANT * LIGHT_SPEED / (wavelength * BOLTZMANN_CONSTANT * kelvin);
        double constant = 2 * PLANCK_CONSTANT * Math.pow(LIGHT_SPEED, 2) / Math.pow(wavelength, 5);
        return constant / (Math.exp(exponentContents) - 1);
    }

    public static Optional<HeatBehavior> fromItem(ItemStack itemStack) {
        String name = itemStack.getPersistentDataContainer().get(PdcKeys.HEAT_BEHAVIOR, PersistentDataType.STRING);
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(HeatBehavior.valueOf(name.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void applyTo(ItemStack item) {
        applyTo(item, 25D);
    }

    public void applyTo(ItemStack item, double temperature) {
        item.editPersistentDataContainer(pdc -> {
            pdc.set(PdcKeys.HEAT_BEHAVIOR, PersistentDataType.STRING, this.name().toLowerCase(Locale.ROOT));
            pdc.set(PdcKeys.TEMPERATURE, PersistentDataType.DOUBLE, temperature);
            pdc.set(PdcKeys.TIMESTAMP, PersistentDataType.LONG, TimeProvider.time());
        });
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                .addColor(color(temperature)).build()
        );
    }

    public static void updateInventory(Inventory inventory) {
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            Optional<HeatBehavior> heatOptional = fromItem(itemStack);
            PersistentDataContainerView pdcView = itemStack.getPersistentDataContainer();
            Double temperature = pdcView.get(PdcKeys.TEMPERATURE, PersistentDataType.DOUBLE);
            Long timestamp = pdcView.get(PdcKeys.TIMESTAMP, PersistentDataType.LONG);
            heatOptional.ifPresent(heatBehavior -> {
                if (temperature == null || timestamp == null) {
                    heatBehavior.applyTo(itemStack);
                    return;
                }
                double newTemperature = Math.max(25D, temperature - (double) (TimeProvider.time() - timestamp) / heatBehavior.decrementTime);
                heatBehavior.applyTo(itemStack, newTemperature);
            });
        }
    }
}
