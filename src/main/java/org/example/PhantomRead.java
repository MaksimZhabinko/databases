package org.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class PhantomRead {
    private static final int ISOLATION_LEVEL = Connection.TRANSACTION_READ_COMMITTED;
    private static final int SOLUTION = Connection.TRANSACTION_SERIALIZABLE;

    public static void main(String[] args) {
        ResultSet resultSetOne = null;
        ResultSet resultSetTwo = null;
        try(Connection connection = Repository.getConnection();
            Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(ISOLATION_LEVEL); //here solution

            resultSetOne = statement.executeQuery("SELECT count(*) FROM person");
            while (resultSetOne.next()) {
                int count = resultSetOne.getInt(1);
                System.out.println("Count: " + count);
            }

            new OtherTransaction().start();
            Thread.sleep(2000);

            resultSetTwo = statement.executeQuery("SELECT count(*) FROM person");
            while (resultSetTwo.next()) {
                int count = resultSetTwo.getInt(1);
                System.out.println("Count: " + count);
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(resultSetOne)) {
                try {
                    resultSetOne.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (Objects.nonNull(resultSetTwo)) {
                try {
                    resultSetTwo.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class OtherTransaction extends Thread {
        @Override
        public void run() {
            try (Connection connection = Repository.getConnection();
                 Statement statement = connection.createStatement()) {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(ISOLATION_LEVEL);

                statement.executeUpdate("INSERT INTO person(id, balance) values (3, 1000)");
                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
