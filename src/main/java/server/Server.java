package server;

import org.json.JSONException;
import org.json.JSONObject;
import server.manager.LoginManager;
import server.manager.PermissionManager;
import server.packagehandle.PackageData;
import server.packagehandle.PackageHandler;
import server.pipe.MessagePipe;
import server.database.DatabaseConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Server {

    public static DatabaseConnection connection;

    public static void main(String[] args) throws SQLException, IOException {
        // Erstellen einer Datenbank-Verbindung
        connection = new DatabaseConnection("", "", "");
        // Erstellen eines neuen Servers auf dem angegebenen Port
        ServerSocket socket = new ServerSocket(3333);
        if (connection.getConnection() == null) {
            System.err.println("Failed to connect to database!");
            return;
        }
        // Erstellen des Login-Managers
        LoginManager loginManager = new LoginManager(connection);
        // Initialisieren anderer Klassen
        MessagePipe messagePipe = MessagePipe.getInstance();
        PermissionManager permissionManager = PermissionManager.getInstance();
        System.out.println("Server is running..");
        System.out.println("Press any key to stop the server..");

        while (true) {
            Socket clientSocket = socket.accept();
            // Erstellen eines neuen Threads für jeden verbundenen Client
            new Thread(() -> {
                // Aufrufen der handleClient-Methode, um die Anfragen des Clients zu verarbeiten
                handleClient(clientSocket);
            }).start();
        }
    }

    public static void handleClient(Socket clientSocket) {
        // Verarbeiten von Anfragen des Clients
        try {
            // Erstellen eines BufferedReader zum Empfangen von Nachrichten vom Client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Erstellen eines PrintWriter zum Senden von Nachrichten an den Client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            // Verarbeiten der Anfragen des Clients
            while (true) {
                // Empfangen einer Nachricht vom Client
                String message = in.readLine();
                // Verarbeiten der Nachricht
                if (message != null) {
                    // Parsen der Nachricht als JSON-Objekt
                    JSONObject data = new JSONObject(message);
                    // Verarbeiten des Pakets
                    PackageData packageData = null;
                    try {
                        String type = data.getString("type");
                        JSONObject dataJSON = data.getJSONObject("data");
                        packageData = new PackageData(type, dataJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String response = PackageHandler.processPackage(packageData, clientSocket);
                    // Senden der Antwort an den Client
                    out.println(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeUser(Socket clientSocket, String message) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
