package dev.thorinwasher.forgery.inventory;

import com.google.gson.JsonParser;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ForgingMaterialPersistentDataType implements PersistentDataType<String, ForgingMaterial> {

    public static final ForgingMaterialPersistentDataType INSTANCE = new ForgingMaterialPersistentDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ForgingMaterial> getComplexType() {
        return ForgingMaterial.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull ForgingMaterial complex, @NotNull PersistentDataAdapterContext context) {
        return complex.asJson().toString();
    }

    @Override
    public @NotNull ForgingMaterial fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return ForgingMaterial.fromJson(JsonParser.parseString(primitive)).get();
    }
}
