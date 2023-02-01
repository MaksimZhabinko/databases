package org.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DirtyRead {
    private static final int ISOLATION_LEVEL = Connection.TRANSACTION_READ_UNCOMMITTED;
    private static final int SOLUTION = Connection.TRANSACTION_READ_COMMITTED;

    public static void main(String[] args) {
        try {
            CreateTable.create();
            InsertIntoTable.insert();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = Repository.getConnectionH2();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(ISOLATION_LEVEL);

            statement.executeUpdate("UPDATE PERSON SET balance = 100000 WHERE id = 1");

            new OtherTransaction().start();
            Thread.sleep(2000);
            connection.rollback();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class OtherTransaction extends Thread {
        @Override
        public void run() {
            ResultSet resultSet = null;
            try (Connection connection = Repository.getConnectionH2();
                 Statement statement = connection.createStatement()) {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(ISOLATION_LEVEL); // here solution

                resultSet = statement.executeQuery("SELECT * FROM PERSON WHERE id = 1");
                while (resultSet.next()) {
                    System.out.println("Balance: " + resultSet.getString("balance"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
