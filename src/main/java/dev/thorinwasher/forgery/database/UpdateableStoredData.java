package dev.thorinwasher.forgery.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface UpdateableStoredData<T, U> extends StoredData<T, U> {

    void update(T object, Connection connection) throws SQLException;
}
