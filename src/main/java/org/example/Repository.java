package org.example;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Repository {

    public static Connection getConnection() {
        try {
            final String url = "jdbc:postgresql://localhost:5432/isolation";
            final String user = "postgres";
            final String passwd = "1111";
            return DriverManager.getConnection(url, user, passwd);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static Connection getConnectionH2() {
        try {
            final String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}
