package dev.thorinwasher.forgery.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ForgingMaterial(@Nullable ForgeryKey key, int score) {

    public ForgingMaterial(ForgeryKey key) {
        this(key, 10);
    }

    public static Optional<ForgingMaterial> fromJson(JsonElement jsonElement) {
        if (!(jsonElement instanceof JsonObject jsonObject)) {
            return Optional.empty();
        }
        ForgeryKey key;
        if (jsonObject.has("key")) {
            key = ForgeryKey.forgery(jsonObject.get("key").getAsString());
        } else {
            key = null;
        }
        int score;
        if (jsonObject.has("score")) {
            score = jsonObject.get("score").getAsInt();
        } else {
            score = 10;
        }
        return Optional.of(new ForgingMaterial(key, score));
    }

    public boolean providesExtraData() {
        return (key != null && key.namespace().equals(Forgery.NAMESPACE)) || score != 10;
    }

    public JsonObject asJson() {
        JsonObject output = new JsonObject();
        if (key != null) {
            output.add("key", new JsonPrimitive(key.asString()));
        }
        if (score != 10) {
            output.add("score", new JsonPrimitive(score));
        }
        return output;
    }
}
