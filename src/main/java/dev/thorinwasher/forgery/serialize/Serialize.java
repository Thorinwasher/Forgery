package dev.thorinwasher.forgery.serialize;

import com.google.gson.JsonElement;
import dev.thorinwasher.forgery.forging.ForgingIngredients;
import dev.thorinwasher.forgery.forging.ForgingStep;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.forging.ToolInput;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.structure.BlockTransform;
import dev.thorinwasher.forgery.structure.Condition;
import dev.thorinwasher.forgery.structure.KeyedSerializer;
import dev.thorinwasher.forgery.util.Duration;
import dev.thorinwasher.forgery.util.ForgeryKey;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.Optional;

public class Serialize {

    private final static GsonConfigurationLoader.Builder GSON_CONFIGURATION_LOADER_BUILDER = GsonConfigurationLoader.builder()
            .defaultOptions(Serialize::registerDefaultOptions);

    private static ConfigurationOptions registerDefaultOptions(ConfigurationOptions configurationOptions) {
        return configurationOptions.serializers(Serialize::registerSerializers);
    }

    public static void registerSerializers(TypeSerializerCollection.Builder builder) {
        builder.register(ForgingSteps.class, new ForgingStepsSerializer())
                .register(ForgingStep.class, new ForgingStepSerializer())
                .register(ForgingIngredients.class, new ForgingIngredientsSerializer())
                .register(ToolInput.class, new ToolInputSerializer())
                .register(ForgingItem.class, new ForgingItemSerializer())
                .register(ForgingMaterial.class, new ForgingMaterialSerializer())
                .register(ForgeryKey.class, new ForgeryKeySerializer())
                .register(BlockType.class, new KeyedSerializer<>(RegistryKey.BLOCK))
                .register(BlockData.class, new BlockDataSerializer())
                .register(BlockTransform.class, new BlockTransformSerializer())
                .register(Condition.class, new ConditionSerializer())
                .register(Duration.class, new DurationSerializer())
                .register(ItemType.class, new KeyedSerializer<>(RegistryKey.ITEM))
                .register(Transformation.class, new TransformationSerializer())
                .register(Vector3f.class, new Vector3fSerializer())
                .register(Quaternionf.class, new QuaternionfSerializer());
    }

    public static <T> Optional<String> asJson(TypeToken<T> token, T value) {
        try {
            return Optional.of(
                    GSON_CONFIGURATION_LOADER_BUILDER.build().createNode().set(token, value).get(JsonElement.class).toString()
            );
        } catch (ConfigurateException e) {
            return Optional.empty();
        }
    }

    public static <T> Optional<String> asJson(Class<T> tClass, T value) {
        return asJson(TypeToken.get(tClass), value);
    }

    public static <T> Optional<T> fromJson(TypeToken<T> token, String json) {
        try {
            return Optional.ofNullable(
                    GSON_CONFIGURATION_LOADER_BUILDER.buildAndLoadString(json)
                            .get(token)
            );
        } catch (ConfigurateException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> Optional<T> fromJson(Class<T> tClass, String json) {
        return fromJson(TypeToken.get(tClass), json);
    }

    public static <T, U> Optional<T> convert(U u, TypeToken<T> tType) {
        try {
            return Optional.ofNullable(GSON_CONFIGURATION_LOADER_BUILDER.build().createNode().set(u).get(tType));
        } catch (SerializationException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
