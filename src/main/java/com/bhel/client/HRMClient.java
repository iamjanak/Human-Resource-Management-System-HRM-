
package com.bhel.client;

import com.bhel.remote.HRMService;
import java.rmi.Naming;

public class HRMClient {

    private HRMService service;

    public HRMClient() {

        try {
            service = (HRMService) Naming.lookup(
                    "rmi://localhost:1099/HRMService"
            );

            System.out.println("Connected to BHEL HRM Server.");

        } catch (Exception e) {
            System.out.println("Unable to connect to HRM Server.");
            e.printStackTrace();
        }
    }

    public String login(String username, String password, String role) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.login(username, password, role);

        } catch (Exception e) {
            return "Login request failed: " + e.getMessage();
        }
    }
}
