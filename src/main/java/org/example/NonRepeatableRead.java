package org.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class NonRepeatableRead {
    private static final int ISOLATION_LEVEL = Connection.TRANSACTION_READ_COMMITTED;
    private static final int SOLUTION = Connection.TRANSACTION_REPEATABLE_READ;

    public static void main(String[] args) {
        ResultSet resultSetOne = null;
        ResultSet resultSetTwo = null;
        try (Connection connection = Repository.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(ISOLATION_LEVEL); //here solution

            resultSetOne = statement.executeQuery("SELECT * FROM person WHERE id = 1");
            while (resultSetOne.next()) {
                String balance = resultSetOne.getString("balance");
                System.out.println("[one] Balance: " + balance);
            }

            new OtherTransaction().start();
            Thread.sleep(2000);

            resultSetTwo = statement.executeQuery("SELECT * FROM person WHERE id = 1");
            while (resultSetTwo.next()) {
                final String balance = resultSetTwo.getString("balance");
                System.out.println("[one] Balance: " + balance);
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
            ResultSet resultSetTwo = null;
            try (Connection connection = Repository.getConnection();
                 Statement statement = connection.createStatement()) {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(ISOLATION_LEVEL);

                statement.executeUpdate("UPDATE person SET balance = 100000 WHERE id = 1");
                connection.commit();

                resultSetTwo = statement.executeQuery("SELECT * FROM person WHERE id = 1");
                while (resultSetTwo.next()) {
                    String balance = resultSetTwo.getString("balance");
                    System.out.println("[two] Balance: " + balance);
                }

                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (Objects.nonNull(resultSetTwo)) {
                    try {
                        resultSetTwo.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
