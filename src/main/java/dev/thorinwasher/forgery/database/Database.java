package dev.thorinwasher.forgery.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.thorinwasher.forgery.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Database {

    private static final int FORGERY_DATABASE_VERSION = 0;
    private HikariDataSource hikariDataSource;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void init(File dataFolder) throws IOException, SQLException {
        HikariConfig config = getHikariConfigForSqlite(dataFolder);
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");
        this.hikariDataSource = new HikariDataSource(config);
        try (Connection connection = hikariDataSource.getConnection()) {
            createTables(connection);
        }
    }

    private static @NotNull HikariConfig getHikariConfigForSqlite(File dataFolder) throws IOException {
        File databaseFile = new File(dataFolder, "brewery.db");
        if (!databaseFile.exists() && !databaseFile.getParentFile().mkdirs() && !databaseFile.createNewFile()) {
            throw new IOException("Could not create file or dirs");
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SQLiteConnectionPool");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile);
        return hikariConfig;
    }

    private void createTables(Connection connection) throws SQLException {
        for (String statement : FileUtil.readInternalResource("/database/create_all_tables.sql").split(";")) {
            connection.prepareStatement(statement + ";").execute();
        }
        ResultSet resultSet = connection.prepareStatement(FileUtil.readInternalResource("/database/get_version.sql")).executeQuery();
        if (resultSet.next()) {
            int previousVersion = resultSet.getInt("version");
            resultSet.close();
            if (previousVersion < FORGERY_DATABASE_VERSION) {
                // Run migration
            } else if (previousVersion > FORGERY_DATABASE_VERSION) {
                throw new IllegalStateException("Can not downgrade Forgery!");
            }
        }
        PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/database/set_version.sql"));
        preparedStatement.setInt(1, FORGERY_DATABASE_VERSION);
        preparedStatement.execute();
    }

    public <T> CompletableFuture<Void> remove(StoredData<T, ?> dataType, T toRemove) {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.remove(toRemove, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public <T> CompletableFuture<Void> updateValue(UpdateableStoredData<T, ?> dataType, T newValue) {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.update(newValue, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public <T> CompletableFuture<Void> insertValue(StoredData<T, ?> dataType, T value) {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.insert(value, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public <T, U> CompletableFuture<List<T>> find(StoredData<T, U> dataType, U searchObject) {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                future.complete(dataType.find(searchObject, connection));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        }, executor);
        return future;
    }

    public CompletableFuture<Void> flush() {
        return CompletableFuture.runAsync(() -> {
        }, executor);
    }
}

