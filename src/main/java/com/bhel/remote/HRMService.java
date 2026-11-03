
package com.bhel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HRMService extends Remote {

    String login(String username, String password, String role)
            throws RemoteException;

}
