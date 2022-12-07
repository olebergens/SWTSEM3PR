package server.listener;

import java.io.File;

// Schnittstelle für MessageListener
public interface IMessageListener {
    void onMessageReceived(String sessionID, String message);
    void onMessageReceived(String sessionID, String username, String message);
    void onFileReceived(String sessionID, String username, File file);
}
