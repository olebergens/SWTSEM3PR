package server.pipe;

import server.Server;
import server.backend.ServerBackend;
import server.database.DatabaseConnection;
import server.listener.IMessageListener;
import server.monitor.IActivityMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePipe {
    public static DatabaseConnection databaseConnection;
    private static MessagePipe instance;
    private final Map<String, IMessageListener> listeners;
    private final Map<String, List<String>> userLists;
    private final Map<String, List<IActivityMonitor>> monitors;

    private MessagePipe() {
        this.listeners = new HashMap<>();
        this.userLists = new HashMap<>();
        this.monitors = new HashMap<>();
        databaseConnection = Server.connection;
    }

    public static MessagePipe getInstance() {
        if (instance == null) instance = new MessagePipe();
        return instance;
    }

    public void addMessageListener(String sessionID, IMessageListener listener) {
        this.listeners.put(sessionID, listener);
    }

    public void removeMessageListener(String sessionID) {
        this.listeners.remove(sessionID);
    }

    public void processMessage(String sessionID, String message) {
        for (IMessageListener listener : listeners.values()) listener.onMessageReceived(sessionID, message);
    }

    public void sendMessage(String sessionID, String username, String message) {
        for (IMessageListener listener : listeners.values()) listener.onMessageReceived(sessionID, username, message);
    }

    public void sendFile(String sessionID, String username, File file) {
        for (IMessageListener listener : listeners.values()) listener.onFileReceived(sessionID, username, file);
    }

    public void addUser(String sessionID, String username) {
        List<String> userList = this.userLists.get(sessionID);
        if (userList == null) {
            userList = new ArrayList<>();
            userLists.put(sessionID, userList);
        }
        userList.add(username);
    }

    public void removeUser(String sessionID, String username) {
        List<String> userList = userLists.get(sessionID);
        if (userList != null) userList.remove(username);
    }

    public List<String> getUserList(String sessionID) {
        return this.userLists.get(sessionID);
    }

    public void addActivityMonitor(String sessionID, IActivityMonitor monitor) {
        List<IActivityMonitor> monitorList = this.monitors.computeIfAbsent(sessionID, k -> new ArrayList<>());
        monitorList.add(monitor);
    }

    public void removeActivityMonitor(String sessionID) {
        this.monitors.remove(sessionID);
    }

    private void notifyUserJoin(String sessionID, String username) {
        List<IActivityMonitor> monitorList = this.monitors.get(sessionID);
        if (monitorList != null) {
            for (IActivityMonitor monitor : monitorList) monitor.onUserJoin(sessionID, username);
        }
    }

    private void notifyUserLeave(String sessionID, String username) {
        List<IActivityMonitor> monitorList = this.monitors.get(sessionID);
        if (monitorList != null) {
            for (IActivityMonitor monitor : monitorList) monitor.onUserLeave(sessionID, username);
        }
    }

    private void notifySessionInactive(String sessionID) {
        List<IActivityMonitor> monitorList = this.monitors.get(sessionID);
        if (monitorList != null) {
            for (IActivityMonitor monitor : monitorList) monitor.onSessionInactive(sessionID);
        }
    }

}
