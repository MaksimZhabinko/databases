package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertIntoTable {
    public static void insert() {
        try (Connection connection = Repository.getConnectionH2();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO person VALUES (1, 1000);" +
                     "INSERT INTO person VALUES (2, 1000);");) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
