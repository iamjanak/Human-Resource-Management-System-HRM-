
package com.bhel.server;

import com.bhel.remote.HRMService;
import com.bhel.database.DBConnection;
import com.bhel.security.PasswordUtil;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.concurrent.CountDownLatch;

public class HRMServer extends UnicastRemoteObject
        implements HRMService {

    public HRMServer() throws RemoteException {
        super();
    }

    @Override
    public String login(String username, String password, String role)
            throws RemoteException {

        String selectedRole;

        if ("Admin / HR Staff".equalsIgnoreCase(role)
                || "ADMIN_HR".equalsIgnoreCase(role)) {

            selectedRole = "ADMIN_HR";

        } else if ("Employee".equalsIgnoreCase(role)
                || "EMPLOYEE".equalsIgnoreCase(role)) {

            selectedRole = "EMPLOYEE";

        } else {
            return "Invalid selected role.";
        }

        String sql = """
                SELECT password_hash, role
                FROM users
                WHERE username = ? AND is_active = TRUE
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String storedHash =
                            result.getString("password_hash");

                    String storedRole =
                            result.getString("role");

                    boolean passwordValid =
                            PasswordUtil.verifyPassword(
                                    password, storedHash
                            );

                    if (passwordValid
                            && selectedRole.equals(storedRole)) {

                        return "Login successful!";
                    }
                }

                return "Invalid username, password, or selected role.";
            }

        } catch (Exception e) {

            e.printStackTrace();

            return "Login service is temporarily unavailable.";
        }
    }

    public static void main(String[] args) {

        try {

            HRMServer server = new HRMServer();

            LocateRegistry.createRegistry(1099);

            Naming.rebind(
                    "rmi://localhost:1099/HRMService",
                    server
            );

            System.out.println(
                    "BHEL HRM RMI Server is running..."
            );

            System.out.println(
                    "Waiting for client requests..."
            );

            // Keep the server running continuously
            new CountDownLatch(1).await();

        } catch (Exception e) {

            System.out.println(
                    "Server error: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
