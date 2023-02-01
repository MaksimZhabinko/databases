package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LostUpdate {
    public static final String READ = "SELECT person.balance FROM person WHERE id = ?";
    public static final String UPDATE = "UPDATE person SET balance = ? WHERE id = ?";
    private static final int ISOLATION_LEVEL = Connection.TRANSACTION_READ_COMMITTED;
    private static final int SOLUTION = Connection.TRANSACTION_REPEATABLE_READ;

    public static void main(String[] args) {
        try {
            CreateTable.create();
            InsertIntoTable.insert();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection connectionOne = getNewConnection();
             Connection connectionTwo = getNewConnection();
             PreparedStatement updateOne = connectionOne.prepareStatement(UPDATE);
             PreparedStatement updateTwo = connectionTwo.prepareStatement(UPDATE)) {

            long balanceOne = getBalance(connectionOne);
            long balanceTwo = getBalance(connectionTwo);

            updateOne.setLong(1, balanceOne + 10);
            updateOne.setLong(2, 1);
            updateOne.execute();

            connectionOne.commit();
            connectionOne.close();

            updateTwo.setLong(1, balanceTwo + 5);
            updateTwo.setLong(2, 1);
            updateTwo.execute();

            connectionTwo.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getNewConnection() throws SQLException {
        Connection connection = Repository.getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(ISOLATION_LEVEL); //here solution
        return connection;
    }

    private static long getBalance(Connection connectionOne) {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connectionOne.prepareStatement(READ)) {
            preparedStatement.setLong(1, 1);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(resultSet)) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
