package dev.thorinwasher.forgery.structure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.util.Pair;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StructureReader {
    private static final Pattern SCHEM_PATTERN = Pattern.compile("\\.json", Pattern.CASE_INSENSITIVE);

    public static ForgeryStructure fromInternalResourceJson(String string) throws IOException, StructureReadException {
        URL url = Forgery.class.getResource(string);
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new StructureReadException(e);
        }
        try {
            return fromJson(Paths.get(uri));
        } catch (FileSystemNotFoundException e) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
                return fromJson(fileSystem.getPath(uri.toString().split("!")[1]));
            }
        }
    }

    public static ForgeryStructure fromJson(Path path) throws IOException, StructureReadException {
        try (Reader reader = new InputStreamReader(new BufferedInputStream(Files.newInputStream(path)))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String schemFileName = jsonObject.get("schem_file").getAsString();
            Path schemFile = path.resolveSibling(schemFileName);
            String schemName = SCHEM_PATTERN.matcher(path.getFileName().toString()).replaceAll("");
            Schematic schematic = new SchematicReader().read(schemFile);
            Map<StructureMeta<?>, Object> structureMeta = jsonObject.get("meta").getAsJsonObject()
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        StructureMeta<?> meta = ForgeryRegistry.STRUCTURE_META.get(Key.key(Forgery.NAMESPACE, entry.getKey()));
                        if (meta == null) {
                            throw new StructureReadException("Unknown meta: " + entry.getKey());
                        }
                        Object value = meta.deserializer().apply(entry.getValue());
                        return new Pair<>(meta, value);
                    })
                    .collect(Collectors.toMap(Pair::first, Pair::second));
            return new ForgeryStructure(schematic, schemName, structureMeta);
        }
    }
}
