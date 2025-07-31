package dev.thorinwasher.forgery;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.thorinwasher.forgery.structure.ForgeryStructureType;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.util.KeyTypeAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForgeryRegistry<T extends Keyed> {
    public static final ForgeryRegistry<ForgeryStructureType> STRUCTURE_TYPES = fromJson("/structure_types.json", ForgeryStructureType.class);
    public static final ForgeryRegistry<StructureMeta<?>> STRUCTURE_META = (ForgeryRegistry<StructureMeta<?>>) fromFields(StructureMeta.class);

    private final ImmutableMap<Key, T> backing;

    private ForgeryRegistry(Collection<T> values) {
        ImmutableMap.Builder<Key, T> registryBacking = ImmutableMap.builder();
        values.forEach(value -> registryBacking.put(value.key(), value));
        this.backing = registryBacking.build();
    }

    public Collection<T> values() {
        return backing.values();
    }

    public @Nullable T get(Key key) {
        return backing.get(key);
    }

    public @Nullable T get(String key) {
        return backing.get(NamespacedKey.fromString(key, Forgery.instance()));
    }

    public boolean containsKey(Key key) {
        return backing.containsKey(key);
    }

    private static <T extends Keyed> ForgeryRegistry<? extends T> fromFields(Class<T> tClass) {
        try {
            List<T> tList = new ArrayList<>();
            for (Field field : tClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Object staticField = field.get(null);
                if (tClass.isInstance(staticField)) {
                    tList.add(tClass.cast(staticField));
                }
            }
            return new ForgeryRegistry<>(tList);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Keyed> ForgeryRegistry<T> fromJson(String path, Class<T> tClass) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Key.class, new KeyTypeAdapter()).create();
        try (
                InputStream inputStream = ForgeryRegistry.class.getResourceAsStream(path);
                InputStreamReader reader = new InputStreamReader(
                        Preconditions.checkNotNull(inputStream, "InputStream for path '" + path + "' cannot be null")
                )
        ) {
            Type listType = TypeToken.getParameterized(List.class, tClass).getType();
            List<T> tList = gson.fromJson(reader, listType);
            return new ForgeryRegistry<>(tList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON at path: " + path, e);
        }
    }
}
