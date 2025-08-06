package dev.thorinwasher.forgery.database;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class SqlStatements {

    private final String folder;
    private final Map<Type, String> statements;

    public SqlStatements(String folder) {
        this.folder = folder;
        this.statements = readStatements();
    }

    public Map<Type, String> readStatements() {
        ImmutableMap.Builder<Type, String> builder = new ImmutableMap.Builder<>();
        for (Type type : Type.values()) {
            try (InputStream inputStream = SqlStatements.class.getResourceAsStream(type.path(folder))) {
                if (inputStream == null) {
                    continue;
                }
                String statement = new String(inputStream.readAllBytes());
                builder.put(type, statement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.build();
    }

    public @NotNull String get(Type type) {
        if (!statements.containsKey(type)) {
            throw new IllegalArgumentException("Statement does not exists.");
        }
        return statements.get(type);
    }


    public enum Type {
        DELETE,
        UPDATE,
        INSERT,
        FIND;

        public String path(String directoryPath) {
            return directoryPath + "/" + name().toLowerCase(Locale.ROOT) + ".sql";
        }
    }
}
