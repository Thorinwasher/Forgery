package dev.thorinwasher.forgery.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.serialize.Serialize;
import dev.thorinwasher.forgery.util.ForgeryKey;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ForgingItem(@Nullable ForgingMaterial material, ForgingSteps steps) {




    public static ForgingItem materialBased(ForgingMaterial material) {
        return new ForgingItem(material, new ForgingSteps(List.of()));
    }

    public static ForgingItem materialBased(ForgeryKey key) {
        return materialBased(new ForgingMaterial(key));
    }
}
