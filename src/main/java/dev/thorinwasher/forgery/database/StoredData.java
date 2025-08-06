package dev.thorinwasher.forgery.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface StoredData<T, U> {

    List<T> find(U searchObject, Connection connection) throws SQLException;

    void insert(T object, Connection connection) throws SQLException;

    void remove(T object, Connection connection) throws SQLException;
}
