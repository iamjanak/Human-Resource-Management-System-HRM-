
package com.bhel.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

import com.bhel.database.DBConnection;

public class CreateAdmin {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter Admin username: ");
            String username = scanner.nextLine();

            System.out.print("Enter Admin password: ");
            String password = scanner.nextLine();

            String passwordHash =
                    PasswordUtil.hashPassword(password);

            String sql = """
                    INSERT INTO users (username, password_hash, role)
                    VALUES (?, ?, 'ADMIN_HR')
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, passwordHash);

                statement.executeUpdate();

                System.out.println(
                        "Admin account created successfully!"
                );
            }

        } catch (Exception e) {
            System.out.println("Unable to create Admin account.");
            e.printStackTrace();
        }
    }
}
