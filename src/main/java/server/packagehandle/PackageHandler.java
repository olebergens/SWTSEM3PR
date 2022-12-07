package server.packagehandle;


import org.json.JSONException;
import org.json.JSONObject;
import server.database.DatabaseConnection;
import server.listener.SessionMessageListener;
import server.manager.PermissionManager;
import server.user.User;
import server.manager.LoginManager;
import server.pipe.MessagePipe;

import java.io.IOException;
import java.net.Socket;

public class PackageHandler {

    private static final MessagePipe MESSAGE_PIPE = MessagePipe.getInstance();
    private static DatabaseConnection databaseConnection;
    private static LoginManager loginManager;
    private static final PermissionManager PERMISSION_MANAGER = PermissionManager.getInstance();

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

    public static String processPackage(PackageData packageData, Socket socket) {
        // Verarbeiten des Pakets je nach Typ
        return switch (packageData.type) {
            case "login" -> processLoginPackage(packageData.data, socket);
            case "join" -> processJoinPackage(packageData.data);
            case "leave" -> processLeavePackage(packageData.data);
            case "create" -> processCreatePackage(packageData.data);
            case "messageToUser" -> processMessageToUserPackage(packageData.data);
            case "message" -> processMessagePackage(packageData.data);
            default -> createErrorResponse("invalid package type");
        };
    }

    public static String processLoginPackage(JSONObject data, Socket socket) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Login-Pakets
        try {
            // Extrahieren der Benutzerdaten aus dem Paket
            String username = data.getString("username");
            String password = data.getString("password");
            if (loginManager.checkLoginCredentials(username, password)) {
                // Erstellen einer erfolgreichen Antwort
                databaseConnection.updateUser(username, password, socket);
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
                MESSAGE_PIPE.processMessage(sessionId, username + " joined the session.");
                // joinen der Session
                loginManager.joinSession(username, sessionId);
                MESSAGE_PIPE.addUser(sessionId, username);
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
                MESSAGE_PIPE.addUser(session, username);
                MESSAGE_PIPE.addMessageListener(session, new SessionMessageListener(session));
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
                MESSAGE_PIPE.processMessage(sessionId, username + " left the session");
            } else {
                // Erstellen einer fehlerhaften Antwort
                response = createErrorResponse("cannot leave session");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String processMessageToUserPackage(JSONObject data) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Message-Pakets
        try {
            // Extrahieren der Daten aus dem Paket
            String message = data.getString("message");
            String username = data.getString("username");
            String toUsername = data.getString("toUsername");
            String sessionId = data.getString("id");
            User user = loginManager.databaseConnection().getUser(username);
            if (user != null) {
                response = createSuccessResponse();
                MESSAGE_PIPE.sendMessage(sessionId, toUsername, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String processMessagePackage(JSONObject data) {
        String response = null;
        assert isValidPackage(data);
        // Verarbeiten der Daten des Message-Pakets
        try {
            // Extrahieren der Daten aus dem Paket
            String message = data.getString("message");
            String username = data.getString("username");
            String sessionId = data.getString("id");
            User user = loginManager.databaseConnection().getUser(username);
            if (user != null) {
                response = createSuccessResponse();
                MESSAGE_PIPE.processMessage(sessionId, message);
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

    public static String createWriteMessage(String message) {
        // Erstellen eines JSON-Objektes für die Antwort
        JSONObject response = new JSONObject();
        // Hinzufügen des "message"-Felds zur Antwort
        response.put("message", message);
        return response.toString();
    }

}
