package server;

import server.manager.LoginManager;

import java.sql.SQLException;

public class ServerBackend {

    private DatabaseConnection databaseConnection;
    private LoginManager loginManager;

    public ServerBackend() throws SQLException {
        // Verbindung zur Datenbank erstellen
        this.databaseConnection = new DatabaseConnection();
        // Login-Manager erstellen
        this.loginManager = new LoginManager(this.databaseConnection);
    }

    public boolean login(String username, String password) {
        // Login-Credentials überprüfen
        return this.loginManager.checkLoginCredentials(username, password);
    }

    public void createUser(String username, String password) {
        loginManager.createUser(username, password);
    }

    public void deleteUser(String username) {
        loginManager.deleteUser(username);
    }

    public String createSession(String username) {
        return this.loginManager.createSession(username);
    }

    public void joinSession(String username, String sessionID) {
        loginManager.joinSession(username, sessionID);
    }

    public void leaveSession(String username) {
        loginManager.leaveSession(username);
    }

}
