package server.packagehandle;


import org.json.JSONException;
import org.json.JSONObject;
import server.DatabaseConnection;
import server.User;
import server.listener.IMessageListener;
import server.manager.LoginManager;
import server.pipe.MessagePipe;

import java.io.File;
import java.sql.ResultSet;

public class PackageHandler {

    private static MessagePipe messagePipe = MessagePipe.getInstance();
    private static DatabaseConnection databaseConnection;
    private static LoginManager loginManager;

    public PackageHandler(DatabaseConnection databaseConnection, LoginManager loginManager) {
        PackageHandler.databaseConnection = databaseConnection;
        PackageHandler.loginManager = loginManager;
    }

    public static boolean isValidPackage(JSONObject jsonObject) {
        // Überprüfen, ob die Anfrage ein gültiges Paket ist
        boolean isValid = false;
        try {
            // Überprüfen, ob das JSON-Objekt die erforderlichen Felder enthält
            if (jsonObject.has("type") && jsonObject.has("data")) {
                isValid = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    public static PackageData extractPackageData(String request) {
        PackageData packageData = null;
        try {
            // Parsen der Anfrage als JSON-Objekt
            JSONObject jsonObject = new JSONObject(request);
            // Extrahieren der Daten des Pakets
            String type = jsonObject.getString("type");
            JSONObject data = jsonObject.getJSONObject("data");
            packageData = new PackageData(type, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return packageData;
    }

    public static String processPackage(PackageData packageData) {
        // Verarbeiten des Pakets je nach Typ
        return switch (packageData.type) {
            case "login" -> processLoginPackage(packageData.data);
            case "join" -> processJoinPackage(packageData.data);
            case "leave" -> processLeavePackage(packageData.data);
            case "create" -> processCreatePackage(packageData.data);
            default -> createErrorResponse("invalid package type");
        };
    }

    public static String processLoginPackage(JSONObject data) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Login-Pakets
        try {
            // Extrahieren der Benutzerdaten aus dem Paket
            String username = data.getString("username");
            String password = data.getString("password");
            if (loginManager.checkLoginCredentials(username, password)) {
                // Erstellen einer erfolgreichen Antwort
                response = createSuccessResponse();
            } else {
                response = createErrorResponse("user does not exist");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String processJoinPackage(JSONObject data) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Join-Pakets
        try {
            // Extrahieren der Daten aus dem Paket
            String username = data.getString("username");
            String password = data.getString("password");
            String sessionId = data.getString("id");

            // Abfragen des Benutzers aus der Datenbank
            User user = loginManager.databaseConnection().getUser(username);
            if (loginManager.checkLoginCredentials(username, password) && user != null) {
                messagePipe.processMessage(sessionId, username + " joined the session.");
                // joinen der Session
                loginManager.joinSession(username, sessionId);
                messagePipe.addUser(sessionId, username);
                // Erstellen einer erfolgreichen Antwort
                response = createSuccessResponse();
            } else {
                // Erstellen einer fehlerhaften Antwort
                response = createErrorResponse("invalid session or user already a member or invalid credentials");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String processCreatePackage(JSONObject data) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Join-Pakets
        try {
            // Extrahieren der Daten aus dem Paket
            String username = data.getString("username");
            String password = data.getString("password");

            // Abfragen des Benutzers aus der Datenbank
            User user = loginManager.databaseConnection().getUser(username);
            if (loginManager.checkLoginCredentials(username, password) && user != null) {
                String session = loginManager.createSession(username);
                loginManager.joinSession(username, session);
                loginManager.joinSession(username, session);
                messagePipe.addUser(session, username);
                // messagePipe.addMessageListener(session);
                response = createSuccessResponse();
            } else {
                response = createErrorResponse("could not create session");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String processLeavePackage(JSONObject data) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Join-Pakets
        try {
            // Extrahieren der Daten aus dem Paket
            String username = data.getString("username");
            String password = data.getString("password");
            String sessionId = data.getString("id");
            // Abfragen des Benutzers aus der Datenbank
            User user = loginManager.databaseConnection().getUser(username);
            if (loginManager.checkLoginCredentials(username, password) && user != null) {
                response = createSuccessResponse();
                loginManager.leaveSession(username);
                messagePipe.processMessage(sessionId, username + " left the session");
            } else {
                // Erstellen einer fehlerhaften Antwort
                response = createErrorResponse("cannot leave session");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String createSuccessResponse() {
        // Erstellen eines JSON-Objekts für die Antwort
        JSONObject response = new JSONObject();
        // Hinzufügen des "success"-Felds zur Antwort
        response.put("success", true);
        // Rückgabe der Antwort als JSON-String
        return response.toString();
    }

    public static String createErrorResponse(String errorMessage) {
        // Erstellen eines JSON-Objekts für die Antwort
        JSONObject response = new JSONObject();
        // Hinzufügen des "success"- und "error"-Felds zur Antwort
        response.put("success", false);
        response.put("error", errorMessage);

        // Rückgabe der Antwort als JSON-String
        return response.toString();
    }

}
