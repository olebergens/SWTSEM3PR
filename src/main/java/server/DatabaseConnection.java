package server;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class DatabaseConnection {

    private Connection conn;
    public DatabaseConnection(String url, String username, String password) throws SQLException {
        // Verbindung zur Datenbank herstellen
        this.conn = DriverManager.getConnection(url, username, password);
    }

    public DatabaseConnection() {
        try {
            // JDBC-Treiber laden
            Class.forName("com.mysql.jdbc.Driver");
            // Verbindung zur Datenbank herstellen
            new DatabaseConnection("jdbc:mysql://localhost/mydb", "username", "password");
        }catch (ClassNotFoundException | SQLException e) {
            // Fehler beim Herstellen der Verbindung zur Datenbank
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return this.conn;
    }

    public void createUser(String username, String password) {
        // SQL-Abfrage für das Erstellen eines neuen Benutzers erstellen
        String sql = "INSERT INTO users (username,password) VALUES (?,?)";
        try (PreparedStatement statement = this.conn.prepareStatement(sql)){
            // Benutzername und Passwort an die SQL-Abfrage übergeben
            statement.setString(1, username);
            statement.setString(2, password);
            // Abfrage ausführen
            statement.executeUpdate();
        }catch (SQLException e) {
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
        }
    }

    public User getUser(String username) {
        // SQL-Abfrage für das Abrufen von Benutzerdaten erstellen
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = this.conn.prepareStatement(sql)){
            // Benutzername an die SQL-Abfrage übergeben
            statement.setString(1, username);
            // Abfrage ausführen und Ergebnis abrufen
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                // Benutzerdaten aus dem ResultSet in ein neues User-Objekt übertragen
                User user = new User();
                user.setUsername(result.getString("username"));
                user.setPassword(result.getString("password"));
                return user;
            } else return null; // Kein Benutzer mit angegebenen Benutzernamen gefunden
        }catch (SQLException e) {
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
            return null;
        }
    }

    public void updateUser(String username, String password){
        // SQL-Abfrage für das Aktualisieren von Benutzerdaten erstellen
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement statement = this.conn.prepareStatement(sql)){
            // Neues Passwort und Benutzername an die SQL-Abfrage übergeben
            statement.setString(1, password);
            statement.setString(2, username);
            // Abfrage ausführen
            statement.executeUpdate();
        }catch (SQLException e) {
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
        }
    }

    public void deleteUser(String username) {
        // SQL-Abfrage für das Löschen von Benutzerdaten erstellen
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement statement = this.conn.prepareStatement(sql)){
            // Benutzername an die SQL-Abfrage übergeben
            statement.setString(1, username);
            // Abfrage ausführen
            statement.executeUpdate();
        }catch (SQLException e) {
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
        }
    }

    public String createSession(String username) {
        // Eindeutige Session-ID erstellen (per UUID)
        String sessionID = UUID.randomUUID().toString();
        // SQL-Abfrage für das Erstellen einer neuen Session erstellen
        String sql = "INSERT INTO session (id, username) VALUES (?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)){
            // Session-ID und Benutzername an die SQL-Abfrage übergeben
            statement.setString(1, sessionID);
            statement.setString(2, username);
            // Abfrage ausführen
            statement.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionID;
    }

    public void joinSession(String username, String sessionID) {
        // SQL-Abfrage für das Einloggen in eine bestehende Session erstellen
        String sql = "UPDATE sessions SET username = ? WHERE id = ?";

        try (PreparedStatement statement = this.conn.prepareStatement(sql)) {
            // Benutzername und Session-ID an die SQL-Abfrage übergeben
            statement.setString(1, username);
            statement.setString(2, sessionID);

            // Abfrage ausführen
            statement.executeUpdate();
        }catch (SQLException e) {
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
        }
    }

    public void leaveSession(String username) {
        // SQL-Abfrage für das Ausloggen aus der aktuellen Session erstellen
        String sql = "UPDATE session SET username = NULL WHERE username = ?";

        try (PreparedStatement statement = this.conn.prepareStatement(sql)){
            // Benutzername an die SQL-Abfrage übergeben
            statement.setString(1, username);
            // Abfrage ausführen
            statement.executeUpdate();
        }catch (SQLException e){
            // Fehler bei der Ausführung der SQL-Abfrage
            e.printStackTrace();
        }
    }

}
