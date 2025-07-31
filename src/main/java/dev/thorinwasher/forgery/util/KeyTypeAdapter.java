package dev.thorinwasher.forgery.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.thorinwasher.forgery.Forgery;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public class KeyTypeAdapter extends TypeAdapter<Key> {
    @Override
    public void write(JsonWriter out, Key value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public Key read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return NamespacedKey.fromString(in.nextString(), Forgery.instance());
    }
}
