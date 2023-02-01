package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateTable {
    public static void create() {
        try (Connection connection = Repository.getConnectionH2();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE person\n" +
                     "(\n" +
                     "    id      BIGINT NOT NULL PRIMARY KEY,\n" +
                     "    balance BIGINT NOT NULL\n" +
                     ");")) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
