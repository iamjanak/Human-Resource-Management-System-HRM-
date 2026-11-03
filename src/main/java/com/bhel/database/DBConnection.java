
package com.bhel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:postgresql://localhost:5432/BHEL_HRM";

    private static final String USER = "postgres";

    // Replace this with your PostgreSQL installation password
    private static final String PASSWORD = "janak";

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}
