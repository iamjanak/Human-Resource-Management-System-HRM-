
package com.bhel.database;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            System.out.println("Database connected successfully!");
            System.out.println("Connected Database: "
                    + connection.getCatalog());

        } catch (Exception e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
