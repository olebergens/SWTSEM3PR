package server.manager;

import server.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginManager {
    private DatabaseConnection databaseConnection;

    public LoginManager(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public boolean checkLoginCredentials(String username, String password) {
        // SQL-Abfrage für die Überprüfung von Benutzernamen und Passwort erstellen
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            // Benutzername und Passwort an die SQL-Abfrage übergeben
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Abfrage ausführen und Ergebnis abrufen
            ResultSet result = stmt.executeQuery();
            result.next();
            int count = result.getInt(1);

            // Wenn die Abfrage einen Treffer ergab, war der Login erfolgreich
            return count > 0;
        } catch (SQLException e) {
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
            return false;
        }
    }

    public void createUser(String username, String password) {
        this.databaseConnection.createUser(username, password);
    }

    public void deleteUser(String username) {
        this.databaseConnection.deleteUser(username);
    }

    public void joinSession(String username, String sessionId) {
        this.databaseConnection.joinSession(username, sessionId);
    }

    public void leaveSession(String username) {
        this.databaseConnection.leaveSession(username);
    }

    public String createSession(String username) {
       return this.databaseConnection.createSession(username);
    }

    public void updateUser(String username, String password) {
        this.databaseConnection.updateUser(username, password);
    }

    public void getUser(String username) {
        this.databaseConnection.getUser(username);
    }
}
