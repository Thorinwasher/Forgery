package dev.thorinwasher.forgery.inventory;

import dev.thorinwasher.forgery.serialize.Serialize;
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
        return Serialize.asJson(ForgingMaterial.class, complex)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material data"));
    }

    @Override
    public @NotNull ForgingMaterial fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return Serialize.fromJson(ForgingMaterial.class, primitive)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material data"));
    }
}
