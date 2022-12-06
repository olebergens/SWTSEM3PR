package server.pipe;

import server.listener.MessageListener;
import server.monitor.ActivityMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePipe {
    private static MessagePipe instance;

    private Map<String, MessageListener> listeners;
    private Map<String, List<String>> userLists;
    private Map<String, List<ActivityMonitor>> monitors;

    private MessagePipe() {
        this.listeners = new HashMap<>();
        this.userLists = new HashMap<>();
        this.monitors = new HashMap<>();
    }

    public static MessagePipe getInstance() {
        if (instance == null) instance = new MessagePipe();
        return instance;
    }

    public void addMessageListener(String sessionID, MessageListener listener) {
        this.listeners.put(sessionID, listener);
    }

    public void removeMessageListener(String sessionID) {
        this.listeners.remove(sessionID);
    }

    public void processMessage(String sessionID, String message) {
        for (MessageListener listener : listeners.values()) listener.onMessageReceived(sessionID, message);
    }

    public void sendMessage(String sessionID, String username, String message) {
        for (MessageListener listener : listeners.values()) listener.onMessageReceived(sessionID, username, message);
    }

    public void sendFile(String sessionID, String username, File file) {
        for (MessageListener listener : listeners.values()) listener.onFileReceived(sessionID, username, file);
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

    public void addActivityMonitor(String sessionID, ActivityMonitor monitor) {
        List<ActivityMonitor> monitorList = this.monitors.computeIfAbsent(sessionID, k -> new ArrayList<>());
        monitorList.add(monitor);
    }

    public void removeActivityMonitor(String sessionID) {
        this.monitors.remove(sessionID);
    }

    private void notifyUserJoin(String sessionID, String username) {
        List<ActivityMonitor> monitorList = this.monitors.get(sessionID);
        if (monitorList != null) {
            for (ActivityMonitor monitor : monitorList) monitor.onUserJoin(sessionID, username);
        }
    }

    private void notifyUserLeave(String sessionID, String username) {
        List<ActivityMonitor> monitorList = this.monitors.get(sessionID);
        if (monitorList != null) {
            for (ActivityMonitor monitor : monitorList) monitor.onUserLeave(sessionID, username);
        }
    }

    private void notifySessionInactive(String sessionID) {
        List<ActivityMonitor> monitorList = this.monitors.get(sessionID);
        if (monitorList != null) {
            for (ActivityMonitor monitor : monitorList) monitor.onSessionInactive(sessionID);
        }
    }

}
